/**
 * =============================================================================
 * Profile Data Model - TA Personal Information
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
 * Represents a Teaching Assistant's personal and professional profile information.
 * This model stores TA's personal details, skills, availability, and CV reference.
 * Used throughout the application for job matching, application processing, and
 * user management.
 * 
 * =============================================================================
 * 
 * 🏷️ FIELD SPECIFICATIONS:
 * 
 * 1. IDENTIFIERS:
 *    - profileId (String): Unique identifier for the profile (format: P001)
 *    - userId (String): Foreign key linking to User entity (format: U001)
 * 
 * 2. PERSONAL INFORMATION:
 *    - name (String): TA's full name (required)
 *    - email (String): TA's email address (required, should be validated)
 *    - major (String): TA's academic major/program (required)
 * 
 * 3. PROFESSIONAL ATTRIBUTES:
 *    - skills (List<String>): List of skills possessed by the TA
 *      * Examples: ["Java", "Python", "Teaching", "Algorithms"]
 *      * Used for job matching calculations
 *    - availability (List<String>): Available time slots for TA work
 *      * Format: DAY_HH_HH (e.g., "MON_14_16", "WED_18_20")
 *      * Used for schedule conflict detection
 * 
 * 4. DOCUMENT MANAGEMENT:
 *    - cvFileName (String): Reference to uploaded CV file
 *      * Format: CV_{userId}_{timestamp}.{ext}
 *      * Stored in {dataDir}/cvs/ directory
 *      * Nullable (TA may not have uploaded CV)
 * 
 * 5. METADATA:
 *    - notes (String): Free-text notes or additional information
 *    - createdAt (String): ISO 8601 timestamp of profile creation
 *    - updatedAt (String): ISO 8601 timestamp of last update
 * 
 * =============================================================================
 * 
 * ⚠️ DATA INTEGRITY CONSTRAINTS:
 * 
 * 1. REQUIRED FIELDS:
 *    - profileId: Must be unique across all profiles
 *    - userId: Must reference an existing User entity
 *    - name, email, major: Cannot be null or empty
 * 
 * 2. FORMAT CONSTRAINTS:
 *    - Email: Should follow email format (validation recommended)
 *    - Timestamps: ISO 8601 format (e.g., "2026-03-27T10:30:00Z")
 *    - ID formats: Follow project naming conventions
 * 
 * 3. BUSINESS RULES:
 *    - One-to-one relationship with User entity
 *    - Skills list should not contain duplicates
 *    - Availability slots should not overlap (validation needed)
 *    - CV file reference must point to existing file if not null
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. FIELD ADDITIONS (Priority: Medium):
 *    - experienceYears: Number of years of TA experience
 *    - gpa: Academic performance indicator
 *    - preferredCourses: List of preferred course codes
 *    - references: Contact information for references
 * 
 * 2. VALIDATION ENHANCEMENTS (Priority: High):
 *    - Add @NotNull annotations for required fields
 *    - Implement custom validators for email and ID formats
 *    - Add size constraints for lists (max skills, availability slots)
 * 
 * 3. PERFORMANCE OPTIMIZATIONS (Priority: Low):
 *    - Add indexing for frequently queried fields
 *    - Consider lazy loading for large text fields (notes)
 *    - Add caching for frequently accessed profiles
 * 
 * 4. INTERNATIONALIZATION (Priority: Low):
 *    - Add language preferences field
 *    - Support for non-Latin character names
 *    - Timezone field for availability scheduling
 * 
 * =============================================================================
 * 
 * 📊 JSON REPRESENTATION EXAMPLE:
 * {
 *   "profileId": "P001",
 *   "userId": "U001",
 *   "name": "Zhang San",
 *   "email": "ta48@school.edu",
 *   "major": "Software Engineering",
 *   "skills": ["Java", "Python", "Web Development"],
 *   "availability": ["MON_14_16", "WED_18_20", "FRI_10_12"],
 *   "notes": "Experienced TA for programming courses",
 *   "cvFileName": "CV_U001_20260327103000.pdf",
 *   "updatedAt": "2026-03-27T10:30:00Z",
 *   "createdAt": "2026-03-27T10:30:00Z"
 * }
 * 
 * =============================================================================
 * 
 * 📁 RELATIONSHIPS:
 *   Profile (1) ───── (1) User
 *   Profile (1) ───── (0..*) Application
 *   Profile (cvFileName) ───── (1) CV File
 * 
 * =============================================================================
 */
package com.ebu6304.group48.model;

import java.util.List;

public class Profile {
    private String profileId;
    private String userId;
    private String name;
    private String email;
    private String major;
    private List<String> skills;
    private List<String> availability;
    private String notes;
    private String cvFileName;
    private String updatedAt;
    private String createdAt;

    // Constructors
    public Profile() {
    }

    public Profile(String profileId, String userId, String name, String email, String major,
                   List<String> skills, List<String> availability, String notes, 
                   String cvFileName, String updatedAt, String createdAt) {
        this.profileId = profileId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.major = major;
        this.skills = skills;
        this.availability = availability;
        this.notes = notes;
        this.cvFileName = cvFileName;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getAvailability() {
        return availability;
    }

    public void setAvailability(List<String> availability) {
        this.availability = availability;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCvFileName() {
        return cvFileName;
    }

    public void setCvFileName(String cvFileName) {
        this.cvFileName = cvFileName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "profileId='" + profileId + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", major='" + major + '\'' +
                ", skills=" + skills +
                ", availability=" + availability +
                ", notes='" + notes + '\'' +
                ", cvFileName='" + cvFileName + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}