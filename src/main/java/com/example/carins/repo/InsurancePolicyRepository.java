package com.example.carins.repo;

import com.example.carins.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    @Query("select case when count(p) > 0 then true else false end " +
            "from InsurancePolicy p " +
            "where p.car.id = :carId " +
            "and p.deleted is false " +
            "and p.startDate <= :date " +
            "and (p.endDate is null or p.endDate >= :date)")
    boolean existsActiveOnDate(@Param("carId") Long carId, @Param("date") LocalDate date);

    List<InsurancePolicy> findByCarIdAndDeletedFalse(Long carId);

    @Query("select p " +
            "from InsurancePolicy p " +
            "where p.car.id = :carId " +
            "and p.deleted is false " +
            "and (p.startDate < current_date " +
            "or p.endDate < current_date)" +
            "order by p.startDate"
    )
    List<InsurancePolicy> carPolicyHistory(Long carId);

    List<InsurancePolicy> findByEndDateAndDeletedFalse(LocalDate endDate);

    Optional<InsurancePolicy> findByIdAndDeletedFalse(Long aLong);
}