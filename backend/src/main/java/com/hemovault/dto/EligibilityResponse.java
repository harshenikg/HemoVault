package com.hemovault.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EligibilityResponse {
    private Boolean eligible;
    private String status;
    private List<String> reasons;
}
