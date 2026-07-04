package com.hemovault.service;

import com.hemovault.dto.PredictionResponse;
import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodRequest;
import com.hemovault.model.PredictionHistory;
import com.hemovault.repository.BloodRequestRepository;
import com.hemovault.repository.PredictionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final BloodRequestRepository requestRepo;
    private final PredictionHistoryRepository predHistoryRepo;
    private final InventoryService inventoryService;

    @Value("${bloodbank.prediction.window-days:30}")
    private int windowDays;

    @Transactional
    public List<PredictionResponse> predictNextWeek() {
        LocalDateTime since = LocalDateTime.now().minusDays(windowDays);
        List<BloodRequest> recentRequests = requestRepo.findRecentRequests(since);

        return Arrays.stream(BloodGroup.values()).map(bg -> {
            double totalDemand = recentRequests.stream()
                    .filter(r -> r.getBloodGroup() == bg)
                    .mapToDouble(BloodRequest::getUnitsRequired)
                    .sum();

            double avgDaily = totalDemand / windowDays;
            double predicted7d = Math.max(2.0, Math.round(avgDaily * 7 * 10.0) / 10.0);

            double currentStock = inventoryService.getTotalAvailable(bg);

            String demandTrend;
            if (predicted7d > currentStock * 0.8)       demandTrend = "↑ HIGH DEMAND";
            else if (predicted7d < currentStock * 0.2)  demandTrend = "↓ LOW DEMAND";
            else                                         demandTrend = "→ STABLE";

            String stockStatus = currentStock >= predicted7d ? "SUFFICIENT" : "INSUFFICIENT";

            // Persist prediction history
            predHistoryRepo.save(PredictionHistory.builder()
                    .bloodGroup(bg)
                    .predictionDate(LocalDate.now())
                    .predictedUnits(predicted7d)
                    .predictionMethod("MOVING_AVERAGE")
                    .windowDays(windowDays)
                    .build());

            return PredictionResponse.builder()
                    .bloodGroup(bg)
                    .bloodGroupLabel(bg.getLabel())
                    .predictedUnits7d(predicted7d)
                    .currentStock(currentStock)
                    .demandTrend(demandTrend)
                    .stockStatus(stockStatus)
                    .build();
        }).toList();
    }

    public List<PredictionResponse> getPredictions() {
        return predictNextWeek();
    }

    public PredictionResponse getPredictionForGroup(BloodGroup bg) {
        return getPredictions().stream()
                .filter(p -> p.getBloodGroup() == bg)
                .findFirst()
                .orElseThrow(() -> new NoSuchFieldError("No prediction for " + bg));
    }
}
