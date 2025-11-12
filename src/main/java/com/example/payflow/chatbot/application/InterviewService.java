package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;

    // 포지션별 면접 질문 템플릿
    private static final Map<String, List<QuestionTemplate>> QUESTION_TEMPLATES = new HashMap<>();

    static {
        // 백엔드 개발자 질문
        QUESTION_TEMPLATES.put("백엔드", Arrays.asList(
            new QuestionTemplate("Java의 JVM 메모리 구조에 대해 설명해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("Spring Boot에서 @Transactional의 동작 원리를 설명해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("RESTful API 설계 시 고려해야 할 사항은 무엇인가요?", QuestionCategory.TECHNICAL),
            new QuestionTemplate("가장 어려웠던 기술적 문제와 해결 방법을 설명해주세요.", QuestionCategory.PROBLEM_SOLVING),
            new QuestionTemplate("팀 프로젝트에서 어떤 역할을 주로 맡으셨나요?", QuestionCategory.EXPERIENCE)
        ));

        // 프론트엔드 개발자 질문
        QUESTION_TEMPLATES.put("프론트엔드", Arrays.asList(
            new QuestionTemplate("React의 Virtual DOM이 무엇이고 왜 사용하나요?", QuestionCategory.TECHNICAL),
            new QuestionTemplate("JavaScript의 클로저(Closure)에 대해 설명해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("웹 성능 최적화를 위해 어떤 방법들을 사용해보셨나요?", QuestionCategory.EXPERIENCE),
            new QuestionTemplate("반응형 웹 디자인 구현 경험을 말씀해주세요.", QuestionCategory.PROJECT),
            new QuestionTemplate("크로스 브라우징 이슈를 어떻게 해결하셨나요?", QuestionCategory.PROBLEM_SOLVING)
        ));

        // 풀스택 개발자 질문
        QUESTION_TEMPLATES.put("풀스택", Arrays.asList(
            new QuestionTemplate("프론트엔드와 백엔드 중 어느 쪽에 더 강점이 있나요?", QuestionCategory.EXPERIENCE),
            new QuestionTemplate("데이터베이스 설계 시 정규화와 비정규화를 어떻게 선택하나요?", QuestionCategory.TECHNICAL),
            new QuestionTemplate("마이크로서비스 아키텍처의 장단점을 설명해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("처음부터 끝까지 혼자 개발한 프로젝트가 있나요?", QuestionCategory.PROJECT),
            new QuestionTemplate("새로운 기술을 학습하는 본인만의 방법이 있나요?", QuestionCategory.CULTURE_FIT)
        ));

        // DevOps 엔지니어 질문
        QUESTION_TEMPLATES.put("DevOps", Arrays.asList(
            new QuestionTemplate("CI/CD 파이프라인을 구축한 경험을 설명해주세요.", QuestionCategory.EXPERIENCE),
            new QuestionTemplate("Docker와 Kubernetes의 차이점을 설명해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("AWS 서비스 중 주로 사용해본 것들을 말씀해주세요.", QuestionCategory.TECHNICAL),
            new QuestionTemplate("장애 발생 시 어떻게 대응하시나요?", QuestionCategory.PROBLEM_SOLVING),
            new QuestionTemplate("인프라 모니터링은 어떤 도구를 사용하시나요?", QuestionCategory.EXPERIENCE)
        ));

        // 기본 질문 (포지션 매칭 안될 때)
        QUESTION_TEMPLATES.put("기본", Arrays.asList(
            new QuestionTemplate("본인의 강점과 약점을 말씀해주세요.", QuestionCategory.CULTURE_FIT),
            new QuestionTemplate("가장 자랑스러운 프로젝트 경험을 공유해주세요.", QuestionCategory.PROJECT),
            new QuestionTemplate("기술적으로 어려운 문제를 해결한 경험이 있나요?", QuestionCategory.PROBLEM_SOLVING),
            new QuestionTemplate("팀원과 의견 충돌이 있을 때 어떻게 해결하나요?", QuestionCategory.CULTURE_FIT),
            new QuestionTemplate("5년 후 본인의 모습을 어떻게 그리고 계신가요?", QuestionCategory.CULTURE_FIT)
        ));
    }

    @Transactional
    public Interview startInterview(Long conversationId, Long jobId, String userId, List<String> techStacks) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        Interview interview = new Interview(conversationId, jobId, userId, techStacks);
        
        // 포지션에 맞는 질문 생성
        List<QuestionTemplate> templates = getQuestionTemplates(job.getPosition());
        int questionNumber = 1;
        for (QuestionTemplate template : templates) {
            InterviewQuestion question = new InterviewQuestion(
                questionNumber++,
                template.question,
                template.category
            );
            interview.addQuestion(question);
        }

        return interviewRepository.save(interview);
    }

    @Transactional
    public void answerQuestion(Long interviewId, Integer questionNumber, String answer) {
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        InterviewQuestion question = interview.getQuestions().stream()
            .filter(q -> q.getQuestionNumber().equals(questionNumber))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // 답변 평가 (간단한 규칙 기반)
        int score = evaluateAnswer(answer, question.getCategory());
        String feedback = generateFeedback(score, question.getCategory());

        question.answerQuestion(answer, score, feedback);
        interviewRepository.save(interview);
    }

    @Transactional
    public InterviewResult completeInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        // 총점 계산
        int totalScore = interview.getQuestions().stream()
            .filter(InterviewQuestion::isAnswered)
            .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
            .sum();

        int answeredCount = interview.getAnsweredQuestionsCount();
        int averageScore = answeredCount > 0 ? totalScore / answeredCount : 0;

        // 합격률 계산 (평균 점수 기반)
        double passRate = calculatePassRate(averageScore, interview.getUserTechStacks().size());

        interview.complete(totalScore, passRate);
        interviewRepository.save(interview);

        return new InterviewResult(
            interview.getId(),
            totalScore,
            averageScore,
            passRate,
            answeredCount,
            interview.getQuestions().size(),
            generateOverallFeedback(averageScore, passRate)
        );
    }

    public Optional<Interview> getInterviewByConversation(Long conversationId) {
        return interviewRepository.findByConversationId(conversationId);
    }

    public InterviewQuestion getNextQuestion(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        return interview.getQuestions().stream()
            .filter(q -> !q.isAnswered())
            .findFirst()
            .orElse(null);
    }

    private List<QuestionTemplate> getQuestionTemplates(String position) {
        for (String key : QUESTION_TEMPLATES.keySet()) {
            if (position.contains(key)) {
                return QUESTION_TEMPLATES.get(key);
            }
        }
        return QUESTION_TEMPLATES.get("기본");
    }

    private int evaluateAnswer(String answer, QuestionCategory category) {
        if (answer == null || answer.trim().isEmpty()) {
            return 0;
        }

        int baseScore = 50;
        int lengthBonus = Math.min(answer.length() / 20, 20); // 길이에 따른 보너스 (최대 20점)
        
        // 키워드 기반 점수 (간단한 휴리스틱)
        int keywordBonus = 0;
        String lowerAnswer = answer.toLowerCase();
        
        if (category == QuestionCategory.TECHNICAL) {
            String[] techKeywords = {"구조", "원리", "방식", "알고리즘", "최적화", "성능", "설계"};
            keywordBonus = Arrays.stream(techKeywords)
                .filter(lowerAnswer::contains)
                .mapToInt(k -> 5)
                .sum();
        } else if (category == QuestionCategory.EXPERIENCE) {
            String[] expKeywords = {"경험", "프로젝트", "개발", "구현", "사용", "적용"};
            keywordBonus = Arrays.stream(expKeywords)
                .filter(lowerAnswer::contains)
                .mapToInt(k -> 5)
                .sum();
        }

        return Math.min(baseScore + lengthBonus + keywordBonus, 100);
    }

    private String generateFeedback(int score, QuestionCategory category) {
        if (score >= 80) {
            return "훌륭한 답변입니다! 해당 분야에 대한 깊은 이해가 느껴집니다. 👍";
        } else if (score >= 60) {
            return "좋은 답변입니다. 조금 더 구체적인 예시를 추가하면 더 좋을 것 같습니다.";
        } else if (score >= 40) {
            return "기본적인 내용은 이해하고 계시네요. 좀 더 깊이 있는 학습이 필요해 보입니다.";
        } else {
            return "답변이 다소 부족합니다. 해당 주제에 대해 더 공부해보시는 것을 추천드립니다.";
        }
    }

    private double calculatePassRate(int averageScore, int techStackCount) {
        // 기본 합격률 (평균 점수 기반)
        double baseRate = averageScore * 0.7; // 70% 가중치
        
        // 기술 스택 보너스 (최대 30%)
        double techBonus = Math.min(techStackCount * 5, 30);
        
        return Math.min(baseRate + techBonus, 95.0); // 최대 95%
    }

    private String generateOverallFeedback(int averageScore, double passRate) {
        if (passRate >= 80) {
            return "🎉 축하합니다! 매우 우수한 면접 결과입니다. 합격 가능성이 높습니다!";
        } else if (passRate >= 60) {
            return "👍 좋은 면접이었습니다. 합격 가능성이 있으니 자신감을 가지세요!";
        } else if (passRate >= 40) {
            return "💪 나쁘지 않은 결과입니다. 부족한 부분을 보완하면 더 좋은 결과를 얻을 수 있을 것입니다.";
        } else {
            return "📚 조금 더 준비가 필요해 보입니다. 기술 면접 준비를 더 하시는 것을 추천드립니다.";
        }
    }

    private static class QuestionTemplate {
        String question;
        QuestionCategory category;

        QuestionTemplate(String question, QuestionCategory category) {
            this.question = question;
            this.category = category;
        }
    }

    public static class InterviewResult {
        public final Long interviewId;
        public final int totalScore;
        public final int averageScore;
        public final double passRate;
        public final int answeredCount;
        public final int totalQuestions;
        public final String overallFeedback;

        public InterviewResult(Long interviewId, int totalScore, int averageScore, 
                             double passRate, int answeredCount, int totalQuestions, 
                             String overallFeedback) {
            this.interviewId = interviewId;
            this.totalScore = totalScore;
            this.averageScore = averageScore;
            this.passRate = passRate;
            this.answeredCount = answeredCount;
            this.totalQuestions = totalQuestions;
            this.overallFeedback = overallFeedback;
        }
    }
}
