package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodRequest;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class EmergencyMatchRequest {
    @NotNull private BloodGroup bloodGroup;
    private String city;
    private Double unitsRequired;
    private BloodRequest.Priority priority;
}
