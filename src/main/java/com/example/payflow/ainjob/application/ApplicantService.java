package com.example.payflow.ainjob.application;

import com.example.payflow.ainjob.application.dto.ApplicantCreateRequest;
import com.example.payflow.ainjob.application.dto.ApplicantResponse;
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
public class ApplicantService {
    
    private final ApplicantRepository applicantRepository;
    private final MajorRepository majorRepository;
    private final AinjobSkillRepository skillRepository;
    
    @Transactional
    public ApplicantResponse createApplicant(ApplicantCreateRequest request) {
        // 이메일 중복 체크
        applicantRepository.findByEmail(request.getEmail()).ifPresent(a -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + request.getEmail());
        });
        
        // Address 생성
        Address address = null;
        if (request.getAddress() != null) {
            address = new Address(
                request.getAddress().getCity(),
                request.getAddress().getDistrict(),
                request.getAddress().getDetail()
            );
        }
        
        // Applicant 생성
        Applicant applicant = Applicant.create(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getBirthDate(),
            address
        );
        
        // Education 추가
        if (request.getEducations() != null) {
            for (ApplicantCreateRequest.EducationDto eduDto : request.getEducations()) {
                Major major = majorRepository.findByName(eduDto.getMajorName())
                    .orElseGet(() -> majorRepository.save(Major.create(eduDto.getMajorName(), "기타")));
                
                Education education = Education.create(
                    eduDto.getLevel(),
                    major,
                    eduDto.getSchoolName(),
                    eduDto.getStartDate(),
                    eduDto.getEndDate(),
                    eduDto.getStatus()
                );
                applicant.addEducation(education);
            }
        }
        
        // Career 추가
        if (request.getCareers() != null) {
            for (ApplicantCreateRequest.CareerDto careerDto : request.getCareers()) {
                Career career = Career.create(
                    careerDto.getCompanyName(),
                    careerDto.getPosition(),
                    careerDto.getDescription(),
                    careerDto.getStartDate(),
                    careerDto.getEndDate()
                );
                
                // Career Skill 추가
                if (careerDto.getSkills() != null) {
                    for (ApplicantCreateRequest.CareerSkillDto skillDto : careerDto.getSkills()) {
                        AinjobSkill skill = skillRepository.findByName(skillDto.getSkillName())
                            .orElseGet(() -> skillRepository.save(
                                AinjobSkill.create(skillDto.getSkillName(), "기타", null)
                            ));
                        
                        CareerSkill careerSkill = CareerSkill.create(skill, skillDto.getProficiencyLevel());
                        career.addSkill(careerSkill);
                    }
                }
                
                applicant.addCareer(career);
            }
        }
        
        Applicant saved = applicantRepository.save(applicant);
        return new ApplicantResponse(saved);
    }
    
    public ApplicantResponse getApplicant(Long id) {
        // 먼저 educations를 fetch
        Applicant applicant = applicantRepository.findByIdWithEducations(id)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + id));
        
        // 그 다음 careers를 fetch (이미 영속성 컨텍스트에 있으므로 같은 엔티티)
        applicantRepository.findByIdWithCareers(id);
        
        return new ApplicantResponse(applicant);
    }
    
    public List<ApplicantResponse> getAllApplicants() {
        return applicantRepository.findAll().stream()
            .map(ApplicantResponse::new)
            .collect(Collectors.toList());
    }
    
    public ApplicantResponse getApplicantByEmail(String email) {
        Applicant applicant = applicantRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + email));
        return new ApplicantResponse(applicant);
    }
}
