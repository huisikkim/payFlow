package com.example.payflow.ainjob.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    
    private String city;
    private String district;
    private String detail;
    
    public Address(String city, String district, String detail) {
        this.city = city;
        this.district = district;
        this.detail = detail;
    }
}
