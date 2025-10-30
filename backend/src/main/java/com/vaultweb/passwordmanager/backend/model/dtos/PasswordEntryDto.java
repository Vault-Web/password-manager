package com.vaultweb.passwordmanager.backend.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordEntryDto {

    private Long id;

    @NotBlank(message = "Name (service/site) is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String url;

    @Size(max = 500)
    private String notes;
}
