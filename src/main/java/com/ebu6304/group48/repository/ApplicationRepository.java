package com.ebu6304.group48.repository;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Application;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JSON persistence for {@code applications.json}. Used by TA jobs, MO selection, and admin revoke.
 */
public class ApplicationRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Application>>() { }.getType();
    private static final Object FILE_LOCK = new Object();
    private static final Set<String> VALID_STATUSES = new HashSet<>(List.of(
            "SUBMITTED", "UNDER_REVIEW", "SELECTED", "REJECTED"
    ));

    private final Path applicationsFile;

    public ApplicationRepository(ServletContext context) {
        String dataDirectory = AppPaths.resolveDataDirectory(context);
        this.applicationsFile = Path.of(dataDirectory, "applications.json");
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(applicationsFile.getParent());
        if (!Files.exists(applicationsFile)) {
            Files.writeString(applicationsFile, "[]", StandardCharsets.UTF_8);
        }
    }

    private List<Application> readAllInternal() throws IOException {
        String json = Files.readString(applicationsFile, StandardCharsets.UTF_8);
        List<Application> apps = GSON.fromJson(json, LIST_TYPE);
        return apps != null ? new ArrayList<>(apps) : new ArrayList<>();
    }

    public List<Application> findAll() {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                applications.sort(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
                return new ArrayList<>(applications);
            } catch (IOException | RuntimeException e) {
                return new ArrayList<>();
            }
        }
    }

    public Application findById(String applicationId) {
        if (applicationId == null || applicationId.isBlank()) {
            return null;
        }
        return findAll().stream()
                .filter(app -> applicationId.equals(app.getApplicationId()))
                .findFirst()
                .orElse(null);
    }

    public List<Application> findByApplicantUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(app -> userId.equals(app.getApplicantUserId()))
                .collect(Collectors.toList());
    }

    public List<Application> findByJobId(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(app -> jobId.equals(app.getJobId()))
                .collect(Collectors.toList());
    }

    /**
     * Sets application to REJECTED with {@code adminRevoked=true} and audit line in note.
     * Only allowed for {@code SUBMITTED} or {@code UNDER_REVIEW}.
     */
    public boolean rejectByAdmin(String applicationId, String adminUserId) {
        if (applicationId == null || applicationId.isBlank()) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                Application target = null;
                int targetIndex = -1;
                for (int i = 0; i < applications.size(); i++) {
                    if (applicationId.equals(applications.get(i).getApplicationId())) {
                        target = applications.get(i);
                        targetIndex = i;
                        break;
                    }
                }
                if (target == null) {
                    return false;
                }
                String st = target.getStatus() != null ? target.getStatus().trim().toUpperCase() : "";
                if ("REJECTED".equals(st) || "SELECTED".equals(st)) {
                    return false;
                }
                if (!"SUBMITTED".equals(st) && !"UNDER_REVIEW".equals(st)) {
                    return false;
                }
                String audit = "[ADMIN_REVOKE] revoked by "
                        + (adminUserId != null ? adminUserId : "unknown")
                        + " at "
                        + Instant.now();
                String existingNote = target.getNote();
                if (existingNote == null || existingNote.isBlank()) {
                    target.setNote(audit);
                } else {
                    target.setNote(existingNote + "\n" + audit);
                }
                target.setStatus("REJECTED");
                target.setAdminRevoked(Boolean.TRUE);
                target.setUpdatedAt(Instant.now().toString());
                applications.set(targetIndex, target);
                Files.writeString(applicationsFile, GSON.toJson(applications), StandardCharsets.UTF_8);
                return true;
            } catch (IOException | RuntimeException e) {
                return false;
            }
        }
    }

    public boolean updateStatus(String applicationId, String status) {
        if (applicationId == null || applicationId.isBlank() || !isValidStatus(status)) {
            return false;
        }
        String normalizedStatus = status.trim().toUpperCase();
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                Application target = null;
                int targetIndex = -1;
                for (int i = 0; i < applications.size(); i++) {
                    if (applicationId.equals(applications.get(i).getApplicationId())) {
                        target = applications.get(i);
                        targetIndex = i;
                        break;
                    }
                }
                if (target == null) {
                    return false;
                }
                target.setStatus(normalizedStatus);
                target.setAdminRevoked(null);
                target.setUpdatedAt(Instant.now().toString());
                applications.set(targetIndex, target);
                Files.writeString(applicationsFile, GSON.toJson(applications), StandardCharsets.UTF_8);
                return true;
            } catch (IOException | RuntimeException e) {
                return false;
            }
        }
    }

    public boolean save(Application application) {
        if (application == null || application.getApplicantUserId() == null || application.getJobId() == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                normalizeBeforeSave(application);

                int sameIdIndex = -1;
                int sameApplicantJobIndex = -1;
                for (int i = 0; i < applications.size(); i++) {
                    Application current = applications.get(i);
                    if (application.getApplicationId().equals(current.getApplicationId())) {
                        sameIdIndex = i;
                    }
                    if (application.getApplicantUserId().equals(current.getApplicantUserId())
                            && application.getJobId().equals(current.getJobId())) {
                        sameApplicantJobIndex = i;
                    }
                }

                if (sameIdIndex >= 0) {
                    application.setCreatedAt(applications.get(sameIdIndex).getCreatedAt());
                    applications.set(sameIdIndex, application);
                } else if (sameApplicantJobIndex >= 0) {
                    Application existing = applications.get(sameApplicantJobIndex);
                    application.setApplicationId(existing.getApplicationId());
                    application.setCreatedAt(existing.getCreatedAt());
                    applications.set(sameApplicantJobIndex, application);
                } else {
                    applications.add(application);
                }

                Files.writeString(applicationsFile, GSON.toJson(applications), StandardCharsets.UTF_8);
                return true;
            } catch (IOException | RuntimeException e) {
                return false;
            }
        }
    }

    private void normalizeBeforeSave(Application application) {
        String now = Instant.now().toString();
        if (application.getApplicationId() == null || application.getApplicationId().isBlank()) {
            application.setApplicationId("A-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        if (application.getMissingSkills() == null) {
            application.setMissingSkills(new ArrayList<>());
        }
        if (application.getMatchScore() == null) {
            application.setMatchScore(0);
        }
        if (!isValidStatus(application.getStatus())) {
            application.setStatus("SUBMITTED");
        } else {
            application.setStatus(application.getStatus().trim().toUpperCase());
        }
        if (application.getCreatedAt() == null || application.getCreatedAt().isBlank()) {
            application.setCreatedAt(now);
        }
        application.setUpdatedAt(now);
    }

    private boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        return VALID_STATUSES.contains(status.trim().toUpperCase());
    }
}
