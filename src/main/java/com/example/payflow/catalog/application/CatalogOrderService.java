package com.example.payflow.catalog.application;

import com.example.payflow.catalog.domain.*;
import com.example.payflow.catalog.presentation.dto.*;
import com.example.payflow.payment.application.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogOrderService {
    
    private final DistributorOrderRepository orderRepository;
    private final OrderCartRepository cartRepository;
    private final ProductCatalogRepository productRepository;
    private final PaymentService paymentService;
    
    /**
     * 장바구니에서 주문 생성
     */
    @Transactional
    public OrderResponse createOrderFromCart(String storeId, CreateOrderRequest request) {
        // 1. 장바구니 조회
        OrderCart cart = cartRepository.findByStoreIdAndDistributorId(storeId, request.getDistributorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("장바구니가 비어있습니다. (매장ID: %s, 유통업체ID: %s)", storeId, request.getDistributorId())));
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("장바구니에 상품이 없습니다. (매장ID: %s, 유통업체ID: %s)", storeId, request.getDistributorId()));
        }
        
        // 2. 재고 확인 및 차감
        for (OrderCartItem cartItem : cart.getItems()) {
            ProductCatalog product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + cartItem.getProductId()));
            
            if (!product.getIsAvailable()) {
                throw new IllegalArgumentException("판매 중단된 상품입니다: " + product.getProductName());
            }
            
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("재고가 부족합니다: " + product.getProductName() + 
                        " (재고: " + product.getStockQuantity() + ", 주문: " + cartItem.getQuantity() + ")");
            }
            
            // 재고 차감
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // 3. 주문 생성
        DistributorOrder order = DistributorOrder.builder()
                .storeId(storeId)
                .distributorId(request.getDistributorId())
                .orderNumber(generateOrderNumber())
                .totalAmount(cart.getTotalAmount())
                .totalQuantity(cart.getTotalQuantity())
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryPhone(request.getDeliveryPhone())
                .deliveryRequest(request.getDeliveryRequest())
                .desiredDeliveryDate(request.getDesiredDeliveryDate())
                .orderedAt(LocalDateTime.now())
                .build();
        
        // 4. 주문 아이템 추가
        for (OrderCartItem cartItem : cart.getItems()) {
            DistributorOrderItem orderItem = DistributorOrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .unitPrice(cartItem.getUnitPrice())
                    .unit(cartItem.getUnit())
                    .quantity(cartItem.getQuantity())
                    .subtotal(cartItem.getSubtotal())
                    .imageUrl(cartItem.getImageUrl())
                    .build();
            
            order.addItem(orderItem);
        }
        
        DistributorOrder savedOrder = orderRepository.save(order);
        
        // 5. 결제 정보 생성 (토스페이먼츠 결제를 위해)
        String orderName = generateOrderName(savedOrder);
        paymentService.createPayment(
            savedOrder.getOrderNumber(),  // orderId로 주문번호 사용
            orderName,
            savedOrder.getTotalAmount(),
            storeId + "@store.com"  // 임시 이메일 (실제로는 매장 정보에서 가져와야 함)
        );
        
        // 6. 장바구니 비우기
        cartRepository.delete(cart);
        
        log.info("주문 생성 완료: {} (매장: {}, 유통업체: {})", 
                savedOrder.getOrderNumber(), storeId, request.getDistributorId());
        
        return toOrderResponse(savedOrder);
    }
    
    /**
     * 주문 목록 조회 (매장별)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getStoreOrders(String storeId) {
        List<DistributorOrder> orders = orderRepository.findByStoreIdOrderByOrderedAtDesc(storeId);
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 주문 목록 조회 (매장 + 유통업체)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getStoreOrdersByDistributor(String storeId, String distributorId) {
        List<DistributorOrder> orders = orderRepository
                .findByStoreIdAndDistributorIdOrderByOrderedAtDesc(storeId, distributorId);
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 유통업체에 들어온 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getDistributorOrders(String distributorId) {
        List<DistributorOrder> orders = orderRepository
                .findByDistributorIdOrderByOrderedAtDesc(distributorId);
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, String storeId) {
        DistributorOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        return toOrderResponse(order);
    }
    
    /**
     * 주문 확정 (결제 완료 후) - ID로 조회
     */
    @Transactional
    public OrderResponse confirmOrder(Long orderId, String storeId) {
        DistributorOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 주문만 확정할 수 있습니다.");
        }
        
        order.confirm();
        DistributorOrder savedOrder = orderRepository.save(order);
        
        log.info("주문 확정 완료: {}", order.getOrderNumber());
        
        return toOrderResponse(savedOrder);
    }
    
    /**
     * 주문 확정 (결제 완료 후) - 주문번호로 조회
     */
    @Transactional
    public OrderResponse confirmOrderByNumber(String orderNumber, String storeId) {
        DistributorOrder order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));
        
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 주문만 확정할 수 있습니다.");
        }
        
        order.confirm();
        DistributorOrder savedOrder = orderRepository.save(order);
        
        log.info("주문 확정 완료: {}", order.getOrderNumber());
        
        return toOrderResponse(savedOrder);
    }
    
    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String storeId, String reason) {
        DistributorOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 주문입니다.");
        }
        
        // 재고 복구
        for (DistributorOrderItem item : order.getItems()) {
            ProductCatalog product = productRepository.findById(item.getProductId())
                    .orElse(null);
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        order.cancel(reason);
        DistributorOrder savedOrder = orderRepository.save(order);
        
        log.info("주문 취소 완료: {} (사유: {})", order.getOrderNumber(), reason);
        
        return toOrderResponse(savedOrder);
    }
    
    /**
     * 주문번호 생성 (ORD-YYYYMMDD-HHMMSS-XXX)
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = String.format("%03d", (int)(Math.random() * 1000));
        return "ORD-" + timestamp + "-" + random;
    }
    
    /**
     * 주문명 생성 (결제 시 표시될 이름)
     */
    private String generateOrderName(DistributorOrder order) {
        if (order.getItems().isEmpty()) {
            return "주문";
        }
        
        DistributorOrderItem firstItem = order.getItems().get(0);
        if (order.getItems().size() == 1) {
            return firstItem.getProductName();
        }
        
        return firstItem.getProductName() + " 외 " + (order.getItems().size() - 1) + "건";
    }
    
    /**
     * Entity -> Response 변환
     */
    private OrderResponse toOrderResponse(DistributorOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .unit(item.getUnit())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .storeId(order.getStoreId())
                .distributorId(order.getDistributorId())
                .orderNumber(order.getOrderNumber())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .totalQuantity(order.getTotalQuantity())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryPhone(order.getDeliveryPhone())
                .deliveryRequest(order.getDeliveryRequest())
                .desiredDeliveryDate(order.getDesiredDeliveryDate())
                .orderedAt(order.getOrderedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
