package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.Donation;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DonationResponse {
    private Long id;
    private Long donorId;
    private String donorName;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double unitsDonated;
    private LocalDate donationDate;
    private Double hemoglobinAtDonation;
    private Donation.DonationStatus status;
    private String deferReason;
    private String stockImpact;
    private LocalDateTime createdAt;
}
