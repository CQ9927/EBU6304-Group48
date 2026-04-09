/**
 * =============================================================================
 * Job Data Model - TA Position Information
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
 * Represents a Teaching Assistant job posting created by Module Organizers (MOs).
 * Contains job details, requirements, scheduling information, and status.
 * Used for job browsing, application processing, and match calculations.
 * 
 * =============================================================================
 * 
 * 🏷️ FIELD SPECIFICATIONS:
 * 
 * 1. IDENTIFIERS:
 *    - jobId (String): Unique identifier for the job (format: J001)
 *    - postedByUserId (String): ID of MO who created the job (format: U010)
 * 
 * 2. JOB DETAILS:
 *    - title (String): Job title/description (required)
 *      * Examples: "CS101 Tutorial Assistant", "Final Exam Invigilator"
 *    - type (String): Job type classification (required)
 *      * Valid values: "MODULE" (course assistance), "INVIGILATION" (exam supervision)
 *    - semester (String): Academic semester for the position (required)
 *      * Format: YYYY_SEASON (e.g., "2026_SPRING", "2025_FALL")
 * 
 * 3. SCHEDULING AND CAPACITY:
 *    - schedule (String): Time schedule for the job (required)
 *      * Format: DAY_HH_HH (e.g., "WED_18_20", "FRI_09_12")
 *      * Represents weekly time commitment
 *    - capacity (Integer): Number of TAs needed for this position (required)
 *      * Must be positive integer
 *      * Used to track remaining openings
 * 
 * 4. REQUIREMENTS:
 *    - requiredSkills (List<String>): Skills required for the position
 *      * Examples: ["Java", "Teaching", "Algorithms"]
 *      * Used for match score calculations with TA profiles
 *      * Empty list indicates no specific skill requirements
 * 
 * 5. STATUS AND METADATA:
 *    - status (String): Current status of the job (required)
 *      * Valid values: "OPEN" (accepting applications), "CLOSED" (filled or expired)
 *      * Default: "OPEN" when created
 *    - createdAt (String): ISO 8601 timestamp of job creation (required)
 * 
 * =============================================================================
 * 
 * ⚠️ DATA INTEGRITY CONSTRAINTS:
 * 
 * 1. REQUIRED FIELDS:
 *    - jobId: Must be unique across all jobs
 *    - title, type, semester, schedule, status: Cannot be null or empty
 *    - capacity: Must be positive integer (> 0)
 *    - postedByUserId: Must reference an existing User with role="MO"
 * 
 * 2. FORMAT CONSTRAINTS:
 *    - ID formats: Follow project naming conventions
 *    - Timestamps: ISO 8601 format
 *    - Enumerated values: type and status must use defined constants
 * 
 * 3. BUSINESS RULES:
 *    - Only users with role="MO" can create jobs
 *    - OPEN jobs are visible to TAs for applications
 *    - CLOSED jobs are not displayed in job browsing
 *    - Capacity should be decreased as TAs are selected
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. FIELD ADDITIONS (Priority: Medium):
 *    - description: Detailed job description
 *    - location: Physical location (room number, building)
 *    - hourlyRate: Compensation information
 *    - applicationDeadline: Cutoff date for applications
 *    - prerequisites: Course prerequisites for the position
 * 
 * 2. ENHANCED VALIDATION (Priority: High):
 *    - Add @Min(1) constraint for capacity
 *    - Implement custom validators for schedule format
 *    - Add business logic for status transitions
 *    - Validate skill names against predefined skill catalog
 * 
 * 3. RELATIONSHIP MANAGEMENT (Priority: Medium):
 *    - Add reference to selected Applications
 *    - Track remaining capacity vs applied count
 *    - Add cancellation reason for CLOSED jobs
 * 
 * 4. INTERNATIONALIZATION (Priority: Low):
 *    - Multi-language job titles and descriptions
 *    - Timezone-aware scheduling
 *    - Localized skill names
 * 
 * =============================================================================
 * 
 * 📊 JSON REPRESENTATION EXAMPLE:
 * {
 *   "jobId": "J001",
 *   "title": "CS101 Tutorial Assistant",
 *   "type": "MODULE",
 *   "semester": "2026_SPRING",
 *   "schedule": "WED_18_20",
 *   "capacity": 2,
 *   "requiredSkills": ["Java", "Teaching", "Algorithms"],
 *   "postedByUserId": "U010",
 *   "status": "OPEN",
 *   "createdAt": "2026-03-27T11:00:00Z"
 * }
 * 
 * =============================================================================
 * 
 * 📁 RELATIONSHIPS:
 *   Job (1) ───── (0..*) Application
 *   Job (postedByUserId) ───── (1) User (role="MO")
 *   Job (requiredSkills) ───── (0..*) Skill Catalog
 * 
 * =============================================================================
 */
package com.ebu6304.group48.model;

import java.util.List;

public class Job {
    private String jobId;
    private String title;
    private String type;
    private String semester;
    private String schedule;
    private Integer capacity;
    private List<String> requiredSkills;
    private String postedByUserId;
    private String status;
    private String createdAt;

    // Constructors
    public Job() {
    }

    public Job(String jobId, String title, String type, String semester, String schedule,
               Integer capacity, List<String> requiredSkills, String postedByUserId,
               String status, String createdAt) {
        this.jobId = jobId;
        this.title = title;
        this.type = type;
        this.semester = semester;
        this.schedule = schedule;
        this.capacity = capacity;
        this.requiredSkills = requiredSkills;
        this.postedByUserId = postedByUserId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getPostedByUserId() {
        return postedByUserId;
    }

    public void setPostedByUserId(String postedByUserId) {
        this.postedByUserId = postedByUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", semester='" + semester + '\'' +
                ", schedule='" + schedule + '\'' +
                ", capacity=" + capacity +
                ", requiredSkills=" + requiredSkills +
                ", postedByUserId='" + postedByUserId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}