package com.hemovault.dto;

import com.hemovault.model.User;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private User.Role role;
    private Boolean isAdmin;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
