package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodInventory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventoryRequest {
    @NotNull private BloodGroup bloodGroup;
    @NotNull @DecimalMin("0.5") private Double unitsAvailable;
    private LocalDate collectionDate;
    private LocalDate expiryDate;
    private String batchNumber;
    private String storageLocation;
}
