package com.hemovault.dto;

import com.hemovault.model.PermissionRequest;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PermissionRequestResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String organization;
    private String reason;
    private PermissionRequest.PermStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
