package com.users.it.web;

import com.users.Application;
import com.users.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String createUser(String username, String email, String cf, String nome, String cognome, Set<Role> roles) throws Exception {
        var body = Map.of(
                "username", username,
                "email", email,
                "codiceFiscale", cf,
                "nome", nome,
                "cognome", cognome,
                "roles", roles
        );
        var res = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var node = om.readTree(res);
        return node.get("id").asText();
    }

    @Test
    @DisplayName("3) Aggiungere un utente con ruoli → 201, body con id e campi")
    void create_user_ok() throws Exception {
        createUser(
                "mario", "mario.rossi@example.com", "RSSMRA85T10A562S",
                "Mario", "Rossi", Set.of(Role.DEVELOPER, Role.REPORTER)
        );
    }

    @Test
    @DisplayName("Validazione CF invalido → 400 con errori")
    void create_invalid_cf_400() throws Exception {
        var body = Map.of(
                "username", "anna",
                "email", "anna.bianchi@example.com",
                "codiceFiscale", "CF-NON-VAL",   // invalido
                "nome", "Anna",
                "cognome", "Bianchi",
                "roles", Set.of(Role.OWNER)
        );
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.codiceFiscale", containsString("non valido")));
    }

    @Test
    @DisplayName("Email duplicata → 409 CONFLICT")
    void create_duplicate_email_409() throws Exception {
        createUser("m1", "dup@example.com", "RSSMRA85T10A562S", "M1", "R1", Set.of(Role.DEVELOPER));
        var body = Map.of(
                "username", "m2",
                "email", "dup@example.com", // duplicata
                "codiceFiscale", "BRSLGU80A01H501Z",
                "nome", "M2",
                "cognome", "R2",
                "roles", Set.of(Role.REPORTER)
        );
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message", containsString("Email già in uso")));
    }

    @Test
    @DisplayName("1) Lista utenti paginata → 200 e struttura Page")
    void list_users_ok() throws Exception {
        // semina dati
        createUser("u1", "u1@example.com", "RSSMRA85T10A562S", "U1", "C1", Set.of(Role.OPERATOR));
        createUser("u2", "u2@example.com", "BRSLGU80A01H501Z", "U2", "C2", Set.of(Role.MAINTAINER));

        mvc.perform(get("/api/v1/users?page=0&size=1&sort=nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("2) Dettaglio utente esistente → 200")
    void get_user_ok() throws Exception {
        String id = createUser("lucia", "lucia@example.com", "RSSLCI85T10A562S", "Lucia", "Verdi",
                Set.of(Role.DEVELOPER));
        mvc.perform(get("/api/v1/users/{id}", UUID.fromString(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("lucia@example.com"));
    }

    @Test
    @DisplayName("2) Dettaglio utente inesistente → 404")
    void get_user_not_found_404() throws Exception {
        mvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("4) Modifica utente/ruoli → 200 e campi aggiornati (email invariata)")
    void update_user_ok() throws Exception {
        String id = createUser("paolo", "paolo@example.com", "PLAPLA85T10A562S", "Paolo", "Blu",
                Set.of(Role.REPORTER));

        var updateBody = Map.of(
                "username", "paoloX",
                "nome", "PaoloX",
                "cognome", "BluX",
                "roles", Set.of(Role.OWNER, Role.DEVELOPER)
        );

        mvc.perform(put("/api/v1/users/{id}", UUID.fromString(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("paoloX"))
                .andExpect(jsonPath("$.nome").value("PaoloX"))
                .andExpect(jsonPath("$.cognome").value("BluX"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("OWNER", "DEVELOPER")))
                .andExpect(jsonPath("$.email").value("paolo@example.com")); // non cambia
    }

    @Test
    @DisplayName("5) Cancellazione utente → 204 e poi 404 al GET")
    void delete_user_ok() throws Exception {
        String id = createUser("luca", "luca@example.com", "LCAAAX85T10A562S", "Luca", "Gialli",
                Set.of(Role.OPERATOR));

        mvc.perform(delete("/api/v1/users/{id}", UUID.fromString(id)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/users/{id}", UUID.fromString(id)))
                .andExpect(status().isNotFound());
    }
}
