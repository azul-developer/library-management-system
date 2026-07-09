package com.liz.library.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a new user")
public class CreateUserRequest {

    @Schema(
            description = "User first name",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{user.firstName.required}")
    private String firstName;

    @Schema(
            description = "User last name",
            example = "Doe"
    )
    private String lastName;

    @Schema(
            description = "User email address",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    private String email;

    @Schema(
            description = "User password",
            example = "MySecurePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{user.password.required}")
    private String password;

    @Schema(
            description = "User role. Defaults to USER if not provided.",
            example = "USER",
            allowableValues = {"USER", "ADMIN"}
    )
    private String roleName;
}