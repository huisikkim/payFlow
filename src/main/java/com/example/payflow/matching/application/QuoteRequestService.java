package com.example.payflow.matching.application;

import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
import com.example.payflow.matching.domain.QuoteRequest;
import com.example.payflow.matching.domain.QuoteRequestRepository;
import com.example.payflow.store.domain.Store;
import com.example.payflow.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteRequestService {
    
    private final QuoteRequestRepository quoteRequestRepository;
    private final StoreRepository storeRepository;
    private final DistributorRepository distributorRepository;
    
    /**
     * 견적 요청 생성
     */
    @Transactional
    public QuoteRequest createQuoteRequest(String storeId, String distributorId, 
                                          String requestedProducts, String message) {
        // 매장 정보 조회
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 유통업체 정보 조회
        Distributor distributor = distributorRepository.findByDistributorId(distributorId)
                .orElseThrow(() -> new IllegalArgumentException("유통업체를 찾을 수 없습니다: " + distributorId));
        
        // 유통업체가 활성화 상태인지 확인
        if (distributor.getIsActive() == null || !distributor.getIsActive()) {
            throw new IllegalStateException("해당 유통업체는 현재 서비스를 제공하지 않습니다");
        }
        
        // 견적 요청 생성
        QuoteRequest quoteRequest = QuoteRequest.builder()
                .storeId(storeId)
                .storeName(store.getStoreName())
                .distributorId(distributorId)
                .distributorName(distributor.getDistributorName())
                .requestedProducts(requestedProducts)
                .message(message)
                .status(QuoteRequest.QuoteStatus.PENDING)
                .build();
        
        QuoteRequest saved = quoteRequestRepository.save(quoteRequest);
        
        log.info("✅ 견적 요청 생성 완료 - 매장: {}, 유통업체: {}, ID: {}", 
                storeId, distributorId, saved.getId());
        
        return saved;
    }
    
    /**
     * 매장의 견적 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<QuoteRequest> getStoreQuoteRequests(String storeId) {
        return quoteRequestRepository.findByStoreIdOrderByRequestedAtDesc(storeId);
    }
    
    /**
     * 유통업체의 견적 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<QuoteRequest> getDistributorQuoteRequests(String distributorId) {
        return quoteRequestRepository.findByDistributorIdOrderByRequestedAtDesc(distributorId);
    }
    
    /**
     * 견적 요청 상세 조회
     */
    @Transactional(readOnly = true)
    public QuoteRequest getQuoteRequest(Long id) {
        return quoteRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + id));
    }
    
    /**
     * 견적 요청 응답 (유통업체)
     */
    @Transactional
    public QuoteRequest respondToQuoteRequest(String distributorId, Long requestId, 
                                             QuoteRequest.QuoteStatus status,
                                             Integer estimatedAmount, 
                                             String response) {
        QuoteRequest quoteRequest = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + requestId));
        
        // 권한 확인
        if (!quoteRequest.getDistributorId().equals(distributorId)) {
            throw new IllegalStateException("해당 견적 요청에 대한 권한이 없습니다");
        }
        
        // 이미 응답한 경우
        if (quoteRequest.getStatus() != QuoteRequest.QuoteStatus.PENDING) {
            throw new IllegalStateException("이미 응답한 견적 요청입니다");
        }
        
        // 응답 업데이트
        quoteRequest.setStatus(status);
        quoteRequest.setEstimatedAmount(estimatedAmount);
        quoteRequest.setDistributorResponse(response);
        quoteRequest.setRespondedAt(LocalDateTime.now());
        
        QuoteRequest updated = quoteRequestRepository.save(quoteRequest);
        
        log.info("✅ 견적 요청 응답 완료 - ID: {}, 상태: {}", requestId, status);
        
        return updated;
    }
    
    /**
     * 견적 요청 취소 (매장)
     */
    @Transactional
    public void cancelQuoteRequest(String storeId, Long requestId) {
        QuoteRequest quoteRequest = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + requestId));
        
        // 권한 확인
        if (!quoteRequest.getStoreId().equals(storeId)) {
            throw new IllegalStateException("해당 견적 요청에 대한 권한이 없습니다");
        }
        
        // 대기중인 요청만 취소 가능
        if (quoteRequest.getStatus() != QuoteRequest.QuoteStatus.PENDING) {
            throw new IllegalStateException("대기중인 견적 요청만 취소할 수 있습니다");
        }
        
        quoteRequestRepository.delete(quoteRequest);
        
        log.info("✅ 견적 요청 취소 완료 - ID: {}", requestId);
    }
    
    /**
     * 견적 완료 처리 (매장)
     */
    @Transactional
    public QuoteRequest completeQuoteRequest(String storeId, Long requestId) {
        QuoteRequest quoteRequest = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + requestId));
        
        // 권한 확인
        if (!quoteRequest.getStoreId().equals(storeId)) {
            throw new IllegalStateException("해당 견적 요청에 대한 권한이 없습니다");
        }
        
        // 수락된 요청만 완료 가능
        if (quoteRequest.getStatus() != QuoteRequest.QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("수락된 견적 요청만 완료할 수 있습니다");
        }
        
        quoteRequest.setStatus(QuoteRequest.QuoteStatus.COMPLETED);
        
        QuoteRequest updated = quoteRequestRepository.save(quoteRequest);
        
        log.info("✅ 견적 완료 처리 - ID: {}", requestId);
        
        return updated;
    }
}
