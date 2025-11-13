package com.example.carins.web;

import com.example.carins.service.CarService;
import com.example.carins.service.PolicyService;
import com.example.carins.web.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;
    private final PolicyService policyService;

    public CarController(CarService carService, PolicyService policyService) {
        this.carService = carService;
        this.policyService = policyService;
    }

    @GetMapping("")
    public List<CarDto> getCars() {
        return carService.listCars().stream().map(CarDto::new).toList();
    }

    @GetMapping("/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@NotNull @PathVariable Long carId, @NotEmpty @RequestParam String date) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Date must be in a valid YYYY-MM-DD format");
        }

        LocalDate d = LocalDate.parse(date);
        try {
            boolean valid = carService.isInsuranceValid(carId, d);
            return ResponseEntity.ok(new InsuranceValidityResponseDTO(carId, d.toString(), valid));
        } catch (ChangeSetPersister.NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{carId}/claims")
    public ResponseEntity<?> registerClaim(@NotNull @PathVariable Long carId,
                                           @Valid @RequestBody RegisterClaimRequestDTO body) {
        try {
            var claimId = carService.registerClaim(carId, body.claimDate(), body.description(), body.amount());
            var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/api/claims/" + claimId)
                    .build()
                    .toUri();
            return ResponseEntity.created(uri).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{carId}/history")
    public ResponseEntity<?> getCarHistory(@NotNull @PathVariable Long carId) {
        try {
            var history = carService.carHistory(carId);
            return ResponseEntity.ok(history);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{carId}/transfer-owner")
    public ResponseEntity<?> transferOwner(@NotNull @PathVariable Long carId,
                                           @Valid @RequestBody TransferOwnerRequestDTO body) {
        try {
            var transferId = carService.transferOwner(carId, body.ownerId());
            var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/api/car-owner-history/" + transferId)
                    .build()
                    .toUri();
            return ResponseEntity.created(uri).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{carId}/policies")
    public ResponseEntity<?> addPolicy(@NotNull @PathVariable Long carId,
                                       @Valid @RequestBody AddPolicyRequestDTO body) {
        try {
            var policyId = policyService.addPolicy(carId, body);
            var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/api/policies/" + policyId)
                    .build()
                    .toUri();
            return ResponseEntity.created(uri).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}