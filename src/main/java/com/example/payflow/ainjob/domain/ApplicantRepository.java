package com.example.payflow.ainjob.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    
    Optional<Applicant> findByEmail(String email);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.educations e " +
           "LEFT JOIN FETCH e.major " +
           "WHERE a.id = :id")
    Optional<Applicant> findByIdWithEducations(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.educations e " +
           "LEFT JOIN FETCH e.major " +
           "WHERE a.id IN :ids")
    java.util.List<Applicant> findByIdInWithEducations(@Param("ids") java.util.List<Long> ids);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.careers " +
           "WHERE a.id = :id")
    Optional<Applicant> findByIdWithCareers(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Applicant a " +
           "LEFT JOIN FETCH a.careers " +
           "WHERE a.id IN :ids")
    java.util.List<Applicant> findByIdInWithCareers(@Param("ids") java.util.List<Long> ids);
    
    default Optional<Applicant> findByIdWithDetails(Long id) {
        Optional<Applicant> applicant = findByIdWithEducations(id);
        if (applicant.isPresent()) {
            findByIdWithCareers(id);
        }
        return applicant;
    }
    
    default java.util.List<Applicant> findByIdInWithDetails(java.util.List<Long> ids) {
        java.util.List<Applicant> applicants = findByIdInWithEducations(ids);
        if (!applicants.isEmpty()) {
            findByIdInWithCareers(ids);
        }
        return applicants;
    }
}
