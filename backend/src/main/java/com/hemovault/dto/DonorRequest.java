package com.hemovault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hemovault.model.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class DonorRequest {
    @NotBlank private String name;
    @NotNull @Min(1) @Max(120) private Integer age;
    @NotNull private BloodGroup bloodGroup;
    private String contactNumber;
    private String email;
    @NotBlank private String city;
    private String address;
    private Double weight;
    private Double hemoglobin;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Boolean isPregnant;
    private Boolean hasRecentIllness;
    private LocalDate lastDonationDate;
}
