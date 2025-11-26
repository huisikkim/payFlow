package com.example.payflow.catalog.infrastructure;

import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.domain.ProductCatalogRepository;
import com.example.payflow.catalog.domain.ProductDeliveryInfo;
import com.example.payflow.catalog.domain.ProductDeliveryInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogDataInitializer implements CommandLineRunner {
    
    private final ProductCatalogRepository productRepository;
    private final ProductDeliveryInfoRepository deliveryInfoRepository;
    
    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("카탈로그 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("카탈로그 테스트 데이터 초기화 시작...");
        
        // 상품 1: 경기미 20kg
        ProductCatalog product1 = ProductCatalog.builder()
                .distributorId("distributor1")
                .productName("경기미 20kg")
                .category("쌀/곡물")
                .description("경기도에서 생산된 고품질 쌀입니다")
                .unitPrice(48000L)
                .unit("포")
                .stockQuantity(100)
                .origin("경기도")
                .brand("농협")
                .imageUrl("https://example.com/rice.jpg")
                .isAvailable(true)
                .minOrderQuantity(1)
                .maxOrderQuantity(50)
                .certifications("친환경인증")
                .build();
        productRepository.save(product1);
        
        ProductDeliveryInfo delivery1 = ProductDeliveryInfo.builder()
                .productId(product1.getId())
                .deliveryType("익일배송")
                .deliveryFee(3000)
                .freeDeliveryThreshold(50000)
                .estimatedDeliveryDays(1)
                .build();
        deliveryInfoRepository.save(delivery1);
        
        // 상품 2: 국산 양파
        ProductCatalog product2 = ProductCatalog.builder()
                .distributorId("distributor1")
                .productName("국산 양파")
                .category("채소")
                .description("신선한 국산 양파입니다")
                .unitPrice(3000L)
                .unit("kg")
                .stockQuantity(450)
                .origin("전라남도")
                .brand("농협")
                .imageUrl("https://example.com/onion.jpg")
                .isAvailable(true)
                .minOrderQuantity(5)
                .maxOrderQuantity(100)
                .certifications("GAP인증")
                .build();
        productRepository.save(product2);
        
        ProductDeliveryInfo delivery2 = ProductDeliveryInfo.builder()
                .productId(product2.getId())
                .deliveryType("익일배송")
                .deliveryFee(3000)
                .freeDeliveryThreshold(50000)
                .estimatedDeliveryDays(1)
                .build();
        deliveryInfoRepository.save(delivery2);
        
        // 상품 3: 한우 등심
        ProductCatalog product3 = ProductCatalog.builder()
                .distributorId("distributor1")
                .productName("한우 등심 1등급")
                .category("육류")
                .description("신선한 한우 등심입니다")
                .unitPrice(85000L)
                .unit("kg")
                .stockQuantity(50)
                .origin("충청남도")
                .brand("한우마을")
                .imageUrl("https://example.com/beef.jpg")
                .isAvailable(true)
                .minOrderQuantity(1)
                .maxOrderQuantity(20)
                .certifications("한우인증")
                .build();
        productRepository.save(product3);
        
        ProductDeliveryInfo delivery3 = ProductDeliveryInfo.builder()
                .productId(product3.getId())
                .deliveryType("당일배송")
                .deliveryFee(5000)
                .freeDeliveryThreshold(100000)
                .estimatedDeliveryDays(0)
                .build();
        deliveryInfoRepository.save(delivery3);
        
        log.info("✅ 카탈로그 테스트 데이터 초기화 완료! (3개 상품)");
    }
}
