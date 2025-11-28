package com.example.payflow.catalog.application;

import com.example.payflow.catalog.domain.*;
import com.example.payflow.catalog.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final DeliveryInfoRepository deliveryRepository;
    private final DistributorOrderRepository orderRepository;
    
    /**
     * 배송 정보 생성 (주문 확정 시 자동 생성)
     */
    @Transactional
    public DeliveryResponse createDeliveryInfo(Long orderId) {
        DistributorOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("확정된 주문만 배송 정보를 생성할 수 있습니다.");
        }
        
        // 이미 배송 정보가 있는지 확인
        if (deliveryRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalArgumentException("이미 배송 정보가 존재합니다.");
        }
        
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .order(order)
                .status(DeliveryStatus.PREPARING)
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryPhone(order.getDeliveryPhone())
                .deliveryRequest(order.getDeliveryRequest())
                .preparedAt(LocalDateTime.now())
                .build();
        
        DeliveryInfo saved = deliveryRepository.save(deliveryInfo);
        
        // 주문 상태 업데이트
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);
        
        log.info("배송 정보 생성: 주문번호={}", order.getOrderNumber());
        
        return toDeliveryResponse(saved);
    }
    
    /**
     * 배송 시작 (유통업자)
     */
    @Transactional
    public DeliveryResponse startShipping(Long orderId, StartShippingRequest request) {
        DeliveryInfo deliveryInfo = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        
        DistributorOrder order = deliveryInfo.getOrder();
        
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalArgumentException("상품 준비 중인 주문만 배송을 시작할 수 있습니다.");
        }
        
        // 배송 방식에 따라 처리
        if (request.getDeliveryType() == DeliveryType.COURIER) {
            // 택배 배송
            if (request.getTrackingNumber() == null || request.getCourierCompany() == null) {
                throw new IllegalArgumentException("택배 배송 시 송장번호와 배송사는 필수입니다.");
            }
            
            deliveryInfo.startCourierShipping(
                    request.getTrackingNumber(),
                    request.getCourierCompany(),
                    request.getEstimatedDeliveryDate()
            );
            
            if (request.getCourierPhone() != null) {
                deliveryInfo.setCourierPhone(request.getCourierPhone());
            }
            
            log.info("택배 배송 시작: 주문번호={}, 송장번호={}, 배송사={}", 
                    order.getOrderNumber(), request.getTrackingNumber(), request.getCourierCompany());
            
        } else if (request.getDeliveryType() == DeliveryType.DIRECT) {
            // 직접 배송
            if (request.getDriverName() == null || request.getDriverPhone() == null) {
                throw new IllegalArgumentException("직접 배송 시 배송 기사 이름과 연락처는 필수입니다.");
            }
            
            deliveryInfo.startDirectShipping(
                    request.getDriverName(),
                    request.getDriverPhone(),
                    request.getVehicleNumber(),
                    request.getEstimatedDeliveryDate()
            );
            
            log.info("직접 배송 시작: 주문번호={}, 기사={}, 연락처={}, 차량번호={}", 
                    order.getOrderNumber(), request.getDriverName(), request.getDriverPhone(), request.getVehicleNumber());
            
        } else {
            throw new IllegalArgumentException("배송 방식을 선택해주세요. (DIRECT: 직접배송, COURIER: 택배배송)");
        }
        
        if (request.getDeliveryNotes() != null) {
            deliveryInfo.setDeliveryNotes(request.getDeliveryNotes());
        }
        
        DeliveryInfo saved = deliveryRepository.save(deliveryInfo);
        
        // 주문 상태 업데이트
        order.ship();
        orderRepository.save(order);
        
        return toDeliveryResponse(saved);
    }
    
    /**
     * 배송 완료 (유통업자 또는 자동)
     */
    @Transactional
    public DeliveryResponse completeDelivery(Long orderId) {
        DeliveryInfo deliveryInfo = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        
        DistributorOrder order = deliveryInfo.getOrder();
        
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("배송 중인 주문만 배송 완료 처리할 수 있습니다.");
        }
        
        // 배송 완료
        deliveryInfo.completeDelivery();
        DeliveryInfo saved = deliveryRepository.save(deliveryInfo);
        
        // 주문 상태 업데이트
        order.deliver();
        orderRepository.save(order);
        
        log.info("배송 완료: 주문번호={}", order.getOrderNumber());
        
        return toDeliveryResponse(saved);
    }
    
    /**
     * 배송 정보 조회 (주문 ID로)
     */
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        DeliveryInfo deliveryInfo = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        
        return toDeliveryResponse(deliveryInfo);
    }
    
    /**
     * 유통업자별 배송 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByDistributor(String distributorId) {
        List<DeliveryInfo> deliveries = deliveryRepository
                .findByOrder_DistributorIdOrderByCreatedAtDesc(distributorId);
        
        return deliveries.stream()
                .map(this::toDeliveryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 매장별 배송 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByStore(String storeId) {
        List<DeliveryInfo> deliveries = deliveryRepository
                .findByOrder_StoreIdOrderByCreatedAtDesc(storeId);
        
        return deliveries.stream()
                .map(this::toDeliveryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 상태별 배송 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status) {
        List<DeliveryInfo> deliveries = deliveryRepository.findByStatus(status);
        
        return deliveries.stream()
                .map(this::toDeliveryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Entity -> Response 변환
     */
    private DeliveryResponse toDeliveryResponse(DeliveryInfo deliveryInfo) {
        DistributorOrder order = deliveryInfo.getOrder();
        
        return DeliveryResponse.builder()
                .id(deliveryInfo.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeId(order.getStoreId())
                .distributorId(order.getDistributorId())
                .deliveryType(deliveryInfo.getDeliveryType())
                .deliveryTypeDescription(deliveryInfo.getDeliveryType() != null ? 
                        deliveryInfo.getDeliveryType().getDescription() : null)
                .trackingNumber(deliveryInfo.getTrackingNumber())
                .courierCompany(deliveryInfo.getCourierCompany())
                .courierPhone(deliveryInfo.getCourierPhone())
                .driverName(deliveryInfo.getDriverName())
                .driverPhone(deliveryInfo.getDriverPhone())
                .vehicleNumber(deliveryInfo.getVehicleNumber())
                .status(deliveryInfo.getStatus())
                .statusDescription(deliveryInfo.getStatus().getDescription())
                .preparedAt(deliveryInfo.getPreparedAt())
                .shippedAt(deliveryInfo.getShippedAt())
                .deliveredAt(deliveryInfo.getDeliveredAt())
                .estimatedDeliveryDate(deliveryInfo.getEstimatedDeliveryDate())
                .deliveryAddress(deliveryInfo.getDeliveryAddress())
                .deliveryPhone(deliveryInfo.getDeliveryPhone())
                .deliveryRequest(deliveryInfo.getDeliveryRequest())
                .deliveryNotes(deliveryInfo.getDeliveryNotes())
                .totalAmount(order.getTotalAmount())
                .createdAt(deliveryInfo.getCreatedAt())
                .build();
    }
}
