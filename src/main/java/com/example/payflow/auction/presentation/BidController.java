package com.example.payflow.auction.presentation;

import com.example.payflow.auction.application.BidService;
import com.example.payflow.auction.application.dto.BidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    
    private final BidService bidService;
    
    @GetMapping("/my")
    public ResponseEntity<Page<BidResponse>> getMyBids(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String bidderId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponse> bids = bidService.getMyBids(bidderId, pageable);
        return ResponseEntity.ok(bids);
    }
}
