package com.example.payflow.reviewkit.presentation;

import com.example.payflow.reviewkit.application.BusinessService;
import com.example.payflow.reviewkit.application.ReviewService;
import com.example.payflow.reviewkit.application.WidgetService;
import com.example.payflow.reviewkit.application.dto.BusinessCreateRequest;
import com.example.payflow.reviewkit.application.dto.WidgetCreateRequest;
import com.example.payflow.reviewkit.domain.Business;
import com.example.payflow.reviewkit.domain.Review;
import com.example.payflow.reviewkit.domain.Widget;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reviewkit")
@RequiredArgsConstructor
public class ReviewKitController {

    private final BusinessService businessService;
    private final ReviewService reviewService;
    private final WidgetService widgetService;

    /**
     * Landing page
     */
    @GetMapping
    public String landing() {
        return "reviewkit/landing";
    }

    /**
     * Dashboard - List businesses
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long userId = getUserId(auth);
        List<Business> businesses = businessService.getBusinessesByOwner(userId);
        model.addAttribute("businesses", businesses);
        return "reviewkit/dashboard";
    }

    /**
     * Create business form
     */
    @GetMapping("/businesses/new")
    public String newBusinessForm(Model model) {
        model.addAttribute("request", new BusinessCreateRequest());
        return "reviewkit/business-form";
    }

    /**
     * Create business
     */
    @PostMapping("/businesses")
    public String createBusiness(@Valid @ModelAttribute BusinessCreateRequest request,
                                 Authentication auth) {
        Long userId = getUserId(auth);
        String slug = businessService.generateSlug(request.getName());
        
        businessService.createBusiness(
                request.getName(),
                slug,
                userId,
                request.getDescription(),
                request.getWebsiteUrl()
        );

        return "redirect:/reviewkit/dashboard";
    }

    /**
     * Business detail - Review management
     */
    @GetMapping("/businesses/{businessId}")
    public String businessDetail(@PathVariable Long businessId, Authentication auth, Model model) {
        Long userId = getUserId(auth);
        
        if (!businessService.isOwner(businessId, userId)) {
            return "redirect:/reviewkit/dashboard";
        }

        Business business = businessService.getBusinessById(businessId);
        List<Review> pendingReviews = reviewService.getPendingReviews(businessId);
        List<Review> approvedReviews = reviewService.getApprovedReviews(businessId);
        List<Widget> widgets = widgetService.getWidgetsByBusiness(businessId);

        model.addAttribute("business", business);
        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("approvedReviews", approvedReviews);
        model.addAttribute("widgets", widgets);
        model.addAttribute("pendingCount", pendingReviews.size());
        model.addAttribute("approvedCount", approvedReviews.size());

        return "reviewkit/business-detail";
    }

    /**
     * Widget creation form
     */
    @GetMapping("/businesses/{businessId}/widgets/new")
    public String newWidgetForm(@PathVariable Long businessId, Model model) {
        model.addAttribute("businessId", businessId);
        model.addAttribute("request", new WidgetCreateRequest());
        return "reviewkit/widget-form";
    }

    /**
     * Create widget
     */
    @PostMapping("/businesses/{businessId}/widgets")
    public String createWidget(@PathVariable Long businessId,
                               @Valid @ModelAttribute WidgetCreateRequest request) {
        widgetService.createWidget(
                businessId,
                request.getName(),
                request.getTheme(),
                request.getLayout(),
                request.getDisplayLimit(),
                request.getLanguage()
        );

        return "redirect:/reviewkit/businesses/" + businessId;
    }

    /**
     * Public review submission form
     */
    @GetMapping("/r/{slug}")
    public String reviewForm(@PathVariable String slug, Model model) {
        Business business = businessService.getBusinessBySlug(slug);
        model.addAttribute("business", business);
        return "reviewkit/review-form";
    }

    /**
     * Widget demo page
     */
    @GetMapping("/widget-demo")
    public String widgetDemo() {
        return "reviewkit/widget-demo";
    }

    /**
     * QR Code generator
     */
    @GetMapping("/businesses/{businessId}/qr-code")
    public String qrCode(@PathVariable Long businessId, Authentication auth, Model model) {
        Long userId = getUserId(auth);
        
        if (!businessService.isOwner(businessId, userId)) {
            return "redirect:/reviewkit/dashboard";
        }

        Business business = businessService.getBusinessById(businessId);
        String reviewUrl = "http://localhost:8080" + business.getPublicReviewUrl();
        
        model.addAttribute("business", business);
        model.addAttribute("reviewUrl", reviewUrl);
        
        return "reviewkit/qr-code";
    }

    private Long getUserId(Authentication auth) {
        // TODO: Extract user ID from authentication
        // For now, return a dummy value
        return 1L;
    }
}
