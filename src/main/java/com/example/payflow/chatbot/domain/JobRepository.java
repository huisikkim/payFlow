package com.example.payflow.chatbot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByIsActiveTrue();

    List<Job> findByRegionAndIsActiveTrue(String region);

    List<Job> findByIndustryAndIsActiveTrue(String industry);

    @Query("SELECT DISTINCT j.region FROM Job j WHERE j.isActive = true ORDER BY j.region")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT j.industry FROM Job j WHERE j.isActive = true ORDER BY j.industry")
    List<String> findDistinctIndustries();

    @Query("SELECT j FROM Job j WHERE j.isActive = true " +
           "AND (:region IS NULL OR j.region = :region) " +
           "AND (:industry IS NULL OR j.industry = :industry) " +
           "AND (:minSalary IS NULL OR j.maxSalary >= :minSalary) " +
           "AND (:maxSalary IS NULL OR j.minSalary <= :maxSalary)")
    List<Job> searchJobs(@Param("region") String region,
                         @Param("industry") String industry,
                         @Param("minSalary") Long minSalary,
                         @Param("maxSalary") Long maxSalary);
}
