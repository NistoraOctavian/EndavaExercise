package com.example.carins.repo;

import com.example.carins.model.Car;
import com.example.carins.model.CarOwnerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarOwnerHistoryRepository extends JpaRepository<CarOwnerHistory, Long> {
    Optional<CarOwnerHistory> findFirstByCarOrderByStartDateDesc(Car carId);
}
