package com.hemovault.dto;

import com.hemovault.model.PermissionRequest;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class PermissionRequestDto {
    @NotBlank private String organization;
    @NotBlank @Size(min = 30, message = "Reason must be at least 30 characters")
    private String reason;
}
