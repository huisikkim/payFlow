package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.EducationLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class ApplicantCreateRequest {
    
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private AddressDto address;
    private List<EducationDto> educations;
    private List<CareerDto> careers;
    
    @Getter
    @NoArgsConstructor
    public static class AddressDto {
        private String city;
        private String district;
        private String detail;
    }
    
    @Getter
    @NoArgsConstructor
    public static class EducationDto {
        private EducationLevel level;
        private String majorName;
        private String schoolName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
    
    @Getter
    @NoArgsConstructor
    public static class CareerDto {
        private String companyName;
        private String position;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<CareerSkillDto> skills;
    }
    
    @Getter
    @NoArgsConstructor
    public static class CareerSkillDto {
        private String skillName;
        private Integer proficiencyLevel;
    }
}
