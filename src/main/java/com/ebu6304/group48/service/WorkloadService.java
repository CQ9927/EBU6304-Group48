package com.ebu6304.group48.service;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.repository.JobRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkloadService {

    private static final Gson GSON = new Gson();
    private static final Type APPLICATION_LIST_TYPE = new TypeToken<List<Application>>() { }.getType();

    private final JobRepository jobRepository;
    private final MatchingService matchingService;
    private final Path applicationsFile;

    public WorkloadService(ServletContext context, MatchingService matchingService) {
        this.jobRepository = new JobRepository(context);
        this.matchingService = matchingService;
        this.applicationsFile = Path.of(AppPaths.resolveDataDirectory(context), "applications.json");
    }

    public WorkloadSnapshot buildSnapshot() {
        List<Job> jobs = jobRepository.findAll();
        List<Application> applications = readApplications();
        Map<String, Integer> submittedByJob = new HashMap<>();
        Map<String, Integer> reviewByJob = new HashMap<>();
        Map<String, Integer> selectedByJob = new HashMap<>();
        Map<String, Integer> rejectedByJob = new HashMap<>();

        int submitted = 0;
        int underReview = 0;
        int selected = 0;
        int rejected = 0;

        for (Application app : applications) {
            if (app == null || isBlank(app.getJobId())) {
                continue;
            }
            String status = safeUpper(app.getStatus());
            if ("SUBMITTED".equals(status)) {
                submitted++;
                submittedByJob.merge(app.getJobId(), 1, Integer::sum);
            } else if ("UNDER_REVIEW".equals(status)) {
                underReview++;
                reviewByJob.merge(app.getJobId(), 1, Integer::sum);
            } else if ("SELECTED".equals(status)) {
                selected++;
                selectedByJob.merge(app.getJobId(), 1, Integer::sum);
            } else if ("REJECTED".equals(status)) {
                rejected++;
                rejectedByJob.merge(app.getJobId(), 1, Integer::sum);
            }
        }

        int openJobs = 0;
        int closedJobs = 0;
        int openJobsWithoutSelection = 0;
        List<JobWorkloadRow> rows = new ArrayList<>();
        for (Job job : jobs) {
            if (job == null || isBlank(job.getJobId())) {
                continue;
            }
            String jobStatus = safeUpper(job.getStatus());
            if ("OPEN".equals(jobStatus)) {
                openJobs++;
            } else if ("CLOSED".equals(jobStatus)) {
                closedJobs++;
            }

            int submittedCount = submittedByJob.getOrDefault(job.getJobId(), 0);
            int underReviewCount = reviewByJob.getOrDefault(job.getJobId(), 0);
            int selectedCount = selectedByJob.getOrDefault(job.getJobId(), 0);
            int rejectedCount = rejectedByJob.getOrDefault(job.getJobId(), 0);
            int total = submittedCount + underReviewCount + selectedCount + rejectedCount;
            int capacity = job.getCapacity() == null ? 0 : Math.max(0, job.getCapacity());
            int remaining = Math.max(0, capacity - selectedCount);
            boolean overCapacity = selectedCount > capacity;
            if ("OPEN".equals(jobStatus) && selectedCount == 0) {
                openJobsWithoutSelection++;
            }

            rows.add(new JobWorkloadRow(
                    job.getJobId(),
                    defaultText(job.getTitle()),
                    defaultText(job.getStatus()),
                    capacity,
                    total,
                    submittedCount,
                    underReviewCount,
                    selectedCount,
                    rejectedCount,
                    overCapacity,
                    remaining
            ));
        }

        rows.sort(Comparator.comparing(JobWorkloadRow::getJobId));
        List<String> hints = matchingService.buildConflictHints(jobs, applications);
        return new WorkloadSnapshot(
                jobs.size(),
                openJobs,
                closedJobs,
                applications.size(),
                submitted,
                underReview,
                selected,
                rejected,
                openJobsWithoutSelection,
                rows,
                hints
        );
    }

    private List<Application> readApplications() {
        try {
            ensureStorage();
            String json = Files.readString(applicationsFile, StandardCharsets.UTF_8);
            List<Application> applications = GSON.fromJson(json, APPLICATION_LIST_TYPE);
            return applications != null ? applications : new ArrayList<>();
        } catch (IOException | RuntimeException e) {
            return new ArrayList<>();
        }
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(applicationsFile.getParent());
        if (!Files.exists(applicationsFile)) {
            Files.writeString(applicationsFile, "[]", StandardCharsets.UTF_8);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public static class WorkloadSnapshot {
        private final int totalJobs;
        private final int openJobs;
        private final int closedJobs;
        private final int totalApplications;
        private final int submittedApplications;
        private final int underReviewApplications;
        private final int selectedApplications;
        private final int rejectedApplications;
        private final int openJobsWithoutSelection;
        private final List<JobWorkloadRow> rows;
        private final List<String> hints;

        public WorkloadSnapshot(int totalJobs, int openJobs, int closedJobs, int totalApplications,
                                int submittedApplications, int underReviewApplications, int selectedApplications,
                                int rejectedApplications, int openJobsWithoutSelection,
                                List<JobWorkloadRow> rows, List<String> hints) {
            this.totalJobs = totalJobs;
            this.openJobs = openJobs;
            this.closedJobs = closedJobs;
            this.totalApplications = totalApplications;
            this.submittedApplications = submittedApplications;
            this.underReviewApplications = underReviewApplications;
            this.selectedApplications = selectedApplications;
            this.rejectedApplications = rejectedApplications;
            this.openJobsWithoutSelection = openJobsWithoutSelection;
            this.rows = rows;
            this.hints = hints;
        }

        public int getTotalJobs() {
            return totalJobs;
        }

        public int getOpenJobs() {
            return openJobs;
        }

        public int getClosedJobs() {
            return closedJobs;
        }

        public int getTotalApplications() {
            return totalApplications;
        }

        public int getSubmittedApplications() {
            return submittedApplications;
        }

        public int getUnderReviewApplications() {
            return underReviewApplications;
        }

        public int getSelectedApplications() {
            return selectedApplications;
        }

        public int getRejectedApplications() {
            return rejectedApplications;
        }

        public int getOpenJobsWithoutSelection() {
            return openJobsWithoutSelection;
        }

        public List<JobWorkloadRow> getRows() {
            return rows;
        }

        public List<String> getHints() {
            return hints;
        }
    }

    public static class JobWorkloadRow {
        private final String jobId;
        private final String title;
        private final String status;
        private final int capacity;
        private final int totalApplications;
        private final int submittedCount;
        private final int underReviewCount;
        private final int selectedCount;
        private final int rejectedCount;
        private final boolean overCapacity;
        private final int remainingSlots;

        public JobWorkloadRow(String jobId, String title, String status, int capacity, int totalApplications,
                              int submittedCount, int underReviewCount, int selectedCount, int rejectedCount,
                              boolean overCapacity, int remainingSlots) {
            this.jobId = jobId;
            this.title = title;
            this.status = status;
            this.capacity = capacity;
            this.totalApplications = totalApplications;
            this.submittedCount = submittedCount;
            this.underReviewCount = underReviewCount;
            this.selectedCount = selectedCount;
            this.rejectedCount = rejectedCount;
            this.overCapacity = overCapacity;
            this.remainingSlots = remainingSlots;
        }

        public String getJobId() {
            return jobId;
        }

        public String getTitle() {
            return title;
        }

        public String getStatus() {
            return status;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getTotalApplications() {
            return totalApplications;
        }

        public int getSubmittedCount() {
            return submittedCount;
        }

        public int getUnderReviewCount() {
            return underReviewCount;
        }

        public int getSelectedCount() {
            return selectedCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }

        public boolean isOverCapacity() {
            return overCapacity;
        }

        public int getRemainingSlots() {
            return remainingSlots;
        }
    }
}
