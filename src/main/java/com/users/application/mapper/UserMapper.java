package com.users.application.mapper;


import com.users.application.dto.*;
import com.users.domain.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) //set via @AfterMapping
    User toEntity(UserCreateRequest req);

    UserResponse toResponse(User user);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "username", source = "req.username"),
            @Mapping(target = "nome",     source = "req.nome"),
            @Mapping(target = "cognome",  source = "req.cognome"),
            @Mapping(target = "roles",    source = "req.roles")
    })
    void update(@MappingTarget User target, UserUpdateRequest req);

    //Imposta l'email solo alla creazione
    @AfterMapping
    default void setEmailOnCreate(UserCreateRequest req, @MappingTarget User user) {
        user.setEmailForCreation(req.getEmail());
    }
}
