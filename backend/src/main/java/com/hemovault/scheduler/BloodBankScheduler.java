package com.hemovault.scheduler;

import com.hemovault.service.InventoryService;
import com.hemovault.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BloodBankScheduler {

    private final InventoryService inventoryService;
    private final PredictionService predictionService;

    /**
     * Hourly: expire old blood units and raise shortage alerts.
     */
    @Scheduled(fixedRateString = "${bloodbank.shortage.check-interval-ms:3600000}")
    public void runHourlyChecks() {
        try {
            int expired = inventoryService.expireOldUnits();
            if (expired > 0) {
                log.info("[Scheduler] Expired {} batch(es) of blood units", expired);
            }
            log.debug("[Scheduler] Hourly inventory check complete");
        } catch (Exception e) {
            log.error("[Scheduler] Error during hourly check: {}", e.getMessage(), e);
        }
    }

    /**
     * Daily at midnight: generate and persist demand predictions for all blood groups.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyPredictions() {
        try {
            predictionService.predictNextWeek();
            log.info("[Scheduler] Daily demand predictions generated for all blood groups");
        } catch (Exception e) {
            log.error("[Scheduler] Error generating daily predictions: {}", e.getMessage(), e);
        }
    }
}
