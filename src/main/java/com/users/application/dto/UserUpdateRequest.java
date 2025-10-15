package com.users.application.dto;

import com.users.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    @NotBlank private String username;
    @NotBlank private String nome;
    @NotBlank private String cognome;
    @NotEmpty private Set<Role> roles;
}
