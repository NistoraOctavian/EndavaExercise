package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.AddPolicyRequestDTO;
import com.example.carins.web.dto.PolicyDTO;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class PolicyService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final CarRepository carRepository;

    public PolicyService(InsurancePolicyRepository insurancePolicyRepository, CarRepository carRepository) {
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.carRepository = carRepository;
    }

    public long addPolicy(Long carId, AddPolicyRequestDTO dto) throws NoSuchElementException {
        var car = carRepository.findById(carId).orElseThrow();

        var newPolicy = new InsurancePolicy();
        newPolicy.setCar(car);
        newPolicy.setProvider(dto.provider());
        newPolicy.setStartDate(dto.startDate());
        newPolicy.setEndDate(dto.endDate());
        insurancePolicyRepository.save(newPolicy);

        return newPolicy.getId();
    }

    public PolicyDTO getPolicy(Long policyId) throws NoSuchElementException {
        var policy = insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow();

        return new PolicyDTO(policy.getCar().getId(), policy.getProvider(), policy.getStartDate(), policy.getEndDate());
    }

    public void updatePolicy(Long policyId, PolicyDTO dto) throws NoSuchElementException {
        var policy = insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow();
        if (policy.isDeleted()) {
            throw new NoSuchElementException();
        }
        var car = carRepository.findById(dto.carId()).orElseThrow();

        policy.setCar(car);
        policy.setProvider(dto.provider());
        policy.setStartDate(dto.startDate());
        policy.setEndDate(dto.endDate());
        insurancePolicyRepository.save(policy);
    }

    public void deletePolicy(Long policyId) throws NoSuchElementException {
        var policy = insurancePolicyRepository.findByIdAndDeletedFalse(policyId).orElseThrow();
        if (policy.isDeleted()) {
            throw new NoSuchElementException();
        }

        policy.setDeleted(true);
        insurancePolicyRepository.save(policy);
    }
}
