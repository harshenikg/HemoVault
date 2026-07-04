package com.hemovault.controller;

import com.hemovault.dto.*;
import com.hemovault.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /** GET /api/inventory/summary — stock per blood group with status */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<InventorySummary>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getSummary()));
    }

    /** GET /api/inventory/details — all individual batches */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<InventoryDetail>>> getDetails() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getDetails()));
    }

    /** GET /api/inventory/expiring?days=7 — batches expiring within N days (admin only) */
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryDetail>>> getExpiring(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getExpiring(days)));
    }

    /** GET /api/inventory/alerts — active shortage alerts (admin only) */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShortageAlertResponse>>> getAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getActiveAlerts()));
    }

    /** GET /api/inventory/stock/{bloodGroup} — quick total for one group */
    @GetMapping("/stock/{bloodGroup}")
    public ResponseEntity<ApiResponse<Double>> getStock(
            @PathVariable com.hemovault.model.BloodGroup bloodGroup) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getTotalAvailable(bloodGroup)));
    }

    /** POST /api/inventory — manually add units (admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDetail>> addInventory(
            @Valid @RequestBody InventoryRequest req) {
        var saved = inventoryService.addManual(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory added successfully", null));
    }
}
