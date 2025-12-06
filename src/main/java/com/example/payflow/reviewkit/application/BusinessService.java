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
    public Business createBusiness(String name, String slug, Long ownerId, String description, String websiteUrl,
                                   String phoneNumber, String address, String openingHours,
                                   String instagramUrl, String facebookUrl, String youtubeUrl, String mapUrl) {
        if (businessRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug already exists: " + slug);
        }

        Business business = Business.builder()
                .name(name)
                .slug(slug)
                .ownerId(ownerId)
                .description(description)
                .websiteUrl(websiteUrl)
                .phoneNumber(phoneNumber)
                .address(address)
                .openingHours(openingHours)
                .instagramUrl(instagramUrl)
                .facebookUrl(facebookUrl)
                .youtubeUrl(youtubeUrl)
                .mapUrl(mapUrl)
                .build();

        return businessRepository.save(business);
    }
    
    @Transactional
    public Business updateBusiness(Long businessId, String description, String websiteUrl,
                                   String phoneNumber, String address, String openingHours,
                                   String instagramUrl, String facebookUrl, String youtubeUrl, String mapUrl) {
        Business business = getBusinessById(businessId);
        
        // Update fields using reflection or builder pattern
        // For simplicity, we'll need to add setters or use a different approach
        // Since we're using Lombok @Getter only, we need to rebuild
        
        Business updated = Business.builder()
                .id(business.getId())
                .name(business.getName())
                .slug(business.getSlug())
                .ownerId(business.getOwnerId())
                .description(description != null ? description : business.getDescription())
                .websiteUrl(websiteUrl != null ? websiteUrl : business.getWebsiteUrl())
                .phoneNumber(phoneNumber != null ? phoneNumber : business.getPhoneNumber())
                .address(address != null ? address : business.getAddress())
                .openingHours(openingHours != null ? openingHours : business.getOpeningHours())
                .instagramUrl(instagramUrl != null ? instagramUrl : business.getInstagramUrl())
                .facebookUrl(facebookUrl != null ? facebookUrl : business.getFacebookUrl())
                .youtubeUrl(youtubeUrl != null ? youtubeUrl : business.getYoutubeUrl())
                .mapUrl(mapUrl != null ? mapUrl : business.getMapUrl())
                .createdAt(business.getCreatedAt())
                .reviews(business.getReviews())
                .widgets(business.getWidgets())
                .build();
        
        return businessRepository.save(updated);
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
        if (name == null || name.trim().isEmpty()) {
            return "business-" + System.currentTimeMillis();
        }
        
        // Simple slug generation: lowercase, replace spaces with hyphens
        // Support Korean characters (가-힣), English (a-z), numbers (0-9)
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9가-힣\\s-]", "")  // Keep Korean, English, numbers
                .replaceAll("\\s+", "-")                // Replace spaces with hyphens
                .replaceAll("-+", "-")                  // Remove duplicate hyphens
                .replaceAll("^-|-$", "")                // Remove leading/trailing hyphens
                .trim();

        // If slug is empty after processing (e.g., only special characters), use timestamp
        if (baseSlug.isEmpty()) {
            baseSlug = "business-" + System.currentTimeMillis();
        }

        // Ensure uniqueness
        String slug = baseSlug;
        int counter = 1;
        while (businessRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
