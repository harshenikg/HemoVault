package com.hemovault.dto;

import com.hemovault.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Size(min=3, max=50) private String username;
    @NotBlank @Size(min=6)         private String password;
    @NotBlank @Email               private String email;
    private String fullName;
    private User.Role role;
}
