package com.example.payflow.escrow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {
    
    @Column(name = "vehicle_vin")
    private String vin;  // 차대번호
    
    @Column(name = "vehicle_manufacturer")
    private String manufacturer;
    
    @Column(name = "vehicle_model")
    private String model;
    
    @Column(name = "vehicle_year")
    private Integer year;
    
    @Column(name = "vehicle_registration_number")
    private String registrationNumber;
    
    public Vehicle(String vin, String manufacturer, String model, Integer year, String registrationNumber) {
        this.vin = vin;
        this.manufacturer = manufacturer;
        this.model = model;
        this.year = year;
        this.registrationNumber = registrationNumber;
    }
}
