package com.example.payflow.reviewkit.presentation;

import com.example.payflow.reviewkit.application.ReviewService;
import com.example.payflow.reviewkit.application.WidgetService;
import com.example.payflow.reviewkit.application.dto.ReviewResponse;
import com.example.payflow.reviewkit.domain.Review;
import com.example.payflow.reviewkit.domain.Widget;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/widgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow embedding from any domain
public class WidgetApiController {

    private final WidgetService widgetService;
    private final ReviewService reviewService;

    /**
     * Public API: Get approved reviews for a widget
     * This endpoint is called by the widget.js SDK
     */
    @GetMapping("/{widgetId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getWidgetReviews(
            @PathVariable String widgetId,
            @RequestParam(required = false, defaultValue = "6") Integer limit) {
        
        Widget widget = widgetService.getWidgetByWidgetId(widgetId);
        List<Review> reviews = reviewService.getApprovedReviews(widget.getBusiness().getId());

        // Apply limit
        int actualLimit = Math.min(limit, widget.getDisplayLimit());
        List<ReviewResponse> response = reviews.stream()
                .limit(actualLimit)
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
