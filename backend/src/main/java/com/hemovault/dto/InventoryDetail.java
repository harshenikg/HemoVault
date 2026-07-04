package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodInventory;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryDetail {
    private Long id;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double unitsAvailable;
    private LocalDate collectionDate;
    private LocalDate expiryDate;
    private String batchNumber;
    private String storageLocation;
    private BloodInventory.InventoryStatus status;
    private Long daysToExpiry;
    private LocalDateTime createdAt;
}
