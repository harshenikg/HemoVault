package com.hemovault.service;

import com.hemovault.dto.InventoryDetail;
import com.hemovault.dto.InventorySummary;
import com.hemovault.dto.ShortageAlertResponse;
import com.hemovault.model.*;
import com.hemovault.repository.BloodInventoryRepository;
import com.hemovault.repository.ShortageAlertRepository;
import com.hemovault.repository.ShortageThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final BloodInventoryRepository invRepo;
    private final ShortageThresholdRepository thresholdRepo;
    private final ShortageAlertRepository alertRepo;

    @Value("${bloodbank.expiry.warning-days:7}")
    private int warningDays;

    // ── Summary ────────────────────────────────────────────────────────────

    public List<InventorySummary> getSummary() {
        return Arrays.stream(BloodGroup.values())
                .map(bg -> {
                    Double total = invRepo.getTotalAvailableByBloodGroup(bg);
                    ShortageThreshold t = thresholdRepo.findByBloodGroup(bg).orElse(null);
                    double threshold = t != null ? t.getThresholdUnits() : 10.0;
                    double critical  = t != null ? t.getCriticalUnits()  : 5.0;

                    String status = "ADEQUATE";
                    if (total <= critical)   status = "CRITICAL";
                    else if (total < threshold) status = "LOW";

                    return InventorySummary.builder()
                            .bloodGroup(bg)
                            .bloodGroupLabel(bg.getLabel())
                            .totalUnits(total)
                            .thresholdUnits(threshold)
                            .criticalUnits(critical)
                            .stockStatus(status)
                            .build();
                })
                .toList();
    }

    public List<InventoryDetail> getDetails() {
        return invRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDetail)
                .toList();
    }

    public List<InventoryDetail> getExpiring(int days) {
        LocalDate warningDate = LocalDate.now().plusDays(days);
        return invRepo.findExpiringBefore(warningDate).stream()
                .map(this::toDetail)
                .toList();
    }

    public Double getTotalAvailable(BloodGroup bg) {
        return invRepo.getTotalAvailableByBloodGroup(bg);
    }

    // ── Add Units ──────────────────────────────────────────────────────────

    @Transactional
    public BloodInventory addUnits(BloodGroup bg, double units,
                                   LocalDate collectionDate, LocalDate expiryDate,
                                   String batchNumber, String storageLocation) {
        BloodInventory inv = BloodInventory.builder()
                .bloodGroup(bg)
                .unitsAvailable(units)
                .collectionDate(collectionDate)
                .expiryDate(expiryDate != null ? expiryDate : LocalDate.now().plusDays(35))
                .batchNumber(batchNumber)
                .storageLocation(storageLocation != null ? storageLocation : "Main Storage")
                .status(BloodInventory.InventoryStatus.AVAILABLE)
                .build();
        inv = invRepo.save(inv);
        resolveAlertsIfSufficient(bg);
        return inv;
    }

    @Transactional
    public BloodInventory addManual(com.hemovault.dto.InventoryRequest req) {
        return addUnits(req.getBloodGroup(), req.getUnitsAvailable(),
                req.getCollectionDate(), req.getExpiryDate(),
                req.getBatchNumber(), req.getStorageLocation());
    }

    // ── Deduct Units (FIFO) ────────────────────────────────────────────────

    @Transactional
    public double deductUnits(BloodGroup bg, double needed) {
        List<BloodInventory> batches = invRepo.findAvailableByBloodGroupFIFO(bg);
        double remaining = needed;
        double totalDeducted = 0;

        for (BloodInventory batch : batches) {
            if (remaining <= 0) break;
            double take = Math.min(batch.getUnitsAvailable(), remaining);
            batch.setUnitsAvailable(batch.getUnitsAvailable() - take);
            if (batch.getUnitsAvailable() <= 0) {
                batch.setStatus(BloodInventory.InventoryStatus.ISSUED);
            }
            invRepo.save(batch);
            totalDeducted += take;
            remaining -= take;
        }

        if (remaining > 0) {
            throw new IllegalStateException(
                    "Insufficient stock for " + bg.getLabel() +
                    ": " + (needed - remaining) + " available, " + needed + " required");
        }

        checkAndRaiseAlert(bg);
        return totalDeducted;
    }

    // ── Expiry ─────────────────────────────────────────────────────────────

    @Transactional
    public int expireOldUnits() {
        List<BloodInventory> expired = invRepo.findExpired();
        expired.forEach(b -> b.setStatus(BloodInventory.InventoryStatus.EXPIRED));
        invRepo.saveAll(expired);

        if (!expired.isEmpty()) {
            Arrays.stream(BloodGroup.values()).forEach(this::checkAndRaiseAlert);
            log.info("Expired {} blood unit batch(es)", expired.size());
        }
        return expired.size();
    }

    // ── Shortage Alerts ────────────────────────────────────────────────────

    public void checkAndRaiseAlert(BloodGroup bg) {
        double total = invRepo.getTotalAvailableByBloodGroup(bg);
        ShortageThreshold t = thresholdRepo.findByBloodGroup(bg).orElse(null);
        if (t == null) return;

        ShortageAlert.AlertType alertType = null;
        if (total <= t.getCriticalUnits())        alertType = ShortageAlert.AlertType.CRITICAL;
        else if (total < t.getThresholdUnits())   alertType = ShortageAlert.AlertType.WARNING;

        if (alertType != null) {
            boolean exists = alertRepo.findByBloodGroupAndAlertTypeAndIsResolvedFalse(bg, alertType).isPresent();
            if (!exists) {
                alertRepo.save(ShortageAlert.builder()
                        .bloodGroup(bg)
                        .currentUnits(total)
                        .thresholdUnits(t.getThresholdUnits())
                        .alertType(alertType)
                        .isResolved(false)
                        .build());
                log.warn("Shortage alert raised for {}: {} units ({})", bg.getLabel(), total, alertType);
            }
        }
    }

    public void resolveAlertsIfSufficient(BloodGroup bg) {
        double total = invRepo.getTotalAvailableByBloodGroup(bg);
        ShortageThreshold t = thresholdRepo.findByBloodGroup(bg).orElse(null);
        if (t == null || total < t.getThresholdUnits()) return;

        List<ShortageAlert> active = alertRepo.findByBloodGroupAndIsResolvedFalse(bg);
        if (!active.isEmpty()) {
            active.forEach(a -> {
                a.setIsResolved(true);
                a.setResolvedAt(java.time.LocalDateTime.now());
            });
            alertRepo.saveAll(active);
        }
    }

    public List<ShortageAlertResponse> getActiveAlerts() {
        return alertRepo.findByIsResolvedFalseOrderByCreatedAtDesc().stream()
                .map(a -> ShortageAlertResponse.builder()
                        .id(a.getId())
                        .bloodGroup(a.getBloodGroup())
                        .bloodGroupLabel(a.getBloodGroup().getLabel())
                        .currentUnits(a.getCurrentUnits())
                        .thresholdUnits(a.getThresholdUnits())
                        .alertType(a.getAlertType())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private InventoryDetail toDetail(BloodInventory b) {
        long daysToExpiry = b.getExpiryDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), b.getExpiryDate()) : 0;
        return InventoryDetail.builder()
                .id(b.getId())
                .bloodGroup(b.getBloodGroup())
                .bloodGroupLabel(b.getBloodGroup().getLabel())
                .unitsAvailable(b.getUnitsAvailable())
                .collectionDate(b.getCollectionDate())
                .expiryDate(b.getExpiryDate())
                .batchNumber(b.getBatchNumber())
                .storageLocation(b.getStorageLocation())
                .status(b.getStatus())
                .daysToExpiry(daysToExpiry)
                .createdAt(b.getCreatedAt())
                .build();
    }
}
