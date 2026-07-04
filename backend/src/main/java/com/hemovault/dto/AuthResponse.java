package com.hemovault.dto;

import com.hemovault.model.User;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private User.Role role;
    private Boolean isAdmin;
}
