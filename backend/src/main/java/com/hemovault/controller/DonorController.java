package com.hemovault.controller;

import com.hemovault.dto.*;
import com.hemovault.model.BloodGroup;
import com.hemovault.model.User;
import com.hemovault.service.DonorService;
import com.hemovault.service.EligibilityService;
import com.hemovault.service.EmergencyMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;
    private final EligibilityService eligibilityService;
    private final EmergencyMatchingService emergencyMatchingService;

    /** GET /api/donors — list all donors */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonorResponse>>> getAll(
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.ok(donorService.findAll(isAdmin)));
    }

    /** GET /api/donors/{id} — get donor by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonorResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.ok(donorService.findById(id, isAdmin)));
    }

    /** GET /api/donors/search?bloodGroup=O_POS&city=Delhi */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DonorResponse>>> search(
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) String city,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.ok(donorService.search(bloodGroup, city, isAdmin)));
    }

    /** POST /api/donors — register new donor */
    @PostMapping
    public ResponseEntity<ApiResponse<DonorResponse>> create(
            @Valid @RequestBody DonorRequest req,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Donor registered successfully", donorService.create(req, isAdmin)));
    }

    /** PUT /api/donors/{id} — update donor */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DonorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DonorRequest req,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.ok("Donor updated successfully", donorService.update(id, req, isAdmin)));
    }

    /** DELETE /api/donors/{id} — delete donor */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        donorService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Donor deleted", null));
    }

    /** POST /api/donors/eligibility-check */
    @PostMapping("/eligibility-check")
    public ResponseEntity<ApiResponse<EligibilityResponse>> checkEligibility(
            @RequestBody EligibilityRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(eligibilityService.check(req)));
    }

    /** POST /api/donors/emergency-match */
    @PostMapping("/emergency-match")
    public ResponseEntity<ApiResponse<EmergencyMatchResponse>> emergencyMatch(
            @Valid @RequestBody EmergencyMatchRequest req,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.getIsAdmin());
        return ResponseEntity.ok(ApiResponse.ok(emergencyMatchingService.findMatches(req, isAdmin)));
    }
}
