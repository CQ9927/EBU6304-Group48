/**
 * =============================================================================
 * User Data Model - System Account Information
 * =============================================================================
 * 
 * OWNER: zzzskl (231226772)
 * CREATED: 2026-04-07
 * VERSION: 1.0
 * STATUS: 🎯 CORE BUSINESS CODE - Data Model
 * 
 * =============================================================================
 * 
 * 📋 ENTITY DESCRIPTION:
 * Represents a user account in the TA Recruitment System with authentication
 * credentials and role-based permissions. Serves as the foundation for all
 * user interactions and access control throughout the application.
 * 
 * =============================================================================
 * 
 * 🏷️ FIELD SPECIFICATIONS:
 * 
 * 1. IDENTIFIERS:
 *    - userId (String): Unique identifier for the user (format: U001)
 *    - username (String): Login username (required, unique)
 *      * Used for authentication and display purposes
 *      * Should follow username conventions (alphanumeric, no spaces)
 * 
 * 2. SECURITY FIELDS:
 *    - passwordHash (String): Hashed password for authentication (required)
 *      * Format: "algorithm:hash" (e.g., "sha256:abc123...")
 *      * Never store plain text passwords
 *      * Should use strong hashing algorithm (SHA-256 or better)
 * 
 * 3. ROLE AND PERMISSIONS:
 *    - role (String): User's role in the system (required)
 *      * Valid values: "TA" (Teaching Assistant), "MO" (Module Organizer), "ADMIN"
 *      * Determines access rights and available features:
 *        - TA: Can create profile, apply for jobs, upload CV
 *        - MO: Can post jobs, review applications, select TAs
 *        - ADMIN: System administration, user management
 * 
 * 4. METADATA:
 *    - createdAt (String): ISO 8601 timestamp of account creation (required)
 *      * Used for account age tracking and auditing
 * 
 * =============================================================================
 * 
 * ⚠️ DATA INTEGRITY CONSTRAINTS:
 * 
 * 1. REQUIRED FIELDS:
 *    - userId: Must be unique across all users
 *    - username: Must be unique, not null, not empty
 *    - passwordHash: Must not be null or empty
 *    - role: Must be one of defined role values
 *    - createdAt: Cannot be null
 * 
 * 2. SECURITY CONSTRAINTS:
 *    - passwordHash must be properly hashed (not reversible)
 *    - User data should never be exposed in API responses (except userId/username)
 *    - Role changes should be audited and restricted
 * 
 * 3. BUSINESS RULES:
 *    - Username should be case-insensitive for login
 *    - Password should meet complexity requirements (enforced at registration)
 *    - Role determines which entities user can create/access
 *    - User deletion should cascade to related entities (profile, applications)
 * 
 * 4. FORMAT CONSTRAINTS:
 *    - userId format: U followed by 3 digits (U001, U002, etc.)
 *    - Timestamps: ISO 8601 format
 *    - Role values: Uppercase predefined constants
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. SECURITY ENHANCEMENTS (Priority: High):
 *    - Add password salt field for stronger hashing
 *    - Implement password expiration and history
 *    - Add multi-factor authentication fields
 *    - Track failed login attempts and lockout
 * 
 * 2. USER PROFILE EXTENSIONS (Priority: Medium):
 *    - Add email field for notifications
 *    - Add contact information (phone, department)
 *    - Add preferences (notification settings, language)
 *    - Add lastLogin timestamp for activity tracking
 * 
 * 3. ROLE MANAGEMENT (Priority: Low):
 *    - Support for hierarchical roles
 *    - Add role expiration dates
 *    - Implement role-based access control (RBAC)
 *    - Add department/group affiliations
 * 
 * 4. COMPLIANCE AND AUDITING (Priority: Medium):
 *    - Add GDPR compliance fields (consent, data retention)
 *    - Track account modification history
 *    - Add account status (ACTIVE, SUSPENDED, DELETED)
 *    - Implement soft delete with archival
 * 
 * =============================================================================
 * 
 * 📊 JSON REPRESENTATION EXAMPLE:
 * {
 *   "userId": "U001",
 *   "username": "zzzskl",
 *   "passwordHash": "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
 *   "role": "TA",
 *   "createdAt": "2026-03-27T10:00:00Z"
 * }
 * 
 * =============================================================================
 * 
 * 📁 RELATIONSHIPS:
 *   User (1) ───── (0..1) Profile (if role="TA")
 *   User (1) ───── (0..*) Job (if role="MO", as postedByUserId)
 *   User (1) ───── (0..*) Application (if role="TA", as applicantUserId)
 *   User (1) ───── (0..*) Selection (if role="MO", as reviewerUserId)
 * 
 * =============================================================================
 */
package com.ebu6304.group48.model;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String role;
    private String createdAt;

    // Constructors
    public User() {
    }

    public User(String userId, String username, String passwordHash, String role, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role='" + role + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}