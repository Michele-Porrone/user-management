package com.users.demo;

import com.users.domain.Role;
import com.users.domain.User;
import com.users.infrastructure.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.util.Set;

@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {
    private final UserRepository repo;
    public DemoDataLoader(UserRepository repo) { this.repo = repo; }

    @Override public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(User.builder()
                    .username("mario")
                    .email("mario.rossi@example.com")
                    .codiceFiscale("RSSMRA85T10A562S")
                    .nome("Mario").cognome("Rossi")
                    .roles(Set.of(Role.DEVELOPER, Role.REPORTER))
                    .build());
            repo.save(User.builder()
                    .username("anna")
                    .email("anna.bianchi@example.com")
                    .codiceFiscale("BNCHNN85T10A562S")
                    .nome("Anna").cognome("Bianchi")
                    .roles(Set.of(Role.OWNER))
                    .build());
        }
    }
}
