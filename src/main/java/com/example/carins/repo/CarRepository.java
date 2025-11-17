package com.example.carins.repo;

import com.example.carins.model.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // TODO: enforce unique VIN at DB and via validation (exercise)
    @EntityGraph(attributePaths = {"owner"})
    List<Car> findAll();

    Optional<Car> findByVin(String vin);

    @Query("SELECT c " +
            "FROM Car c " +
            "WHERE (coalesce(:ownerId, NULL) IS NULL OR c.owner.id = :ownerId) " +
            "AND (coalesce(:vin, NULL) IS NULL OR c.vin = :vin) ")
    List<Car> basicSearch(@Param("ownerId") Long ownerId,
                          @Param("vin") String vin);

    @Query("SELECT c " +
            "FROM Car c " +
            "WHERE (coalesce(:ownerId, NULL) IS NULL OR c.owner.id = :ownerId) " +
            "AND (coalesce(:vin, NULL) IS NULL OR c.vin = :vin) " +
            "AND EXISTS(SELECT p FROM InsurancePolicy p WHERE p.car.id = c.id AND p.startDate <= CURRENT_DATE AND p" +
            ".endDate >= CURRENT_DATE )")
    List<Car> basicSearchActivePolicy(@Param("ownerId") Long ownerId,
                                      @Param("vin") String vin);

    @Query("SELECT c " +
            "FROM Car c " +
            "WHERE (coalesce(:ownerId, NULL) IS NULL OR c.owner.id = :ownerId) " +
            "AND (coalesce(:vin, NULL) IS NULL OR c.vin = :vin) " +
            "AND NOT EXISTS(SELECT p FROM InsurancePolicy p WHERE p.car.id = c.id AND p.startDate <= CURRENT_DATE AND" +
            " p" +
            ".endDate >= CURRENT_DATE) " +
            "AND EXISTS(SELECT p FROM InsurancePolicy p WHERE p.car.id = c.id)")
    List<Car> basicSearchExpiredPolicy(@Param("ownerId") Long ownerId,
                                       @Param("vin") String vin);

    @Query("SELECT c " +
            "FROM Car c " +
            "WHERE (coalesce(:ownerId, NULL) IS NULL OR c.owner.id = :ownerId) " +
            "AND (coalesce(:vin, NULL) IS NULL OR c.vin = :vin) " +
            "AND NOT EXISTS(SELECT p FROM InsurancePolicy p WHERE p.car.id = c.id)")
    List<Car> basicSearchNoPolicy(@Param("ownerId") Long ownerId,
                                  @Param("vin") String vin);
}