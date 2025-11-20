package com.example.payflow.auction.presentation;

import com.example.payflow.auction.application.AuctionService;
import com.example.payflow.auction.application.AutoBidService;
import com.example.payflow.auction.application.BidService;
import com.example.payflow.auction.application.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {
    
    private final AuctionService auctionService;
    private final BidService bidService;
    private final AutoBidService autoBidService;
    
    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            Authentication authentication) {
        String sellerId = authentication.getName();
        AuctionResponse response = auctionService.createAuction(request, sellerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> getAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getActiveAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/active")
    public ResponseEntity<Page<AuctionResponse>> getActiveAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getActiveAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/scheduled")
    public ResponseEntity<Page<AuctionResponse>> getScheduledAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getScheduledAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/ended")
    public ResponseEntity<Page<AuctionResponse>> getEndedAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getEndedAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/popular")
    public ResponseEntity<Page<AuctionResponse>> getPopularAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getPopularAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/ending-soon")
    public ResponseEntity<Page<AuctionResponse>> getEndingSoonAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getEndingSoonAuctions(pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long id) {
        AuctionResponse auction = auctionService.getAuctionWithViewCount(id);
        return ResponseEntity.ok(auction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAuction(
            @PathVariable Long id,
            Authentication authentication) {
        String sellerId = authentication.getName();
        auctionService.cancelAuction(id, sellerId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/end")
    public ResponseEntity<Void> endAuction(@PathVariable Long id) {
        auctionService.endAuction(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/bids")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable Long id,
            @Valid @RequestBody BidRequest request,
            Authentication authentication) {
        String bidderId = authentication.getName();
        BidResponse response = bidService.placeBid(id, request, bidderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/buy-now")
    public ResponseEntity<BidResponse> buyNow(
            @PathVariable Long id,
            Authentication authentication) {
        String buyerId = authentication.getName();
        BidResponse response = bidService.buyNow(id, buyerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/bids")
    public ResponseEntity<Page<BidResponse>> getBids(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponse> bids = bidService.getBidsByAuctionPaged(id, pageable);
        return ResponseEntity.ok(bids);
    }
    
    @GetMapping("/{id}/top-bid")
    public ResponseEntity<BidResponse> getTopBid(@PathVariable Long id) {
        return bidService.getTopBid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/auto-bid")
    public ResponseEntity<AutoBidResponse> createAutoBid(
            @PathVariable Long id,
            @Valid @RequestBody AutoBidRequest request,
            Authentication authentication) {
        String bidderId = authentication.getName();
        AutoBidResponse response = autoBidService.createAutoBid(id, request, bidderId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}/auto-bid")
    public ResponseEntity<Void> cancelAutoBid(
            @PathVariable Long id,
            Authentication authentication) {
        String bidderId = authentication.getName();
        autoBidService.cancelAutoBid(id, bidderId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/auto-bid")
    public ResponseEntity<AutoBidResponse> getAutoBid(
            @PathVariable Long id,
            Authentication authentication) {
        String bidderId = authentication.getName();
        return autoBidService.getAutoBid(id, bidderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/my-selling")
    public ResponseEntity<Page<AuctionResponse>> getMySellingAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String sellerId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getMySellingAuctions(sellerId, pageable);
        return ResponseEntity.ok(auctions);
    }
    
    @GetMapping("/my-winning")
    public ResponseEntity<Page<AuctionResponse>> getMyWinningAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionResponse> auctions = auctionService.getMyWinningAuctions(userId, pageable);
        return ResponseEntity.ok(auctions);
    }
}
