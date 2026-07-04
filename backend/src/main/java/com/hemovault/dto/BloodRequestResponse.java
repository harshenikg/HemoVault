package com.hemovault.dto;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodRequest;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodRequestResponse {
    private Long id;
    private String hospitalName;
    private String hospitalContact;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Double unitsRequired;
    private Double unitsIssued;
    private LocalDate requestDate;
    private LocalDate requiredByDate;
    private BloodRequest.Priority priority;
    private String patientName;
    private String clinicalReason;
    private String attendingDoctor;
    private BloodRequest.RequestStatus status;
    private String adminNotes;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
