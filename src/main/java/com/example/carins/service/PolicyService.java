package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.shared.exceptions.InvalidRangeException;
import com.example.carins.shared.exceptions.OverlapException;
import com.example.carins.web.dto.AddPolicyRequestDTO;
import com.example.carins.web.dto.DateRangeDTO;
import com.example.carins.web.dto.PolicyCoverageDTO;
import com.example.carins.web.dto.PolicyDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class PolicyService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final CarRepository carRepository;

    public PolicyService(InsurancePolicyRepository insurancePolicyRepository, CarRepository carRepository) {
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.carRepository = carRepository;
    }

    public long addPolicy(Long carId, AddPolicyRequestDTO dto) {
        if (dto.startDate().isAfter(dto.endDate())) {
            throw new InvalidRangeException("Start date is after end date");
        }

        var car = carRepository.findById(carId).orElseThrow();
        if (insurancePolicyRepository.existsOverlapping(car.getId(), dto.startDate(), dto.endDate())) {
            throw new OverlapException("Overlapping exists");
        }


        var newPolicy = new InsurancePolicy();
        newPolicy.setCar(car);
        newPolicy.setProvider(dto.provider());
        newPolicy.setStartDate(dto.startDate());
        newPolicy.setEndDate(dto.endDate());
        insurancePolicyRepository.save(newPolicy);

        return newPolicy.getId();
    }

    public PolicyDTO getPolicy(Long policyId) {
        var policy =
                insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow(() -> new EntityNotFoundException("Policy not found"));

        return new PolicyDTO(policy.getCar().getId(), policy.getProvider(), policy.getStartDate(), policy.getEndDate());
    }

    public void updatePolicy(Long policyId, PolicyDTO dto) {
        if (dto.startDate().isAfter(dto.endDate())) {
            throw new InvalidRangeException("Start date is after end date");
        }

        var car = carRepository.findById(dto.carId()).orElseThrow();
        if (insurancePolicyRepository.existsOverlapping(car.getId(), dto.startDate(), dto.endDate())) {
            throw new OverlapException("Overlapping exists");
        }

        var policy =
                insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow(() -> new EntityNotFoundException("Policy not found"));

        policy.setCar(car);
        policy.setProvider(dto.provider());
        policy.setStartDate(dto.startDate());
        policy.setEndDate(dto.endDate());
        insurancePolicyRepository.save(policy);
    }

    public void deletePolicy(Long policyId) {
        var policy =
                insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow(() -> new EntityNotFoundException("Policy not found"));

        policy.setDeleted(true);
        insurancePolicyRepository.save(policy);
    }

    public PolicyCoverageDTO getCoverage(Long carId, LocalDate from, LocalDate to) {
        carRepository.findById(carId).orElseThrow(() -> new EntityNotFoundException("Car not found"));

        var carCoverageWindows = insurancePolicyRepository.findCarCoverageWindows(carId, from, to);
        carCoverageWindows.sort(Comparator.comparing(InsurancePolicy::getStartDate));

        var currentCarCoverageWindow = carCoverageWindows.getFirst();
        var gapList = new ArrayList<DateRangeDTO>();
        var windowList = new ArrayList<DateRangeDTO>();
        var i = 0;

        if (from != null) {
            var startsWithGap = currentCarCoverageWindow.getStartDate().isAfter(from);
            if (startsWithGap) {
                gapList.add(new DateRangeDTO(from, currentCarCoverageWindow.getStartDate()));
            }
            windowList.add(new DateRangeDTO(startsWithGap ? currentCarCoverageWindow.getStartDate() : from,
                    currentCarCoverageWindow.getEndDate()));

            var nextCarCoverageWindow = carCoverageWindows.get(i + 1);
            gapList.add(new DateRangeDTO(currentCarCoverageWindow.getEndDate(), nextCarCoverageWindow.getStartDate()));
            currentCarCoverageWindow = nextCarCoverageWindow;
        }

        for (; i < carCoverageWindows.size() - 1; i++) {
            windowList.add(new DateRangeDTO(currentCarCoverageWindow.getStartDate(),
                    currentCarCoverageWindow.getEndDate()));

            var nextCarCoverageWindow = carCoverageWindows.get(i + 1);
            gapList.add(new DateRangeDTO(currentCarCoverageWindow.getEndDate(), nextCarCoverageWindow.getStartDate()));
            currentCarCoverageWindow = nextCarCoverageWindow;
        }

        if (to != null) {
            var endsWithGap = currentCarCoverageWindow.getEndDate().isBefore(to);
            windowList.add(new DateRangeDTO(currentCarCoverageWindow.getStartDate(), endsWithGap ?
                    currentCarCoverageWindow.getEndDate() : to));
            if (endsWithGap) {
                gapList.add(new DateRangeDTO(currentCarCoverageWindow.getEndDate(), to));
            }
        } else {
            windowList.add(new DateRangeDTO(currentCarCoverageWindow.getStartDate(),
                    currentCarCoverageWindow.getEndDate()));
        }

        return new PolicyCoverageDTO(gapList, windowList);
    }
}
