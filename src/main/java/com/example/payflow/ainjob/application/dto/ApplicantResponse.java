package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ApplicantResponse {
    
    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDate birthDate;
    private final AddressDto address;
    private final List<EducationDto> educations;
    private final List<CareerDto> careers;
    private final Integer totalYearsOfExperience;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public ApplicantResponse(Applicant applicant) {
        this.id = applicant.getId();
        this.name = applicant.getName();
        this.email = applicant.getEmail();
        this.phone = applicant.getPhone();
        this.birthDate = applicant.getBirthDate();
        this.address = applicant.getAddress() != null ? new AddressDto(applicant.getAddress()) : null;
        this.educations = applicant.getEducations().stream()
            .map(EducationDto::new)
            .collect(Collectors.toList());
        this.careers = applicant.getCareers().stream()
            .map(CareerDto::new)
            .collect(Collectors.toList());
        this.totalYearsOfExperience = applicant.getTotalYearsOfExperience();
        this.createdAt = applicant.getCreatedAt();
        this.updatedAt = applicant.getUpdatedAt();
    }
    
    @Getter
    public static class AddressDto {
        private final String city;
        private final String district;
        private final String detail;
        
        public AddressDto(Address address) {
            this.city = address.getCity();
            this.district = address.getDistrict();
            this.detail = address.getDetail();
        }
    }
    
    @Getter
    public static class EducationDto {
        private final Long id;
        private final EducationLevel level;
        private final String majorName;
        private final String schoolName;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String status;
        
        public EducationDto(Education education) {
            this.id = education.getId();
            this.level = education.getLevel();
            this.majorName = education.getMajor() != null ? education.getMajor().getName() : null;
            this.schoolName = education.getSchoolName();
            this.startDate = education.getStartDate();
            this.endDate = education.getEndDate();
            this.status = education.getStatus();
        }
    }
    
    @Getter
    public static class CareerDto {
        private final Long id;
        private final String companyName;
        private final String position;
        private final String description;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Integer yearsOfExperience;
        private final List<CareerSkillDto> skills;
        
        public CareerDto(Career career) {
            this.id = career.getId();
            this.companyName = career.getCompanyName();
            this.position = career.getPosition();
            this.description = career.getDescription();
            this.startDate = career.getStartDate();
            this.endDate = career.getEndDate();
            this.yearsOfExperience = career.getYearsOfExperience();
            this.skills = career.getSkills().stream()
                .map(CareerSkillDto::new)
                .collect(Collectors.toList());
        }
    }
    
    @Getter
    public static class CareerSkillDto {
        private final Long id;
        private final String skillName;
        private final Integer proficiencyLevel;
        
        public CareerSkillDto(CareerSkill careerSkill) {
            this.id = careerSkill.getId();
            this.skillName = careerSkill.getSkill().getName();
            this.proficiencyLevel = careerSkill.getProficiencyLevel();
        }
    }
}
