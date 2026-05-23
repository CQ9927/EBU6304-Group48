package com.ebu6304.group48.model;

/**
 * Represents an MO invitation sent to a TA for a specific job.
 * TAs can accept (auto-creates an Application) or decline.
 *
 * <p>Status flow: PENDING → ACCEPTED | DECLINED | EXPIRED</p>
 *
 * <p>JSON example:</p>
 * <pre>
 * {
 *   "invitationId": "I-ABC12345",
 *   "jobId": "J001",
 *   "moUserId": "U-DEMO-MO",
 *   "taUserId": "U-DEMO-TA",
 *   "status": "PENDING",
 *   "createdAt": "2026-05-23T12:00:00Z",
 *   "updatedAt": "2026-05-23T12:00:00Z"
 * }
 * </pre>
 */
public class Invitation {
    private String invitationId;
    private String jobId;
    private String moUserId;
    private String taUserId;
    private String status;   // PENDING, ACCEPTED, DECLINED, EXPIRED
    private String createdAt;
    private String updatedAt;

    public Invitation() {
    }

    public Invitation(String invitationId, String jobId, String moUserId, String taUserId,
                      String status, String createdAt, String updatedAt) {
        this.invitationId = invitationId;
        this.jobId = jobId;
        this.moUserId = moUserId;
        this.taUserId = taUserId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getMoUserId() { return moUserId; }
    public void setMoUserId(String moUserId) { this.moUserId = moUserId; }

    public String getTaUserId() { return taUserId; }
    public void setTaUserId(String taUserId) { this.taUserId = taUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Invitation{" +
                "invitationId='" + invitationId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", moUserId='" + moUserId + '\'' +
                ", taUserId='" + taUserId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
