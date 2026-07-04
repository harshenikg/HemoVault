package com.hemovault.controller;

import com.hemovault.dto.*;
import com.hemovault.model.BloodRequest;
import com.hemovault.model.User;
import com.hemovault.service.BloodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class BloodRequestController {

    private final BloodRequestService requestService;

    /** GET /api/requests?status=PENDING — filter by status */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getAll(
            @RequestParam(required = false) BloodRequest.RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(requestService.findAll(status)));
    }

    /** GET /api/requests/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(requestService.findById(id)));
    }

    /** GET /api/requests/pending/count — badge count */
    @GetMapping("/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingCount() {
        return ResponseEntity.ok(ApiResponse.ok(requestService.countPending()));
    }

    /** POST /api/requests — submit a blood request */
    @PostMapping
    public ResponseEntity<ApiResponse<BloodRequestResponse>> create(
            @Valid @RequestBody BloodRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Blood request submitted — awaiting admin approval",
                        requestService.create(dto)));
    }

    /** PUT /api/requests/{id}/approve — admin approves, stock deducted automatically */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Request approved and stock deducted",
                requestService.approve(id, admin.getId())));
    }

    /** PUT /api/requests/{id}/reject — admin rejects */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) AdminNoteRequest notes) {
        String noteText = notes != null ? notes.getNotes() : null;
        return ResponseEntity.ok(ApiResponse.ok("Request rejected",
                requestService.reject(id, noteText)));
    }

    /** PUT /api/requests/{id}/issue — mark as physically dispatched */
    @PutMapping("/{id}/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> issue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Request marked as issued",
                requestService.issue(id)));
    }
}
