/**
 * =============================================================================
 * Job Repository - Data Access Layer (Placeholder Implementation)
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
 * Data access layer for Job entities. Responsible for CRUD operations
 * on job postings stored in JSON format. Provides filtered queries
 * for job browsing and application processing.
 * 
 * =============================================================================
 * 
 * 🎯 IMPLEMENTATION REQUIREMENTS:
 * 
 * 1. FILE OPERATIONS:
 *    - Read from: data/jobs.json
 *    - Write to: data/jobs.json
 *    - File format: JSON array of Job objects
 *    - Location: Determined by AppPaths.resolveDataDirectory()
 * 
 * 2. REQUIRED METHODS:
 *    - findAllOpenJobs(): Retrieve all jobs with status="OPEN"
 *    - findById(String jobId): Find specific job by ID
 *    - findAll(): Retrieve all jobs (regardless of status)
 * 
 * 3. EXPECTED BEHAVIOR:
 *    - findAllOpenJobs: Filters by status="OPEN", returns empty list if none
 *    - findById: Returns null if job not found
 *    - findAll: Returns all jobs, empty list if file doesn't exist
 *    - Sorting: Jobs should be sorted by createdAt (newest first)
 * 
 * =============================================================================
 * 
 * 🔧 TECHNICAL IMPLEMENTATION GUIDE:
 * 
 * 1. FILTERING LOGIC:
 *    - findAllOpenJobs should filter in memory after loading all jobs
 *    - Consider performance implications for large job datasets
 *    - Could implement streaming JSON parsing for efficiency
 * 
 * 2. DATA INTEGRITY:
 *    - Validate job data on read (required fields, valid status values)
 *    - Ensure jobId uniqueness when saving new jobs
 *    - Handle corrupted JSON gracefully (log error, return empty list)
 * 
 * 3. PERFORMANCE OPTIMIZATIONS:
 *    - Cache open jobs list (frequently accessed)
 *    - Implement pagination support for large datasets
 *    - Consider indexing by status for faster filtering
 * 
 * 4. CONCURRENCY CONSIDERATIONS:
 *    - Multiple MOs may create jobs simultaneously
 *    - Job status updates may conflict with reads
 *    - Implement file locking or optimistic concurrency control
 * 
 * =============================================================================
 * 
 * 📝 PLACEHOLDER IMPLEMENTATION DETAILS:
 * 
 * Current methods return dummy values:
 *   - findAllOpenJobs: Returns empty ArrayList
 *   - findById: Always returns null
 *   - findAll: Returns empty ArrayList
 * 
 * This allows dependent code to compile but provides no real data.
 * Real implementation must read/write jobs.json file.
 * 
 * =============================================================================
 * 
 * 🧪 TESTING RECOMMENDATIONS:
 * 
 * 1. Unit Tests:
 *    - Test findAllOpenJobs returns only OPEN status jobs
 *    - Test findById with valid/invalid job IDs
 *    - Test findAll returns correct job count
 *    - Test filtering with mixed status jobs
 * 
 * 2. Integration Tests:
 *    - Test file creation with initial empty array
 *    - Test job creation and retrieval persistence
 *    - Test status updates (OPEN to CLOSED)
 *    - Test concurrent job creation by multiple users
 * 
 * 3. Performance Tests:
 *    - Measure query performance with 1000+ jobs
 *    - Test memory usage during filtering operations
 *    - Benchmark file I/O operations
 * 
 * =============================================================================
 * 
 * 📁 DEPENDENCIES:
 *   - Gson (already in pom.xml), consistent with UserRepository
 *   - ServletContext (for data directory resolution)
 *   - Job model class (for data representation)
 * 
 * =============================================================================
 */
package com.ebu6304.group48.repository;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Job;
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
import java.util.stream.Collectors;

public class JobRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Job>>() { }.getType();
    private static final Object FILE_LOCK = new Object();

    private final Path jobsFile;

    public JobRepository(ServletContext context) {
        String dataDirectory = AppPaths.resolveDataDirectory(context);
        this.jobsFile = Path.of(dataDirectory, "jobs.json");
    }

    public List<Job> findAllOpenJobs() {
        return findAll().stream()
                .filter(j -> "OPEN".equalsIgnoreCase(j.getStatus()))
                .collect(Collectors.toList());
    }

    public Job findById(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return null;
        }
        return findAll().stream()
                .filter(j -> jobId.equals(j.getJobId()))
                .findFirst()
                .orElse(null);
    }

    public List<Job> findAll() {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                String json = Files.readString(jobsFile, StandardCharsets.UTF_8);
                List<Job> jobs = GSON.fromJson(json, LIST_TYPE);
                if (jobs == null) {
                    return new ArrayList<>();
                }
                jobs.sort(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
                return new ArrayList<>(jobs);
            } catch (IOException e) {
                return new ArrayList<>();
            } catch (RuntimeException e) {
                return new ArrayList<>();
            }
        }
    }

    public boolean save(Job job) {
        if (job == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Job> jobs = findAllInternal();
                normalizeBeforeSave(job);

                int existingIndex = -1;
                for (int i = 0; i < jobs.size(); i++) {
                    if (job.getJobId().equals(jobs.get(i).getJobId())) {
                        existingIndex = i;
                        break;
                    }
                }
                if (existingIndex >= 0) {
                    jobs.set(existingIndex, job);
                } else {
                    jobs.add(job);
                }
                Files.writeString(jobsFile, GSON.toJson(jobs), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    private void normalizeBeforeSave(Job job) {
        if (job.getJobId() == null || job.getJobId().isBlank()) {
            job.setJobId("J-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        if (job.getStatus() == null || job.getStatus().isBlank()) {
            job.setStatus("OPEN");
        }
        if (job.getCreatedAt() == null || job.getCreatedAt().isBlank()) {
            job.setCreatedAt(Instant.now().toString());
        }
        if (job.getRequiredSkills() == null) {
            job.setRequiredSkills(new ArrayList<>());
        }
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(jobsFile.getParent());
        if (!Files.exists(jobsFile)) {
            Files.writeString(jobsFile, "[]", StandardCharsets.UTF_8);
        }
    }

    private List<Job> findAllInternal() throws IOException {
        String json = Files.readString(jobsFile, StandardCharsets.UTF_8);
        List<Job> jobs = GSON.fromJson(json, LIST_TYPE);
        return jobs != null ? jobs : new ArrayList<>();
    }
}