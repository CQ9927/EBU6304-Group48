package com.ebu6304.group48.model;

/**
 * Mirrors {@code users.json} in docs/DATA_SCHEMA.md
 */
public class User {

    private String userId;
    private String username;
    private String passwordHash;
    private String role;
    private String createdAt;

    public User() {
    }

    public User(String userId, String username, String passwordHash, String role, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{"
                + "userId='" + userId + '\''
                + ", username='" + username + '\''
                + ", passwordHash='" + passwordHash + '\''
                + ", role='" + role + '\''
                + ", createdAt='" + createdAt + '\''
                + '}';
    }
}
