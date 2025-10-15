package com.users.application;

import com.users.application.dto.*;
import com.users.application.mapper.UserMapper;
import com.users.domain.User;
import com.users.domain.NotFoundException;
import com.users.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        User u = repo.findById(id).orElseThrow(() -> new NotFoundException("Utente non trovato"));
        return mapper.toResponse(u);
    }

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        if (repo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("Email già in uso");
        if (repo.existsByCodiceFiscale(req.getCodiceFiscale())) throw new IllegalArgumentException("Codice fiscale già in uso");
        User saved = repo.save(mapper.toEntity(req));
        return mapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest req) {
        User u = repo.findById(id).orElseThrow(() -> new NotFoundException("Utente non trovato"));
        mapper.update(u, req); // email non toccata
        return mapper.toResponse(u);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Utente non trovato");
        repo.deleteById(id);
    }
}
