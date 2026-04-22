package com.sian.noteshare.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object representing the payload for a user login request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
