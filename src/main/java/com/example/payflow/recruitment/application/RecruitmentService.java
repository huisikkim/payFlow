package com.example.payflow.recruitment.application;

import com.example.payflow.hr.domain.Department;
import com.example.payflow.hr.domain.DepartmentRepository;
import com.example.payflow.hr.domain.Position;
import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {
    
    private final JobPostingRepository jobPostingRepository;
    private final DepartmentRepository departmentRepository;
    private final SkillRepository skillRepository;
    
    @Transactional
    public JobPosting createJobPosting(String title, String description, Long departmentId,
                                      Position position, Integer headcount,
                                      LocalDate startDate, LocalDate endDate) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다: " + departmentId));
        
        JobPosting jobPosting = JobPosting.create(
            title, description, department, position, headcount, startDate, endDate
        );
        
        return jobPostingRepository.save(jobPosting);
    }
    
    @Transactional
    public void addRequirement(Long jobPostingId, Long skillId, RequirementType type,
                              ProficiencyLevel minProficiency, Integer minYearsOfExperience,
                              String description) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobPostingId));
        
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + skillId));
        
        JobRequirement requirement = JobRequirement.create(
            skill, type, minProficiency, minYearsOfExperience, description
        );
        
        jobPosting.addRequirement(requirement);
        jobPostingRepository.save(jobPosting);
    }
    
    @Transactional
    public void publishJobPosting(Long jobPostingId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobPostingId));
        
        jobPosting.publish();
    }
    
    @Transactional
    public void closeJobPosting(Long jobPostingId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + jobPostingId));
        
        jobPosting.close();
    }
    
    public JobPosting getJobPosting(Long id) {
        return jobPostingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + id));
    }
    
    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }
    
    public List<JobPosting> getActiveJobPostings() {
        return jobPostingRepository.findActivePostings(JobPostingStatus.OPEN, LocalDate.now());
    }
    
    public List<JobPosting> getJobPostingsByDepartment(Long departmentId) {
        return jobPostingRepository.findByDepartmentId(departmentId);
    }
    
    public List<JobPosting> getJobPostingsBySkill(Long skillId) {
        return jobPostingRepository.findByRequiredSkill(skillId);
    }
}
