/**
 * =============================================================================
 * Application Data Model - TA Job Application
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
 * Represents a Teaching Assistant's application for a specific job position.
 * Tracks application status, match metrics, and review information.
 * Serves as the central entity for the application review workflow.
 * 
 * =============================================================================
 * 
 * 🏷️ FIELD SPECIFICATIONS:
 * 
 * 1. IDENTIFIERS:
 *    - applicationId (String): Unique identifier for the application (format: A001)
 *    - jobId (String): Foreign key to Job entity (format: J001)
 *    - applicantUserId (String): Foreign key to User entity (format: U001)
 * 
 * 2. MATCH METRICS:
 *    - matchScore (Integer): Calculated compatibility score between TA and job
 *      * Range: 0-100 (percentage)
 *      * Calculation: (matching skills / total required skills) × 100
 *      * Used for ranking and decision making
 *    - missingSkills (List<String>): Skills required by job but not possessed by TA
 *      * Examples: ["Teaching", "Algorithms"] (if TA lacks these)
 *      * Used for skill gap analysis and feedback
 * 
 * 3. APPLICATION STATUS:
 *    - status (String): Current state of the application (required)
 *      * Valid values:
 *        - "SUBMITTED": Initial state after TA applies
 *        - "UNDER_REVIEW": Being reviewed by MO
 *        - "SELECTED": TA has been chosen for the position
 *        - "REJECTED": Application was not successful
 *      * Status transitions should follow workflow rules
 * 
 * 4. CONTENT AND METADATA:
 *    - note (String): Optional note from TA or MO
 *      * TA: Motivation statement, additional qualifications
 *      * MO: Feedback, interview notes, selection rationale
 *    - createdAt (String): ISO 8601 timestamp of application submission
 *    - updatedAt (String): ISO 8601 timestamp of last status change
 * 
 * =============================================================================
 * 
 * ⚠️ DATA INTEGRITY CONSTRAINTS:
 * 
 * 1. REQUIRED FIELDS:
 *    - applicationId: Must be unique across all applications
 *    - jobId, applicantUserId: Must reference existing entities
 *    - status: Cannot be null, must use defined status values
 *    - matchScore: Must be between 0 and 100 (inclusive)
 *    - createdAt, updatedAt: Cannot be null
 * 
 * 2. UNIQUENESS CONSTRAINTS:
 *    - Combination of jobId and applicantUserId must be unique
 *      (TA cannot apply to same job multiple times)
 * 
 * 3. BUSINESS RULES:
 *    - Only TAs can submit applications (role="TA")
 *    - Only OPEN jobs can receive applications
 *    - Match score should be calculated at application time
 *    - Status updates should be logged and timestamped
 * 
 * 4. REFERENTIAL INTEGRITY:
 *    - jobId must reference a valid Job entity
 *    - applicantUserId must reference a valid User entity with role="TA"
 *    - Deleting a Job should cascade delete related Applications
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. FIELD ADDITIONS (Priority: Medium):
 *    - interviewDate: Scheduled interview timestamp
 *    - interviewNotes: Notes from interview process
 *    - rankingPosition: Position in candidate ranking
 *    - withdrawalReason: If TA withdraws application
 *    - autoRejectReason: If automatically rejected by system
 * 
 * 2. WORKFLOW ENHANCEMENTS (Priority: High):
 *    - Add intermediate statuses (SHORTLISTED, INTERVIEWED)
 *    - Implement status transition validation
 *    - Add application versioning for updates
 *    - Support for application attachments
 * 
 * 3. ANALYTICS INTEGRATION (Priority: Low):
 *    - Track application view counts by MO
 *    - Calculate average time in each status
 *    - Success rate analytics by skill match
 *    - Predictive analytics for selection likelihood
 * 
 * 4. NOTIFICATION SUPPORT (Priority: Medium):
 *    - Status change notification triggers
 *    - Interview invitation tracking
 *    - Deadline reminders for MO review
 *    - Application follow-up prompts
 * 
 * =============================================================================
 * 
 * 📊 JSON REPRESENTATION EXAMPLE:
 * {
 *   "applicationId": "A001",
 *   "jobId": "J001",
 *   "applicantUserId": "U001",
 *   "matchScore": 84,
 *   "missingSkills": ["Teaching"],
 *   "status": "SUBMITTED",
 *   "note": "Sample note",
 *   "createdAt": "2026-03-27T12:00:00Z",
 *   "updatedAt": "2026-03-27T12:00:00Z"
 * }
 * 
 * =============================================================================
 * 
 * 📁 RELATIONSHIPS:
 *   Application (jobId) ───── (1) Job
 *   Application (applicantUserId) ───── (1) User (role="TA")
 *   Application (1) ───── (0..1) Selection (decision record)
 * 
 * =============================================================================
 */
package com.ebu6304.group48.model;

import java.util.List;

public class Application {
    private String applicationId;
    private String jobId;
    private String applicantUserId;
    private Integer matchScore;
    private List<String> missingSkills;
    private String status;
    private String note;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Application() {
    }

    public Application(String applicationId, String jobId, String applicantUserId,
                      Integer matchScore, List<String> missingSkills, String status,
                      String note, String createdAt, String updatedAt) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.applicantUserId = applicantUserId;
        this.matchScore = matchScore;
        this.missingSkills = missingSkills;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(String applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationId='" + applicationId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", applicantUserId='" + applicantUserId + '\'' +
                ", matchScore=" + matchScore +
                ", missingSkills=" + missingSkills +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}