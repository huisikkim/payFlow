package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {
    private String vin;
    private String manufacturer;
    private String model;
    private Integer year;
    private String registrationNumber;
    
    public Vehicle toEntity() {
        return new Vehicle(vin, manufacturer, model, year, registrationNumber);
    }
    
    public static VehicleDto from(Vehicle vehicle) {
        return new VehicleDto(
            vehicle.getVin(),
            vehicle.getManufacturer(),
            vehicle.getModel(),
            vehicle.getYear(),
            vehicle.getRegistrationNumber()
        );
    }
}
