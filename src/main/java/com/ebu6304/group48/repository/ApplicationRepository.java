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
import java.util.List;
import java.util.UUID;

/**
 * JSON persistence for {@code applications.json}. Used by TA job list, MO flows, and admin revoke.
 */
public class ApplicationRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Application>>() { }.getType();
    private static final Object FILE_LOCK = new Object();

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
        List<Application> list = GSON.fromJson(json, LIST_TYPE);
        return list != null ? list : new ArrayList<>();
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
        for (Application app : findAll()) {
            if (applicationId.equals(app.getApplicationId())) {
                return app;
            }
        }
        return null;
    }

    public List<Application> findByApplicantUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return new ArrayList<>();
        }
        List<Application> out = new ArrayList<>();
        for (Application app : findAll()) {
            if (userId.equals(app.getApplicantUserId())) {
                out.add(app);
            }
        }
        return out;
    }

    public List<Application> findByJobId(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return new ArrayList<>();
        }
        List<Application> out = new ArrayList<>();
        for (Application app : findAll()) {
            if (jobId.equals(app.getJobId())) {
                out.add(app);
            }
        }
        return out;
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
        if (application == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                normalizeBeforeSave(application);

                int existingIndex = -1;
                for (int i = 0; i < applications.size(); i++) {
                    Application a = applications.get(i);
                    if (application.getApplicationId() != null && application.getApplicationId().equals(a.getApplicationId())) {
                        existingIndex = i;
                        break;
                    }
                }
                if (existingIndex < 0 && application.getJobId() != null && application.getApplicantUserId() != null) {
                    for (int i = 0; i < applications.size(); i++) {
                        Application a = applications.get(i);
                        if (application.getJobId().equals(a.getJobId())
                                && application.getApplicantUserId().equals(a.getApplicantUserId())) {
                            existingIndex = i;
                            application.setApplicationId(a.getApplicationId());
                            break;
                        }
                    }
                }
                if (existingIndex >= 0) {
                    applications.set(existingIndex, application);
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

    private static boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        String s = status.trim().toUpperCase();
        return "SUBMITTED".equals(s) || "UNDER_REVIEW".equals(s) || "SELECTED".equals(s) || "REJECTED".equals(s);
    }

    private void normalizeBeforeSave(Application app) {
        if (app.getApplicationId() == null || app.getApplicationId().isBlank()) {
            app.setApplicationId("A-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        }
        if (app.getStatus() == null || app.getStatus().isBlank()) {
            app.setStatus("SUBMITTED");
        }
        if (app.getCreatedAt() == null || app.getCreatedAt().isBlank()) {
            app.setCreatedAt(Instant.now().toString());
        }
        if (app.getUpdatedAt() == null || app.getUpdatedAt().isBlank()) {
            app.setUpdatedAt(app.getCreatedAt());
        }
    }
}
