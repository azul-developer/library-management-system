package com.liz.library.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request used to authenticate a user")
public class LoginRequest {

    @Schema(
            description = "User email address",
            example = "admin@test.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )

    @NotBlank(message = "Email is required.")
    @Email
    private String email;

    @Schema(
            description = "User password",
            example = "Password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "password is required.")
    private String password;
}