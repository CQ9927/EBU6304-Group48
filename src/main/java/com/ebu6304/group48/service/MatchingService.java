package com.ebu6304.group48.service;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds simple rule hints for admin workload dashboard.
 */
public class MatchingService {

    public List<String> buildConflictHints(List<Job> jobs, List<Application> applications) {
        List<String> hints = new ArrayList<>();
        Map<String, Job> jobsById = new HashMap<>();
        for (Job job : jobs) {
            if (job != null && job.getJobId() != null) {
                jobsById.put(job.getJobId(), job);
            }
        }

        addUnknownJobHints(hints, jobsById, applications);
        addDuplicateApplicationHints(hints, applications);
        addCapacityHints(hints, jobs, applications);
        addClosedJobApplicationHints(hints, jobsById, applications);
        addLowScoreSelectedHints(hints, applications);

        hints.sort(Comparator.naturalOrder());
        return hints;
    }

    private void addUnknownJobHints(List<String> hints, Map<String, Job> jobsById, List<Application> applications) {
        for (Application app : applications) {
            if (app == null || isBlank(app.getJobId())) {
                continue;
            }
            if (!jobsById.containsKey(app.getJobId())) {
                hints.add("Application " + app.getApplicationId() + " references unknown jobId " + app.getJobId() + ".");
            }
        }
    }

    private void addDuplicateApplicationHints(List<String> hints, List<Application> applications) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicated = new HashSet<>();
        for (Application app : applications) {
            if (app == null || isBlank(app.getJobId()) || isBlank(app.getApplicantUserId())) {
                continue;
            }
            String key = app.getJobId() + "|" + app.getApplicantUserId();
            if (!seen.add(key)) {
                duplicated.add(key);
            }
        }
        for (String key : duplicated) {
            String[] parts = key.split("\\|", 2);
            hints.add("Duplicate applications found for job " + parts[0] + " by TA " + parts[1] + ".");
        }
    }

    private void addCapacityHints(List<String> hints, List<Job> jobs, List<Application> applications) {
        Map<String, Integer> selectedByJob = new HashMap<>();
        for (Application app : applications) {
            if (app == null || isBlank(app.getJobId())) {
                continue;
            }
            if ("SELECTED".equalsIgnoreCase(safe(app.getStatus()))) {
                selectedByJob.merge(app.getJobId(), 1, Integer::sum);
            }
        }

        for (Job job : jobs) {
            if (job == null || isBlank(job.getJobId())) {
                continue;
            }
            int capacity = job.getCapacity() == null ? 0 : Math.max(job.getCapacity(), 0);
            int selected = selectedByJob.getOrDefault(job.getJobId(), 0);
            if (selected > capacity) {
                hints.add("Job " + job.getJobId() + " is over capacity: selected " + selected + " > capacity " + capacity + ".");
            }
        }
    }

    private void addClosedJobApplicationHints(List<String> hints, Map<String, Job> jobsById, List<Application> applications) {
        for (Application app : applications) {
            if (app == null || isBlank(app.getJobId())) {
                continue;
            }
            Job job = jobsById.get(app.getJobId());
            if (job == null) {
                continue;
            }
            if ("CLOSED".equalsIgnoreCase(safe(job.getStatus()))
                    && ("SUBMITTED".equalsIgnoreCase(safe(app.getStatus()))
                    || "UNDER_REVIEW".equalsIgnoreCase(safe(app.getStatus())))) {
                hints.add("Job " + job.getJobId() + " is CLOSED but still has " + app.getStatus() + " application " + app.getApplicationId() + ".");
            }
        }
    }

    private void addLowScoreSelectedHints(List<String> hints, List<Application> applications) {
        for (Application app : applications) {
            if (app == null) {
                continue;
            }
            if ("SELECTED".equalsIgnoreCase(safe(app.getStatus()))) {
                int score = app.getMatchScore() == null ? 0 : app.getMatchScore();
                if (score < 50) {
                    hints.add("Selected application " + app.getApplicationId() + " has low matchScore (" + score + ").");
                }
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
