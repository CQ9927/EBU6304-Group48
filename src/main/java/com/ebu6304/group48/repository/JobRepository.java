/**
 * JSON persistence for {@code jobs.json}.
 * Provides CRUD operations for Job entities with thread-safe file I/O.
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

    public boolean updateStatus(String jobId, String status) {
        if (jobId == null || jobId.isBlank() || status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        if (!"OPEN".equals(normalized) && !"CLOSED".equals(normalized)) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Job> jobs = findAllInternal();
                for (int i = 0; i < jobs.size(); i++) {
                    Job j = jobs.get(i);
                    if (jobId.equals(j.getJobId())) {
                        j.setStatus(normalized);
                        jobs.set(i, j);
                        Files.writeString(jobsFile, GSON.toJson(jobs), StandardCharsets.UTF_8);
                        return true;
                    }
                }
                return false;
            } catch (IOException | RuntimeException e) {
                return false;
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