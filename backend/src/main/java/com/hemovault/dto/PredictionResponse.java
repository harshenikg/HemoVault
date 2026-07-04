package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PredictionResponse {
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double predictedUnits7d;
    private Double currentStock;
    private String demandTrend;   // ↑ HIGH DEMAND | → STABLE | ↓ LOW DEMAND
    private String stockStatus;   // SUFFICIENT | INSUFFICIENT
}
