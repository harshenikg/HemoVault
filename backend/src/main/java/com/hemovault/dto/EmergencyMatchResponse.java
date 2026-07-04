package com.hemovault.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyMatchResponse {
    private List<String> compatibleGroups;
    private List<DonorResponse> matchedDonors;
    private Integer totalMatchesFound;
    private Double stockAvailable;
    private String stockStatus; // SUFFICIENT | PARTIAL | UNAVAILABLE
}
