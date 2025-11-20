package com.example.payflow.auction.presentation;

import com.example.payflow.auction.application.AuctionService;
import com.example.payflow.auction.application.BidService;
import com.example.payflow.auction.application.dto.AuctionResponse;
import com.example.payflow.auction.application.dto.BidResponse;
import com.example.payflow.product.application.ProductService;
import com.example.payflow.product.application.dto.ProductDetailResponse;
import com.example.payflow.product.application.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pickswap/auctions")
@RequiredArgsConstructor
public class AuctionWebController {
    
    private final AuctionService auctionService;
    private final BidService bidService;
    private final ProductService productService;
    
    @GetMapping
    public String auctionList(
            @RequestParam(defaultValue = "active") String tab,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<AuctionResponse> auctions;
        
        switch (tab) {
            case "scheduled":
                auctions = auctionService.getScheduledAuctions(pageable);
                break;
            case "ended":
                auctions = auctionService.getEndedAuctions(pageable);
                break;
            case "popular":
                auctions = auctionService.getPopularAuctions(pageable);
                break;
            case "ending-soon":
                auctions = auctionService.getEndingSoonAuctions(pageable);
                break;
            default:
                auctions = auctionService.getActiveAuctions(pageable);
        }
        
        model.addAttribute("auctions", auctions);
        model.addAttribute("currentTab", tab);
        return "auction/auction-list";
    }
    
    @GetMapping("/create")
    public String auctionCreateForm(
            @RequestParam(required = false) Long productId,
            Model model) {
        // 웹 페이지는 누구나 접근 가능, JavaScript에서 JWT 토큰으로 로그인 체크
        model.addAttribute("selectedProductId", productId);
        return "auction/auction-create";
    }
    
    @GetMapping("/my")
    public String myAuctions(
            @RequestParam(defaultValue = "selling") String tab,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<AuctionResponse> auctions;
        
        if (authentication != null) {
            String userId = authentication.getName();
            if ("winning".equals(tab)) {
                auctions = auctionService.getMyWinningAuctions(userId, pageable);
            } else {
                auctions = auctionService.getMySellingAuctions(userId, pageable);
            }
        } else {
            // 로그인하지 않은 경우 빈 페이지 반환
            auctions = Page.empty(pageable);
        }
        
        model.addAttribute("auctions", auctions);
        model.addAttribute("currentTab", tab);
        return "auction/my-auctions";
    }
    
    @GetMapping("/{id}")
    public String auctionDetail(@PathVariable Long id, Model model, Authentication authentication) {
        AuctionResponse auction = auctionService.getAuctionWithViewCount(id);
        List<BidResponse> bids = bidService.getBidsByAuction(id);
        
        // 상품 정보 조회
        ProductDetailResponse product = productService.getProductDetail(auction.getProductId());
        
        model.addAttribute("auction", auction);
        model.addAttribute("product", product);
        model.addAttribute("bids", bids);
        
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("isOwner", auction.getSellerId().equals(username));
            model.addAttribute("currentUser", username);
        } else {
            model.addAttribute("isOwner", false);
            model.addAttribute("currentUser", null);
        }
        
        return "auction/auction-detail";
    }
}
