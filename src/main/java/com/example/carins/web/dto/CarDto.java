package com.example.carins.web.dto;

import com.example.carins.model.Car;

public record CarDto(Long id,
                     String vin,
                     String make,
                     String model,
                     int year,
                     Long ownerId,
                     String ownerName,
                     String ownerEmail) {
    public CarDto(Car car) {
        this(car.getId(),
                car.getVin(),
                car.getMake(),
                car.getModel(),
                car.getYearOfManufacture(),
                car.getOwner() != null ? car.getOwner().getId() : null,
                car.getOwner() != null ? car.getOwner().getName() : null,
                car.getOwner() != null ? car.getOwner().getEmail() : null);
    }
}
