package com.users.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_users_email", columnNames="email"),
                @UniqueConstraint(name="uk_users_cf",    columnNames="codice_fiscale")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false)
    private String username;

    @Column(nullable=false, updatable=false)
    @Setter(AccessLevel.NONE)     //email immutabile
    private String email;

    @Column(name="codice_fiscale", nullable=false)
    private String codiceFiscale;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false)
    private String cognome;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable=false)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void setEmailForCreation(String email) {
        if (this.id != null) throw new IllegalStateException("Email non modificabile dopo la creazione");
        this.email = email;
    }
}
