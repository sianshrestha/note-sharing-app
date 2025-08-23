package com.sian.noteshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 50, message = "Username must be less than 50 characters")
    private String username;

    @Size(max = 100, message = "Email must be less than 100 characters")
    @Email(message = "Email should be valid")
    private String email;
}
