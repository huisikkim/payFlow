package com.example.payflow.ainjob.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    
    Optional<Applicant> findByEmail(String email);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.educations " +
           "WHERE a.id = :id")
    Optional<Applicant> findByIdWithEducations(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.careers " +
           "WHERE a.id = :id")
    Optional<Applicant> findByIdWithCareers(@Param("id") Long id);
}
