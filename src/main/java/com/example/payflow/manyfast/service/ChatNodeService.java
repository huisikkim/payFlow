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
        
        // 사용자 메시지에서 parent 힌트 추출
        String parentHint = extractParentHint(userMessage, existingNodes);
        log.info("Parent hint from user message: {}", parentHint);
        
        int nodesCreated = 0;
        
        for (Map<String, String> nodeData : parsedNodes) {
            try {
                NodeRequest request = new NodeRequest();
                request.setTitle(nodeData.get("title"));
                request.setDescription(nodeData.get("description"));
                request.setNodeType(nodeData.getOrDefault("type", "FEATURE"));
                request.setPosition(existingNodes.size() + nodesCreated);
                
                String parentTitle = nodeData.get("parent");
                // AI가 parent를 비워뒀으면 사용자 메시지에서 추출한 힌트 사용
                if ((parentTitle == null || parentTitle.isEmpty()) && parentHint != null) {
                    parentTitle = parentHint;
                    log.info("Using parent hint: {}", parentHint);
                }
                if (parentTitle != null && !parentTitle.isEmpty()) {
                    // 특수문자, 공백 정리
                    String cleanParent = parentTitle.trim()
                        .replaceAll("[^가-힣a-zA-Z0-9\\s]", "")
                        .replaceAll("\\s+", " ")
                        .trim();
                    
                    log.info("Looking for parent: '{}' (cleaned: '{}')", parentTitle, cleanParent);
                    
                    // 1. 정확한 매칭
                    Optional<ProjectNode> match = existingNodes.stream()
                        .filter(n -> n.getTitle().equalsIgnoreCase(cleanParent))
                        .findFirst();
                    
                    // 2. 부분 매칭 (한글 키워드 기준)
                    if (match.isEmpty()) {
                        String[] keywords = cleanParent.split("\\s+");
                        match = existingNodes.stream()
                            .filter(n -> {
                                String nodeTitle = n.getTitle().toLowerCase();
                                for (String kw : keywords) {
                                    if (kw.length() >= 2 && nodeTitle.contains(kw.toLowerCase())) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .findFirst();
                    }
                    
                    // 3. 유사도 기반 매칭 (공통 글자 수)
                    if (match.isEmpty() && cleanParent.length() >= 2) {
                        match = existingNodes.stream()
                            .max((a, b) -> {
                                int scoreA = countCommonChars(a.getTitle(), cleanParent);
                                int scoreB = countCommonChars(b.getTitle(), cleanParent);
                                return Integer.compare(scoreA, scoreB);
                            })
                            .filter(n -> countCommonChars(n.getTitle(), cleanParent) >= 2);
                    }
                    
                    match.ifPresent(parent -> {
                        request.setParentId(parent.getId());
                        log.info("Parent matched: '{}' -> '{}'(id:{})", cleanParent, parent.getTitle(), parent.getId());
                    });
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
        sb.append("당신은 소프트웨어 기능 설계 전문가입니다. 사용자의 요청을 분석하여 새로운 기능을 정의해주세요.\n\n");
        
        // 한글 강제 지시
        sb.append("★★★ 중요: 반드시 한글로만 응답하세요! ★★★\n");
        sb.append("- 한자(漢字) 사용 금지\n");
        sb.append("- 일본어 사용 금지\n");
        sb.append("- 기능명, 설명 모두 순수 한글과 영문만 사용\n");
        sb.append("- 예: 검색機能(X) → 검색 기능(O), 登録(X) → 등록(O)\n\n");
        
        if (!existingNodes.isEmpty()) {
            sb.append("=== 현재 프로젝트의 기존 기능 목록 ===\n");
            for (int i = 0; i < existingNodes.size(); i++) {
                ProjectNode node = existingNodes.get(i);
                sb.append(i + 1).append(". [").append(node.getNodeType()).append("] ").append(node.getTitle());
                if (node.getDescription() != null && !node.getDescription().isEmpty()) {
                    sb.append(" - ").append(node.getDescription());
                }
                sb.append("\n");
            }
            sb.append("=====================================\n\n");
        }
        
        sb.append("사용자 요청: ").append(userMessage).append("\n\n");
        sb.append("중요 규칙:\n");
        sb.append("1. 사용자가 '~에', '~의', '~기능에' 등으로 기존 기능을 언급하면 해당 기능을 parent로 지정하세요.\n");
        sb.append("2. parent는 위 기존 기능 목록에서 정확한 이름을 복사해서 사용하세요.\n");
        sb.append("3. 모든 텍스트는 한글과 영문만 사용하세요. 한자 절대 금지!\n\n");
        sb.append("반드시 다음 형식으로 기능을 정의해주세요:\n");
        sb.append("[NODE]\n");
        sb.append("title: 새 기능 이름 (한글/영문만)\n");
        sb.append("type: FEATURE\n");
        sb.append("description: 기능 설명 (한글/영문만)\n");
        sb.append("parent: 상위 기능 이름 (기존 기능 목록에서 복사)\n");
        sb.append("[/NODE]\n\n");
        sb.append("예시 - '공고내용 목록에 검색 추가해줘' 요청 시:\n");
        sb.append("[NODE]\n");
        sb.append("title: 검색 기능\n");
        sb.append("type: TASK\n");
        sb.append("description: 공고 내용을 검색할 수 있습니다.\n");
        sb.append("parent: 공고내용 목록\n");
        sb.append("[/NODE]");
        
        return sb.toString();
    }
    
    private List<Map<String, String>> parseNodesFromResponse(String response) {
        List<Map<String, String>> nodes = new ArrayList<>();
        log.info("Parsing AI response: {}", response.substring(0, Math.min(200, response.length())));
        
        // [NODE]...[/NODE] 형식 파싱
        Pattern pattern = Pattern.compile("\\[NODE\\](.*?)\\[/NODE\\]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            Map<String, String> nodeData = parseNodeBlock(matcher.group(1));
            if (nodeData.containsKey("title") && !nodeData.get("title").isEmpty()) {
                log.info("Parsed NODE block: {}", nodeData);
                nodes.add(nodeData);
            }
        }
        
        // [FEATURE], [TASK], [REQUIREMENT] 형식도 파싱
        if (nodes.isEmpty()) {
            Pattern altPattern = Pattern.compile("\\[(FEATURE|TASK|REQUIREMENT)\\]\\s*([^\n]+)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher altMatcher = altPattern.matcher(response);
            
            while (altMatcher.find()) {
                Map<String, String> nodeData = new HashMap<>();
                nodeData.put("type", altMatcher.group(1).toUpperCase());
                nodeData.put("title", altMatcher.group(2).trim());
                
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
                    }
                }
                
                if (!nodeData.get("title").isEmpty()) {
                    log.info("Parsed ALT format: {}", nodeData);
                    nodes.add(nodeData);
                }
            }
        }
        
        // title: xxx 형식으로 직접 파싱 (fallback)
        if (nodes.isEmpty()) {
            Pattern titlePattern = Pattern.compile("title:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
            Matcher titleMatcher = titlePattern.matcher(response);
            
            while (titleMatcher.find()) {
                String title = titleMatcher.group(1).trim();
                if (!title.isEmpty()) {
                    Map<String, String> nodeData = new HashMap<>();
                    nodeData.put("title", title);
                    nodeData.put("type", "FEATURE");
                    
                    // 주변에서 type, description, parent 찾기
                    int searchStart = Math.max(0, titleMatcher.start() - 50);
                    int searchEnd = Math.min(response.length(), titleMatcher.end() + 200);
                    String context = response.substring(searchStart, searchEnd);
                    
                    Pattern typeP = Pattern.compile("type:\\s*(FEATURE|TASK|REQUIREMENT)", Pattern.CASE_INSENSITIVE);
                    Matcher typeM = typeP.matcher(context);
                    if (typeM.find()) nodeData.put("type", typeM.group(1).toUpperCase());
                    
                    Pattern descP = Pattern.compile("description:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
                    Matcher descM = descP.matcher(context);
                    if (descM.find()) nodeData.put("description", descM.group(1).trim());
                    
                    Pattern parentP = Pattern.compile("parent:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
                    Matcher parentM = parentP.matcher(context);
                    if (parentM.find()) nodeData.put("parent", parentM.group(1).trim());
                    
                    log.info("Parsed fallback: {}", nodeData);
                    nodes.add(nodeData);
                }
            }
        }
        
        log.info("Total nodes parsed: {}", nodes.size());
        return nodes;
    }
    
    private Map<String, String> parseNodeBlock(String block) {
        Map<String, String> nodeData = new HashMap<>();
        for (String line : block.split("\n")) {
            line = line.trim();
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    // 한자를 한글로 변환 후 저장
                    String value = convertHanjaToKorean(parts[1].trim());
                    nodeData.put(parts[0].trim().toLowerCase(), value);
                }
            }
        }
        return nodeData;
    }
    
    private String cleanResponse(String response) {
        String cleaned = response.replaceAll("\\[NODE\\].*?\\[/NODE\\]", "").trim()
            .replaceAll("\n{3,}", "\n\n");
        return convertHanjaToKorean(cleaned);
    }
    
    /**
     * 한자를 한글로 변환 (자주 나오는 한자 매핑)
     */
    private String convertHanjaToKorean(String text) {
        if (text == null) return null;
        
        // 자주 나오는 한자 → 한글 매핑
        Map<String, String> hanjaMap = new LinkedHashMap<>();
        hanjaMap.put("機能", "기능");
        hanjaMap.put("検索", "검색");
        hanjaMap.put("登録", "등록");
        hanjaMap.put("削除", "삭제");
        hanjaMap.put("修正", "수정");
        hanjaMap.put("編集", "편집");
        hanjaMap.put("一覧", "목록");
        hanjaMap.put("詳細", "상세");
        hanjaMap.put("管理", "관리");
        hanjaMap.put("設定", "설정");
        hanjaMap.put("追加", "추가");
        hanjaMap.put("確認", "확인");
        hanjaMap.put("保存", "저장");
        hanjaMap.put("取消", "취소");
        hanjaMap.put("完了", "완료");
        hanjaMap.put("開始", "시작");
        hanjaMap.put("終了", "종료");
        hanjaMap.put("入力", "입력");
        hanjaMap.put("出力", "출력");
        hanjaMap.put("表示", "표시");
        hanjaMap.put("非表示", "숨김");
        hanjaMap.put("有効", "유효");
        hanjaMap.put("無効", "무효");
        hanjaMap.put("新規", "신규");
        hanjaMap.put("更新", "갱신");
        hanjaMap.put("変更", "변경");
        hanjaMap.put("選択", "선택");
        hanjaMap.put("解除", "해제");
        hanjaMap.put("実行", "실행");
        hanjaMap.put("停止", "정지");
        hanjaMap.put("再開", "재개");
        hanjaMap.put("送信", "전송");
        hanjaMap.put("受信", "수신");
        hanjaMap.put("認証", "인증");
        hanjaMap.put("権限", "권한");
        hanjaMap.put("使用者", "사용자");
        hanjaMap.put("利用者", "이용자");
        hanjaMap.put("画面", "화면");
        hanjaMap.put("情報", "정보");
        hanjaMap.put("内容", "내용");
        hanjaMap.put("項目", "항목");
        hanjaMap.put("条件", "조건");
        hanjaMap.put("結果", "결과");
        hanjaMap.put("処理", "처리");
        hanjaMap.put("作成", "작성");
        hanjaMap.put("生成", "생성");
        hanjaMap.put("分析", "분석");
        hanjaMap.put("統計", "통계");
        hanjaMap.put("報告", "보고");
        hanjaMap.put("履歴", "이력");
        hanjaMap.put("記録", "기록");
        hanjaMap.put("日付", "날짜");
        hanjaMap.put("時間", "시간");
        hanjaMap.put("期間", "기간");
        hanjaMap.put("数量", "수량");
        hanjaMap.put("金額", "금액");
        hanjaMap.put("価格", "가격");
        hanjaMap.put("合計", "합계");
        hanjaMap.put("小計", "소계");
        hanjaMap.put("総計", "총계");
        
        String result = text;
        for (Map.Entry<String, String> entry : hanjaMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
    
    private String extractParentHint(String userMessage, List<ProjectNode> existingNodes) {
        // 한글만 추출해서 비교
        String msgKorean = userMessage.replaceAll("[^가-힣]", "").toLowerCase();
        log.info("Extracting parent from message: '{}' -> korean: '{}'", userMessage, msgKorean);
        
        ProjectNode bestMatch = null;
        int bestScore = 0;
        
        for (ProjectNode node : existingNodes) {
            String titleKorean = node.getTitle().replaceAll("[^가-힣]", "").toLowerCase();
            log.info("Checking node: '{}' -> korean: '{}'", node.getTitle(), titleKorean);
            
            if (titleKorean.length() >= 2 && msgKorean.contains(titleKorean)) {
                int score = titleKorean.length();
                if (node.getParentId() == null) score += 100;
                
                log.info("Match found! score: {}", score);
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = node;
                }
            }
        }
        
        if (bestMatch != null) {
            log.info("Best parent match: '{}' (score: {})", bestMatch.getTitle(), bestScore);
            return bestMatch.getTitle();
        }
        
        log.info("No parent match found");
        return null;
    }
    
    private int countCommonChars(String a, String b) {
        int count = 0;
        String shorter = a.length() < b.length() ? a : b;
        String longer = a.length() < b.length() ? b : a;
        for (char c : shorter.toCharArray()) {
            if (longer.indexOf(c) >= 0) count++;
        }
        return count;
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
