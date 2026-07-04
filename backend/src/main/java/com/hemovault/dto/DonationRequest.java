package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.Donation;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class DonationRequest {
    @NotNull private Long donorId;
    private Double unitsDonated;
    @NotNull private LocalDate donationDate;
    private Double hemoglobinAtDonation;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private String notes;
}
