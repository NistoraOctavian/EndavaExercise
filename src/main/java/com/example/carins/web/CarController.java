package com.example.carins.web;

import com.example.carins.service.CarService;
import com.example.carins.service.PolicyService;
import com.example.carins.web.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

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
    public List<CarDto> getCars(@RequestParam(required = false) Long ownerId,
                                @RequestParam(required = false) String vin,
                                @RequestParam(required = false) String policyStatus) {
        return carService.getCars(ownerId, vin, policyStatus);
    }

    @GetMapping("/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@NotNull @PathVariable Long carId, @NotEmpty @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);

        boolean valid = carService.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponseDTO(carId, d.toString(), valid));
    }

    @PostMapping("/{carId}/claims")
    public ResponseEntity<?> registerClaim(@NotNull @PathVariable Long carId,
                                           @Valid @RequestBody RegisterClaimRequestDTO body) {
        var claimId = carService.registerClaim(carId, body.claimDate(), body.description(), body.amount());
        var uri =
                ServletUriComponentsBuilder.fromCurrentRequest().replacePath("/api/claims/" + claimId).build().toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{carId}/history")
    public ResponseEntity<?> getCarHistory(@NotNull @PathVariable Long carId) {
        var history = carService.carHistory(carId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{carId}/transfer-owner")
    public ResponseEntity<?> transferOwner(@NotNull @PathVariable Long carId,
                                           @Valid @RequestBody TransferOwnerRequestDTO body) {
        var transferId = carService.transferOwner(carId, body.ownerId());
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/car-owner-history/" + transferId)
                .build()
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{carId}/policies")
    public ResponseEntity<?> addPolicy(@NotNull @PathVariable Long carId,
                                       @Valid @RequestBody AddPolicyRequestDTO body) {
        var policyId = policyService.addPolicy(carId, body);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/policies/" + policyId)
                .build()
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{carId}/coverage")
    public ResponseEntity<?> getCoverage(@PathVariable long carId,
                                         @RequestParam(required = false) LocalDate from,
                                         @RequestParam(required = false) LocalDate to) {
        var coverage = policyService.getCoverage(carId, from, to);
        return ResponseEntity.ok(coverage);
    }
}