package com.example.payflow.manyfast.service;

import com.example.payflow.manyfast.entity.Project;
import com.example.payflow.manyfast.entity.ProjectNode;
import com.example.payflow.manyfast.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrdGeneratorService {
    
    private final ManyfastService manyfastService;
    private final ProjectRepository projectRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:qwen2.5:1.5b}")
    private String ollamaModel;
    
    @Transactional
    public String generatePrd(String projectId) {
        Project project = manyfastService.getProject(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        List<ProjectNode> nodes = manyfastService.getAllNodes(projectId);
        
        String prompt = buildPrompt(project, nodes);
        String prd = callOllama(prompt);
        
        project.setGeneratedPrd(prd);
        projectRepository.save(project);
        
        return prd;
    }
    
    private String buildPrompt(Project project, List<ProjectNode> nodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음 프로젝트에 대한 PRD(제품 요구사항 문서)를 한글로 작성해주세요.\n\n");
        sb.append("프로젝트명: ").append(project.getName()).append("\n");
        sb.append("설명: ").append(project.getDescription()).append("\n\n");
        sb.append("기능 구조:\n");
        
        for (ProjectNode node : nodes) {
            sb.append("- [").append(node.getNodeType()).append("] ");
            sb.append(node.getTitle());
            if (node.getDescription() != null && !node.getDescription().isEmpty()) {
                sb.append(": ").append(node.getDescription());
            }
            sb.append("\n");
        }
        
        sb.append("\n다음 섹션을 포함한 PRD를 작성해주세요: 개요, 목표, 기능 상세, 사용자 스토리, 기술 요구사항, 성공 지표");
        
        return sb.toString();
    }
    
    private String callOllama(String prompt) {
        try {
            log.info("Calling Ollama at {} with model {}", ollamaUrl, ollamaModel);
            
            WebClient webClient = webClientBuilder
                .baseUrl(ollamaUrl)
                .build();
            
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
            
            log.warn("Ollama response was empty");
            return generateFallbackPRD();
            
        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage());
            return generateFallbackPRD();
        }
    }
    
    private String generateFallbackPRD() {
        return """
            # PRD (제품 요구사항 문서)
            
            ## 개요
            Ollama 연결에 실패했습니다.
            
            ## 확인사항
            1. Ollama가 실행 중인지 확인: ollama serve
            2. 모델이 설치되어 있는지 확인: ollama list
            3. 모델 설치: ollama pull qwen2.5:1.5b
            
            ## 설정
            application.properties에서 설정 확인:
            - ollama.url=http://localhost:11434
            - ollama.model=qwen2.5:1.5b
            """;
    }
}
