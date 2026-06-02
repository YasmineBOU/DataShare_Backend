package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$")
    private String password;
}
