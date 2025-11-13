package com.example.carins.web;

import com.example.carins.service.PolicyService;
import com.example.carins.web.dto.PolicyDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/policies")
class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDTO> getPolicy(@PathVariable long policyId) {
        try {
            var dto = policyService.getPolicy(policyId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<?> updatePolicy(@PathVariable long policyId,
                                          @RequestBody @Valid PolicyDTO body) {
        try {
            policyService.updatePolicy(policyId, body);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<?> deletePolicy(@PathVariable long policyId) {
        try {
            policyService.deletePolicy(policyId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
