package com.users.application.dto;


import com.users.domain.Role;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String codiceFiscale;
    private String nome;
    private String cognome;
    private Set<Role> roles;
}
