package com.hemovault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class ChatRequest {
    @NotBlank private String message;
    private String sessionId;
}
