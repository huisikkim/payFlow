package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private DistributorOrder order;
    
    @Enumerated(EnumType.STRING)
    @Column
    private DeliveryType deliveryType; // 배송 방식 (직접배송/택배배송)
    
    @Column
    private String trackingNumber; // 송장번호 (택배배송 시)
    
    @Column
    private String courierCompany; // 배송사 (택배배송 시: CJ대한통운, 로젠택배 등)
    
    @Column
    private String courierPhone; // 배송사 연락처 (택배배송 시)
    
    @Column
    private String driverName; // 배송 기사 이름 (직접배송 시)
    
    @Column
    private String driverPhone; // 배송 기사 연락처 (직접배송 시)
    
    @Column
    private String vehicleNumber; // 차량 번호 (직접배송 시)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PREPARING;
    
    @Column
    private LocalDateTime preparedAt; // 상품 준비 완료 시간
    
    @Column
    private LocalDateTime shippedAt; // 배송 시작 시간
    
    @Column
    private LocalDateTime deliveredAt; // 배송 완료 시간
    
    @Column
    private LocalDateTime estimatedDeliveryDate; // 예상 배송일
    
    @Column
    private String deliveryAddress; // 배송지 주소
    
    @Column
    private String deliveryPhone; // 수령인 연락처
    
    @Column
    private String deliveryRequest; // 배송 요청사항
    
    @Column
    private String deliveryNotes; // 배송 메모 (유통업자용)
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void startPreparing() {
        this.status = DeliveryStatus.PREPARING;
        this.preparedAt = LocalDateTime.now();
    }
    
    // 택배 배송 시작
    public void startCourierShipping(String trackingNumber, String courierCompany, LocalDateTime estimatedDeliveryDate) {
        this.deliveryType = DeliveryType.COURIER;
        this.status = DeliveryStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.courierCompany = courierCompany;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shippedAt = LocalDateTime.now();
    }
    
    // 직접 배송 시작
    public void startDirectShipping(String driverName, String driverPhone, String vehicleNumber, LocalDateTime estimatedDeliveryDate) {
        this.deliveryType = DeliveryType.DIRECT;
        this.status = DeliveryStatus.SHIPPED;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.vehicleNumber = vehicleNumber;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shippedAt = LocalDateTime.now();
    }
    
    public void completeDelivery() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
