package com.hemovault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hemovault.model.BloodGroup;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonorResponse {
    private Long id;
    private String name;
    private Integer age;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private String city;
    private Boolean isEligible;
    private String eligibilityReason;
    private LocalDate lastDonationDate;
    private LocalDateTime createdAt;
    // Admin-only fields (null for non-admins)
    private String contactNumber;
    private String email;
    private Double weight;
    private Double hemoglobin;
    private String bloodPressure;
    private String address;
}
