package com.hemovault.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatResponse {
    private String response;
    private String sessionId;
    private LocalDateTime timestamp;
}
