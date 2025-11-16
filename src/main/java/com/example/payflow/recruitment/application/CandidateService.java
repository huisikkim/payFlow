package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    private final SkillRepository skillRepository;
    
    @Transactional
    public Candidate createCandidate(String name, String email, String phone,
                                    EducationLevel education, String university, String major) {
        // 이메일 중복 체크
        candidateRepository.findByEmail(email).ifPresent(c -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        });
        
        Candidate candidate = Candidate.create(name, email, phone, education, university, major);
        return candidateRepository.save(candidate);
    }
    
    @Transactional
    public void addSkillToCandidate(Long candidateId, Long skillId,
                                   ProficiencyLevel proficiencyLevel,
                                   Integer yearsOfExperience, String description) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + skillId));
        
        CandidateSkill candidateSkill = CandidateSkill.create(
            skill, proficiencyLevel, yearsOfExperience, description
        );
        
        candidate.addSkill(candidateSkill);
        candidateRepository.save(candidate);
    }
    
    @Transactional
    public void addExperienceToCandidate(Long candidateId, String company, String position,
                                        LocalDate startDate, LocalDate endDate,
                                        boolean currentlyWorking,
                                        String description, String achievements) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        WorkExperience experience = WorkExperience.create(
            company, position, startDate, endDate, currentlyWorking, description, achievements
        );
        
        candidate.addExperience(experience);
        candidateRepository.save(candidate);
    }
    
    @Transactional
    public void updateCandidate(Long candidateId, String name, String phone, String address,
                               LocalDate birthDate, String summary, String resumeUrl) {
        Candidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + candidateId));
        
        candidate.update(name, phone, address, birthDate, summary, resumeUrl);
    }
    
    public Candidate getCandidate(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + id));
    }
    
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }
    
    public List<Candidate> getCandidatesBySkill(Long skillId) {
        return candidateRepository.findBySkill(skillId);
    }
    
    public Candidate getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + email));
    }
}
