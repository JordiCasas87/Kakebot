package com.jordi.kakebot.user.model;

import com.jordi.kakebot.user.enums.UserProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserProvider provider;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "external_id", length = 120)
    private String externalId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    //Constructor vacio para JPA.
    protected User() {
    }


     // Constructor  para crear usuarios desde la aplicacion.
    public User(UserProvider provider, String username, String passwordHash, String externalId, LocalDateTime createdAt) {
        this.provider = provider;
        this.username = username;
        this.passwordHash = passwordHash;
        this.externalId = externalId;
        this.createdAt = createdAt;
    }

    // getter setter

    public Long getId() {
        return id;
    }

    public UserProvider getProvider() {
        return provider;
    }

    public void setProvider(UserProvider provider) {
        this.provider = provider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
