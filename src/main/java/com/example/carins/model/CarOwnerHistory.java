package com.example.carins.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "car_owner_history")
public class CarOwnerHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Car car;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Owner owner;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;

    public CarOwnerHistory() {
    }

    public CarOwnerHistory(Long id, Car car, Owner owner, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.car = car;
        this.owner = owner;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
