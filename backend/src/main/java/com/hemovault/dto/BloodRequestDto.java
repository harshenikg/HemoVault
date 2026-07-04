package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodRequest;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class BloodRequestDto {
    @NotBlank private String hospitalName;
    private String hospitalContact;
    @NotNull private BloodGroup bloodGroup;
    @NotNull @DecimalMin("0.5") private Double unitsRequired;
    private LocalDate requiredByDate;
    private BloodRequest.Priority priority;
    private String patientName;
    @NotBlank
    @Size(min = 20, message = "Clinical reason must be at least 20 characters with specific medical details")
    private String clinicalReason;
    private String attendingDoctor;
}
