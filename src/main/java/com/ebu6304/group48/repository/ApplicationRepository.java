/**
 * =============================================================================
 * Application Repository - Data Access Layer (Placeholder Implementation)
 * =============================================================================
 * 
 * OWNER: zzzskl (231226772)
 * CREATED: 2026-04-07
 * VERSION: 1.0
 * STATUS: 🔧 TEST/PLACEHOLDER CODE - Requires Implementation
 * 
 * =============================================================================
 * 
 * ⚠️ IMPORTANT NOTE:
 * This is a PLACEHOLDER implementation created to allow compilation of
 * dependent servlets. It does NOT provide actual data persistence.
 * Another team member must implement the actual JSON file operations.
 * 
 * =============================================================================
 * 
 * 📋 REPOSITORY DESCRIPTION:
 * Data access layer for Application entities. Responsible for CRUD operations
 * on job applications stored in JSON format. Provides query methods for
 * retrieving applications by user or job, supporting both TA and MO workflows.
 * 
 * =============================================================================
 * 
 * 🎯 IMPLEMENTATION REQUIREMENTS:
 * 
 * 1. FILE OPERATIONS:
 *    - Read from: data/applications.json
 *    - Write to: data/applications.json
 *    - File format: JSON array of Application objects
 *    - Location: Determined by AppPaths.resolveDataDirectory()
 * 
 * 2. REQUIRED METHODS:
 *    - findByApplicantUserId(String userId): Find all applications by TA
 *    - findByJobId(String jobId): Find all applications for a job
 *    - save(Application application): Create or update application
 * 
 * 3. EXPECTED BEHAVIOR:
 *    - findByApplicantUserId: Returns empty list if no applications found
 *    - findByJobId: Returns empty list if no applications for job
 *    - save: Creates new if applicationId doesn't exist, updates otherwise
 *    - Uniqueness: Prevent duplicate applications (same TA + job)
 * 
 * =============================================================================
 * 
 * 🔧 TECHNICAL IMPLEMENTATION GUIDE:
 * 
 * 1. QUERY OPTIMIZATION:
 *    - Both findBy methods require filtering full dataset
 *    - Consider creating indexes in memory for frequent queries
 *    - Could implement composite indexing for (userId, jobId) queries
 * 
 * 2. DATA VALIDATION:
 *    - Validate foreign key references (userId, jobId exist)
 *    - Ensure matchScore is within 0-100 range
 *    - Validate status transitions follow business rules
 *    - Check for duplicate applications before saving
 * 
 * 3. PERFORMANCE CONSIDERATIONS:
 *    - Applications may grow large (many TAs applying for many jobs)
 *    - Implement pagination for application lists
 *    - Cache frequently accessed application data
 *    - Consider partitioning by semester or job type
 * 
 * 4. CONCURRENCY HANDLING:
 *    - Multiple TAs may apply to same job simultaneously
 *    - MOs may review multiple applications concurrently
 *    - Status updates need to be atomic
 *    - Implement optimistic locking with version field
 * 
 * =============================================================================
 * 
 * 📝 PLACEHOLDER IMPLEMENTATION DETAILS:
 * 
 * Current methods return dummy values:
 *   - findByApplicantUserId: Returns empty ArrayList
 *   - findByJobId: Returns empty ArrayList  
 *   - save: Always returns true (pretends success)
 * 
 * This allows dependent code to compile but provides no real data.
 * Real implementation must read/write applications.json file.
 * 
 * =============================================================================
 * 
 * 🧪 TESTING RECOMMENDATIONS:
 * 
 * 1. Unit Tests:
 *    - Test findByApplicantUserId returns correct user's applications
 *    - Test findByJobId returns correct job's applications
 *    - Test save creates new and updates existing applications
 *    - Test duplicate application prevention
 * 
 * 2. Integration Tests:
 *    - Test application persistence across sessions
 *    - Test concurrent application submissions
 *    - Test status update workflows
 *    - Test referential integrity with users/jobs
 * 
 * 3. Performance Tests:
 *    - Measure query performance with 1000+ applications
 *    - Test concurrent read/write operations
 *    - Benchmark filtering operations on large datasets
 * 
 * =============================================================================
 * 
 * 📁 DEPENDENCIES:
 *   - Gson (already in pom.xml), consistent with UserRepository
 *   - ServletContext (for data directory resolution)
 *   - Application model class (for data representation)
 *   - JobRepository (for job existence validation)
 *   - ProfileRepository (for user existence validation)
 * 
 * =============================================================================
 */
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

    public List<Application> findAll() {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Application> applications = readAllInternal();
                applications.sort(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
                return applications;
            } catch (IOException e) {
                return new ArrayList<>();
            } catch (RuntimeException e) {
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
                for (Application app : applications) {
                    if (applicationId.equals(app.getApplicationId())) {
                        target = app;
                        break;
                    }
                }
                if (target == null) {
                    return false;
                }
                target.setStatus(normalizedStatus);
                target.setUpdatedAt(Instant.now().toString());
                Files.writeString(applicationsFile, GSON.toJson(applications), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
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
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
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
}