package com.liz.library.domain.model;

import com.liz.library.domain.valueobject.Email;
import lombok.Getter;

import java.util.UUID;

@Getter
public class User {

    private final UUID id;

    private final String firstName;

    private final String lastName;

    private final Email email;

    private final String passwordHash;

    private final Role role;

    private User(
            UUID id,
            String firstName,
            String lastName,
            Email email,
            String passwordHash,
            Role role) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static User create(
            String firstName,
            String lastName,
            Email email,
            String passwordHash,
            Role role) {

        return new User(
                UUID.randomUUID(),
                firstName,
                lastName,
                email,
                passwordHash,
                role
        );
    }

        public static User restore(
            UUID id,
            String firstName,
            String lastName,
            Email email,
            String passwordHash,
            Role role) {

        return new User(
            id,
            firstName,
            lastName,
            email,
            passwordHash,
            role
        );
        }
}