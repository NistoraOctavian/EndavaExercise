package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.CarOwnerHistory;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.*;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final CarOwnerHistoryRepository carOwnerHistoryRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository insurancePolicyRepository,
                      InsuranceClaimRepository insuranceClaimRepository,
                      CarOwnerHistoryRepository carOwnerHistoryRepository,
                      OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.insuranceClaimRepository = insuranceClaimRepository;
        this.carOwnerHistoryRepository = carOwnerHistoryRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) throws ChangeSetPersister.NotFoundException {
        if (carId == null || date == null) return false;
        carRepository.findById(carId).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return insurancePolicyRepository.existsActiveOnDate(carId, date);
    }

    public long registerClaim(Long carId, LocalDate claimDate, String description, int amount) throws NoSuchElementException {
        var car = carRepository.findById(carId).orElseThrow();
        var insuranceClaim = new InsuranceClaim();
        insuranceClaim.setCar(car);
        insuranceClaim.setClaimDate(claimDate);
        insuranceClaim.setDescription(description);
        insuranceClaim.setAmount(amount);

        insuranceClaimRepository.save(insuranceClaim);

        return insuranceClaim.getId();
    }

    public List<String> carHistory(Long carId) throws NoSuchElementException {
        carRepository.findById(carId).orElseThrow();
        List<InsurancePolicy> policyList = insurancePolicyRepository.carPolicyHistory(carId);
        List<InsuranceClaim> claimList = insuranceClaimRepository.carClaimHistory(carId);
        var result = new ArrayList<String>();
        int claimIndex = 0;

        for (int i = 0; i < policyList.size() - 1; i++) {
            InsurancePolicy policy = policyList.get(i);

            claimIndex = addClaimsToHistory(result, claimList, claimIndex, policy.getStartDate());
            result.add("Policy " + policy.getId() + " for car " + policy.getCar().getId() + " started on "
                    + policy.getStartDate());
            claimIndex = addClaimsToHistory(result, claimList, claimIndex, policy.getEndDate());
            result.add("Policy " + policy.getId() + " for car " + policy.getCar().getId() + " expired on "
                    + policy.getEndDate());
        }
        InsurancePolicy policy = policyList.getLast();

        claimIndex = addClaimsToHistory(result, claimList, claimIndex, policy.getStartDate());

        result.add("Policy " + policy.getId() + " for car " + policy.getCar().getId() + " started on "
                + policy.getStartDate());

        claimIndex = addClaimsToHistory(result, claimList, claimIndex, policy.getEndDate());

        if (policy.getEndDate().isBefore(LocalDate.now())) {
            result.add("Policy " + policy.getId() + " for car " + policy.getCar().getId() + " expired on "
                    + policy.getEndDate());

            while (claimIndex < claimList.size()) {
                InsuranceClaim claim = claimList.get(claimIndex++);
                result.add("Claim " + claim.getId() + " for car " + claim.getCar().getId() + " on "
                        + claim.getClaimDate() + " with amount " + claim.getAmount() + " and description: "
                        + claim.getDescription());
            }
        }

        return result;
    }

    public long transferOwner(long carId, long ownerId) throws NoSuchElementException {
        var car = carRepository.findById(carId).orElseThrow();
        var newOwner = ownerRepository.findById(ownerId).orElseThrow();

        car.setOwner(newOwner);
        carRepository.save(car);

        var carOwnerHistory = carOwnerHistoryRepository.findFirstByCarOrderByStartDateDesc(car);
        if (carOwnerHistory.isPresent()) {
            var oldRecord = carOwnerHistory.get();

            oldRecord.setEndDate(LocalDate.now());
            carOwnerHistoryRepository.save(oldRecord);
        }

        var newRecord = new CarOwnerHistory();
        newRecord.setCar(car);
        newRecord.setOwner(newOwner);
        newRecord.setStartDate(LocalDate.now());
        carOwnerHistoryRepository.save(newRecord);

        return newRecord.getId();
    }


    private int addClaimsToHistory(List<String> result, List<InsuranceClaim> claimList, int claimIndex,
                                   LocalDate beforeDate) {
        while (claimIndex < claimList.size() &&
                claimList.get(claimIndex).getClaimDate().isBefore(beforeDate)) {
            InsuranceClaim claim = claimList.get(claimIndex++);
            result.add("Claim " + claim.getId() + " for car " + claim.getCar().getId() + " on "
                    + claim.getClaimDate() + " with amount " + claim.getAmount() + " and description: "
                    + claim.getDescription());
        }

        return claimIndex;
    }
}
