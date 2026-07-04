package com.hemovault.controller;

import com.hemovault.dto.*;
import com.hemovault.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    /** GET /api/donations — all donations */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(donationService.findAll()));
    }

    /** GET /api/donations/{id} — single donation */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(donationService.findById(id)));
    }

    /** GET /api/donations/donor/{donorId} — donations by donor */
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.ok(donationService.findByDonor(donorId)));
    }

    /** POST /api/donations — record a donation */
    @PostMapping
    public ResponseEntity<ApiResponse<DonationResponse>> record(
            @Valid @RequestBody DonationRequest req) {
        DonationResponse resp = donationService.record(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Donation recorded — " + resp.getStockImpact(), resp));
    }
}
