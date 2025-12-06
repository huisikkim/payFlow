package com.example.payflow.reviewkit.application;

import com.example.payflow.reviewkit.domain.Business;
import com.example.payflow.reviewkit.infrastructure.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("reviewKitBusinessService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional
    public Business createBusiness(String name, String slug, Long ownerId, String description, String websiteUrl) {
        if (businessRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug already exists: " + slug);
        }

        Business business = Business.builder()
                .name(name)
                .slug(slug)
                .ownerId(ownerId)
                .description(description)
                .websiteUrl(websiteUrl)
                .build();

        return businessRepository.save(business);
    }

    public Business getBusinessById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + id));
    }

    public Business getBusinessBySlug(String slug) {
        return businessRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + slug));
    }

    public List<Business> getBusinessesByOwner(Long ownerId) {
        return businessRepository.findByOwnerId(ownerId);
    }

    public boolean isOwner(Long businessId, Long userId) {
        Business business = getBusinessById(businessId);
        return business.getOwnerId().equals(userId);
    }

    public String generateSlug(String name) {
        // Simple slug generation: lowercase, replace spaces with hyphens
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Ensure uniqueness
        String slug = baseSlug;
        int counter = 1;
        while (businessRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
