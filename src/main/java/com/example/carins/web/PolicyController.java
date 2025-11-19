package com.example.carins.web;

import com.example.carins.service.PolicyService;
import com.example.carins.web.dto.PolicyDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDTO> getPolicy(@PathVariable long policyId) {
        var dto = policyService.getPolicy(policyId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<?> updatePolicy(@PathVariable long policyId,
                                          @RequestBody @Valid PolicyDTO body) {
        policyService.updatePolicy(policyId, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<?> deletePolicy(@PathVariable long policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.ok().build();
    }
}
