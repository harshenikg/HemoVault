package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class EligibilityRequest {
    private Integer age;
    private Double weight;
    private Double hemoglobin;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Boolean isPregnant;
    private Boolean hasRecentIllness;
    private LocalDate lastDonationDate;
    private Boolean onBloodThinners;
    private Boolean recentTattoo;
}
