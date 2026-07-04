package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodInventory;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventorySummary {
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double totalUnits;
    private Double thresholdUnits;
    private Double criticalUnits;
    private String stockStatus; // ADEQUATE, LOW, CRITICAL
}
