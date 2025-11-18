package com.example.payflow.ainjob.application;

import com.example.payflow.ainjob.application.dto.ApplicantMatchingScore;
import com.example.payflow.ainjob.application.dto.QualifiedApplicantResponse;
import com.example.payflow.ainjob.domain.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantMatchingService {
    
    private final ApplicantRepository applicantRepository;
    private final AinjobJobPostingRepository jobPostingRepository;
    private final ApplicationTrackingRepository applicationTrackingRepository;
    private final Gson gson = new Gson();
    
    /**
     * 특정 채용 공고에 지원한 지원자 중 자격 요건을 충족하는 지원자 목록 조회
     */
    public List<QualifiedApplicantResponse> getQualifiedApplicants(Long jobPostingId) {
        // 채용 공고 조회
        AinjobJobPosting jobPosting = jobPostingRepository.findByIdWithSkills(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + jobPostingId));
        
        // 해당 공고에 지원한 지원자 ID 목록
        List<Long> applicantIds = applicationTrackingRepository.findByJobPostingId(jobPostingId)
            .stream()
            .map(ApplicationTracking::getApplicantId)
            .collect(Collectors.toList());
        
        if (applicantIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 지원자 상세 정보 조회
        List<Applicant> applicants = applicantRepository.findByIdInWithDetails(applicantIds);
        
        // 각 지원자의 자격 요건 매칭 평가
        return applicants.stream()
            .map(applicant -> evaluateApplicant(applicant, jobPosting))
            .sorted(Comparator.comparing(QualifiedApplicantResponse::getMatchingScore).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * 특정 지원의 매칭 점수 계산
     */
    public ApplicantMatchingScore calculateMatchingScore(Long applicationId) {
        ApplicationTracking application = applicationTrackingRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다: " + applicationId));
        
        Applicant applicant = applicantRepository.findByIdWithDetails(application.getApplicantId())
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + application.getApplicantId()));
        
        AinjobJobPosting jobPosting = jobPostingRepository.findByIdWithSkills(application.getJobPostingId())
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + application.getJobPostingId()));
        
        return calculateDetailedScore(applicant, jobPosting);
    }
    
    /**
     * 지원자 평가
     */
    private QualifiedApplicantResponse evaluateApplicant(Applicant applicant, AinjobJobPosting jobPosting) {
        Qualification qualification = jobPosting.getQualification();
        
        // 1. 학력 체크
        EducationLevel highestEducation = getHighestEducation(applicant);
        boolean educationMatched = checkEducation(highestEducation, qualification.getMinEducationLevel());
        
        // 2. 전공 체크
        String majorName = getHighestEducationMajor(applicant);
        List<String> acceptedMajors = parseAcceptedMajors(qualification.getAcceptedMajors());
        boolean majorMatched = checkMajor(majorName, acceptedMajors);
        
        // 3. 스킬 체크 (대소문자 무시, 원본 이름 유지)
        Set<String> applicantSkillsLower = getApplicantSkills(applicant);
        Set<String> requiredSkillsLower = getRequiredSkills(jobPosting);
        boolean skillsMatched = checkSkills(applicantSkillsLower, requiredSkillsLower);
        
        // 원본 스킬명 가져오기
        List<String> applicantSkillsOriginal = applicant.getCareers().stream()
            .flatMap(career -> career.getSkills().stream())
            .map(cs -> cs.getSkill().getName())
            .distinct()
            .collect(Collectors.toList());
        
        // 4. 경력 체크
        double totalExperience = calculateTotalExperience(applicant);
        boolean experienceMatched = totalExperience >= qualification.getMinYearsOfExperience();
        
        // 매칭 점수 계산 (각 항목 25점)
        int matchingScore = 0;
        if (educationMatched) matchingScore += 25;
        if (majorMatched) matchingScore += 25;
        if (skillsMatched) matchingScore += 25;
        if (experienceMatched) matchingScore += 25;
        
        // 자격 충족 여부
        boolean isQualified = educationMatched && majorMatched && skillsMatched && experienceMatched;
        
        // 매칭 사유
        String matchingReason = buildMatchingReason(educationMatched, majorMatched, skillsMatched, experienceMatched);
        
        return new QualifiedApplicantResponse(
            applicant.getId(),
            applicant.getName(),
            applicant.getEmail(),
            highestEducation != null ? highestEducation.name() : "없음",
            majorName,
            applicantSkillsOriginal,
            totalExperience,
            matchingScore,
            matchingReason,
            isQualified,
            educationMatched,
            majorMatched,
            skillsMatched,
            experienceMatched
        );
    }
    
    /**
     * 상세 점수 계산
     */
    private ApplicantMatchingScore calculateDetailedScore(Applicant applicant, AinjobJobPosting jobPosting) {
        Qualification qualification = jobPosting.getQualification();
        
        // 학력 점수 (25점)
        EducationLevel highestEducation = getHighestEducation(applicant);
        int educationScore = checkEducation(highestEducation, qualification.getMinEducationLevel()) ? 25 : 0;
        
        // 전공 점수 (25점)
        String majorName = getHighestEducationMajor(applicant);
        List<String> acceptedMajors = parseAcceptedMajors(qualification.getAcceptedMajors());
        int majorScore = checkMajor(majorName, acceptedMajors) ? 25 : 0;
        
        // 스킬 점수 (25점) - 원본 스킬명 유지
        Map<String, String> applicantSkillsMap = applicant.getCareers().stream()
            .flatMap(career -> career.getSkills().stream())
            .collect(Collectors.toMap(
                cs -> cs.getSkill().getName().toLowerCase().trim(),
                cs -> cs.getSkill().getName(),
                (existing, replacement) -> existing
            ));
        
        Map<String, String> requiredSkillsMap = jobPosting.getRequiredSkills().stream()
            .filter(JobPostingSkill::getIsRequired)
            .collect(Collectors.toMap(
                jps -> jps.getSkill().getName().toLowerCase().trim(),
                jps -> jps.getSkill().getName(),
                (existing, replacement) -> existing
            ));
        
        Set<String> applicantSkillsLower = applicantSkillsMap.keySet();
        Set<String> requiredSkillsLower = requiredSkillsMap.keySet();
        
        int skillScore = applicantSkillsLower.containsAll(requiredSkillsLower) ? 25 : 0;
        
        // 매칭된 스킬 (원본 이름)
        List<String> matchedSkills = requiredSkillsLower.stream()
            .filter(applicantSkillsLower::contains)
            .map(requiredSkillsMap::get)
            .collect(Collectors.toList());
        
        // 부족한 스킬 (원본 이름)
        List<String> missingSkills = requiredSkillsLower.stream()
            .filter(skill -> !applicantSkillsLower.contains(skill))
            .map(requiredSkillsMap::get)
            .collect(Collectors.toList());
        
        // 경력 점수 (25점)
        double totalExperience = calculateTotalExperience(applicant);
        int experienceScore = totalExperience >= qualification.getMinYearsOfExperience() ? 25 : 0;
        
        int totalScore = educationScore + majorScore + skillScore + experienceScore;
        double percentage = (totalScore / 100.0) * 100;
        
        String recommendation;
        if (totalScore == 100) {
            recommendation = "모든 자격 요건을 충족합니다. 적극 추천합니다.";
        } else if (totalScore >= 75) {
            recommendation = "대부분의 자격 요건을 충족합니다. 면접 진행을 권장합니다.";
        } else if (totalScore >= 50) {
            recommendation = "일부 자격 요건이 부족합니다. 신중한 검토가 필요합니다.";
        } else {
            recommendation = "자격 요건이 많이 부족합니다. 불합격을 권장합니다.";
        }
        
        return new ApplicantMatchingScore(
            applicant.getId(),
            jobPosting.getId(),
            totalScore,
            100,
            percentage,
            educationScore,
            majorScore,
            skillScore,
            experienceScore,
            matchedSkills,
            missingSkills,
            recommendation
        );
    }
    
    // === Helper Methods ===
    
    private EducationLevel getHighestEducation(Applicant applicant) {
        return applicant.getEducations().stream()
            .map(Education::getLevel)
            .max(Comparator.comparing(EducationLevel::ordinal))
            .orElse(null);
    }
    
    private String getHighestEducationMajor(Applicant applicant) {
        EducationLevel highest = getHighestEducation(applicant);
        if (highest == null) return null;
        
        return applicant.getEducations().stream()
            .filter(e -> e.getLevel() == highest)
            .findFirst()
            .map(e -> e.getMajor() != null ? e.getMajor().getName() : null)
            .orElse(null);
    }
    
    private boolean checkEducation(EducationLevel applicantLevel, EducationLevel requiredLevel) {
        if (applicantLevel == null || requiredLevel == null) return false;
        return applicantLevel.ordinal() >= requiredLevel.ordinal();
    }
    
    private List<String> parseAcceptedMajors(String acceptedMajorsJson) {
        if (acceptedMajorsJson == null || acceptedMajorsJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(acceptedMajorsJson, listType);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    private boolean checkMajor(String applicantMajor, List<String> acceptedMajors) {
        if (applicantMajor == null || acceptedMajors == null || acceptedMajors.isEmpty()) {
            return false;
        }
        return acceptedMajors.contains(applicantMajor);
    }
    
    private Set<String> getApplicantSkills(Applicant applicant) {
        return applicant.getCareers().stream()
            .flatMap(career -> career.getSkills().stream())
            .map(cs -> cs.getSkill().getName().toLowerCase().trim())
            .collect(Collectors.toSet());
    }
    
    private Set<String> getRequiredSkills(AinjobJobPosting jobPosting) {
        return jobPosting.getRequiredSkills().stream()
            .filter(JobPostingSkill::getIsRequired)
            .map(jps -> jps.getSkill().getName().toLowerCase().trim())
            .collect(Collectors.toSet());
    }
    
    private boolean checkSkills(Set<String> applicantSkills, Set<String> requiredSkills) {
        return applicantSkills.containsAll(requiredSkills);
    }
    
    private double calculateTotalExperience(Applicant applicant) {
        return applicant.getCareers().stream()
            .mapToDouble(career -> {
                LocalDate start = career.getStartDate();
                LocalDate end = career.getEndDate() != null ? career.getEndDate() : LocalDate.now();
                long days = ChronoUnit.DAYS.between(start, end);
                return days / 365.25;
            })
            .sum();
    }
    
    private String buildMatchingReason(boolean education, boolean major, boolean skills, boolean experience) {
        List<String> passed = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        if (education) passed.add("학력"); else failed.add("학력");
        if (major) passed.add("전공"); else failed.add("전공");
        if (skills) passed.add("스킬"); else failed.add("스킬");
        if (experience) passed.add("경력"); else failed.add("경력");
        
        if (failed.isEmpty()) {
            return "모든 자격 요건 충족";
        } else {
            return "충족: " + String.join(", ", passed) + " / 미충족: " + String.join(", ", failed);
        }
    }
}
