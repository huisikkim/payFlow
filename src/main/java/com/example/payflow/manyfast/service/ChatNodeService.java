package com.example.payflow.manyfast.service;

import com.example.payflow.manyfast.dto.NodeRequest;
import com.example.payflow.manyfast.entity.ProjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNodeService {
    
    private final ManyfastService manyfastService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:llama3.2:latest}")
    private String ollamaModel;
    
    public Map<String, Object> processChat(String projectId, String userMessage) {
        List<ProjectNode> existingNodes = manyfastService.getAllNodes(projectId);
        String prompt = buildPrompt(userMessage, existingNodes);
        String aiResponse = callOllama(prompt);
        
        List<Map<String, String>> parsedNodes = parseNodesFromResponse(aiResponse);
        int nodesCreated = 0;
        
        for (Map<String, String> nodeData : parsedNodes) {
            try {
                NodeRequest request = new NodeRequest();
                request.setTitle(nodeData.get("title"));
                request.setDescription(nodeData.get("description"));
                request.setNodeType(nodeData.getOrDefault("type", "FEATURE"));
                request.setPosition(existingNodes.size() + nodesCreated);
                
                String parentTitle = nodeData.get("parent");
                if (parentTitle != null && !parentTitle.isEmpty()) {
                    existingNodes.stream()
                        .filter(n -> n.getTitle().contains(parentTitle) || parentTitle.contains(n.getTitle()))
                        .findFirst()
                        .ifPresent(parent -> request.setParentId(parent.getId()));
                }
                
                manyfastService.createNode(projectId, request);
                nodesCreated++;
            } catch (Exception e) {
                log.error("Failed to create node: {}", e.getMessage());
            }
        }
        
        String responseText = nodesCreated > 0 
            ? String.format("%d개의 기능이 추가되었습니다.\n\n%s", nodesCreated, cleanResponse(aiResponse))
            : cleanResponse(aiResponse);
        
        return Map.of(
            "response", responseText,
            "nodesCreated", nodesCreated
        );
    }
    
    private String buildPrompt(String userMessage, List<ProjectNode> existingNodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 소프트웨어 기능 설계 전문가입니다. 사용자의 요청을 분석하여 기능을 정의해주세요.\n\n");
        
        if (!existingNodes.isEmpty()) {
            sb.append("현재 프로젝트의 기능 구조:\n");
            for (ProjectNode node : existingNodes) {
                sb.append("- [").append(node.getNodeType()).append("] ").append(node.getTitle()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("사용자 요청: ").append(userMessage).append("\n\n");
        sb.append("다음 형식으로 기능을 정의해주세요 (여러 개 가능):\n");
        sb.append("[NODE]\n");
        sb.append("title: 기능 이름\n");
        sb.append("type: FEATURE 또는 TASK 또는 REQUIREMENT\n");
        sb.append("description: 기능 설명\n");
        sb.append("parent: 상위 기능 이름 (없으면 생략)\n");
        sb.append("[/NODE]\n\n");
        sb.append("기능 정의 후 간단한 설명도 추가해주세요.");
        
        return sb.toString();
    }
    
    private List<Map<String, String>> parseNodesFromResponse(String response) {
        List<Map<String, String>> nodes = new ArrayList<>();
        
        // [NODE]...[/NODE] 형식 파싱
        Pattern pattern = Pattern.compile("\\[NODE\\](.*?)\\[/NODE\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            Map<String, String> nodeData = parseNodeBlock(matcher.group(1));
            if (nodeData.containsKey("title") && !nodeData.get("title").isEmpty()) {
                nodes.add(nodeData);
            }
        }
        
        // [FEATURE], [TASK], [REQUIREMENT] 형식도 파싱
        if (nodes.isEmpty()) {
            Pattern altPattern = Pattern.compile("\\[(FEATURE|TASK|REQUIREMENT)\\]\\s*([^\n]+)", Pattern.MULTILINE);
            Matcher altMatcher = altPattern.matcher(response);
            
            while (altMatcher.find()) {
                Map<String, String> nodeData = new HashMap<>();
                nodeData.put("type", altMatcher.group(1));
                nodeData.put("title", altMatcher.group(2).trim());
                
                // 다음 줄들에서 description, parent 찾기
                int start = altMatcher.end();
                int end = response.indexOf("[", start);
                if (end == -1) end = response.length();
                String block = response.substring(start, end);
                
                for (String line : block.split("\n")) {
                    line = line.trim();
                    if (line.toLowerCase().startsWith("description:")) {
                        nodeData.put("description", line.substring(12).trim());
                    } else if (line.toLowerCase().startsWith("parent:")) {
                        nodeData.put("parent", line.substring(7).trim());
                    } else if (line.toLowerCase().startsWith("type:")) {
                        // type이 이미 있으면 스킵
                    }
                }
                
                if (!nodeData.get("title").isEmpty()) {
                    nodes.add(nodeData);
                }
            }
        }
        
        return nodes;
    }
    
    private Map<String, String> parseNodeBlock(String block) {
        Map<String, String> nodeData = new HashMap<>();
        for (String line : block.split("\n")) {
            line = line.trim();
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    nodeData.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        }
        return nodeData;
    }
    
    private String cleanResponse(String response) {
        return response.replaceAll("\\[NODE\\].*?\\[/NODE\\]", "").trim()
            .replaceAll("\n{3,}", "\n\n");
    }
    
    private String callOllama(String prompt) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(ollamaUrl).build();
            
            Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
            );
            
            Map<String, Object> response = webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMinutes(2))
                .block();
            
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            }
            return "응답을 생성하지 못했습니다.";
        } catch (Exception e) {
            log.error("Ollama call failed: {}", e.getMessage());
            return "AI 연결에 실패했습니다. Ollama가 실행 중인지 확인해주세요.";
        }
    }
}
