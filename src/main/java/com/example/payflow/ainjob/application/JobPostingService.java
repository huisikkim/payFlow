package com.example.payflow.ainjob.application;

import com.example.payflow.ainjob.application.dto.JobPostingCreateRequest;
import com.example.payflow.ainjob.application.dto.JobPostingResponse;
import com.example.payflow.ainjob.domain.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {
    
    private final AinjobJobPostingRepository jobPostingRepository;
    private final AinjobSkillRepository skillRepository;
    private final Gson gson = new Gson();
    
    @Transactional
    public JobPostingResponse createJobPosting(JobPostingCreateRequest request) {
        // Qualification 생성
        String acceptedMajorsJson = gson.toJson(request.getQualification().getAcceptedMajors());
        Qualification qualification = new Qualification(
            request.getQualification().getMinEducationLevel(),
            acceptedMajorsJson,
            request.getQualification().getMinYearsOfExperience()
        );
        
        // JobPosting 생성
        AinjobJobPosting jobPosting = AinjobJobPosting.create(
            request.getCompanyId(),
            request.getTitle(),
            request.getDescription(),
            request.getPosition(),
            qualification,
            request.getOpenDate(),
            request.getCloseDate()
        );
        
        // Required Skills 추가
        if (request.getRequiredSkills() != null) {
            for (JobPostingCreateRequest.RequiredSkillDto skillDto : request.getRequiredSkills()) {
                AinjobSkill skill = skillRepository.findByName(skillDto.getSkillName())
                    .orElseGet(() -> skillRepository.save(
                        AinjobSkill.create(skillDto.getSkillName(), "기타", null)
                    ));
                
                JobPostingSkill jobPostingSkill = JobPostingSkill.create(
                    skill,
                    skillDto.getIsRequired(),
                    skillDto.getMinProficiency()
                );
                jobPosting.addRequiredSkill(jobPostingSkill);
            }
        }
        
        AinjobJobPosting saved = jobPostingRepository.save(jobPosting);
        return new JobPostingResponse(saved);
    }
    
    public JobPostingResponse getJobPosting(Long id) {
        AinjobJobPosting jobPosting = jobPostingRepository.findByIdWithSkills(id)
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + id));
        return new JobPostingResponse(jobPosting);
    }
    
    public List<JobPostingResponse> getAllJobPostings() {
        return jobPostingRepository.findAll().stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
    }
    
    public List<JobPostingResponse> getJobPostingsByCompany(Long companyId) {
        return jobPostingRepository.findByCompanyId(companyId).stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
    }
    
    public List<JobPostingResponse> getJobPostingsByStatus(JobPostingStatus status) {
        return jobPostingRepository.findByStatus(status).stream()
            .map(JobPostingResponse::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void openJobPosting(Long id) {
        AinjobJobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + id));
        jobPosting.open();
    }
    
    @Transactional
    public void closeJobPosting(Long id) {
        AinjobJobPosting jobPosting = jobPostingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + id));
        jobPosting.close();
    }
}
