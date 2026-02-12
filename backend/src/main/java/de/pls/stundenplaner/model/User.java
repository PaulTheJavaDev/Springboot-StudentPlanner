package de.pls.stundenplaner.model;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("all")
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, updatable = false)
    @JsonProperty("useruuid")
    @NotNull
    private UUID userUUID;

    @Column(unique = true, nullable = false)
    @NotNull
    private String username;

    @Column(nullable = false)
    @JsonProperty("password_hash")
    @NotNull
    private String password_hash;

    @Column(unique = true)
    private UUID sessionID;

    public User(
            @NonNull final String username,
            @NonNull final String password_hash
    ) throws EmptyUsernameException {
        setUserUUID(UUID.randomUUID());
        setUsername(username);
        this.password_hash = password_hash;
    }

    void setUsername(@NonNull final String username) throws EmptyUsernameException {
        if (username.isEmpty()) {
            throw new EmptyUsernameException();
        }
        this.username = username;
    }
}