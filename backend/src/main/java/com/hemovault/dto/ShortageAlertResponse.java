package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.ShortageAlert;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShortageAlertResponse {
    private Long id;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double currentUnits;
    private Double thresholdUnits;
    private ShortageAlert.AlertType alertType;
    private LocalDateTime createdAt;
}
