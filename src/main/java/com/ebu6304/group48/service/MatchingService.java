package com.ebu6304.group48.service;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds simple rule hints for admin workload dashboard.
 */
public class MatchingService {

    private static final Map<String, String> MAJOR_KEYWORDS;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("software", "software engineering");
        m.put("database", "computer science");
        m.put("algorithm", "computer science");
        m.put("web", "software engineering");
        m.put("network", "computer science");
        m.put("data", "computer science");
        m.put("security", "computer science");
        m.put("exam", "");
        m.put("invigilation", "");
        m.put("proctoring", "");
        MAJOR_KEYWORDS = Collections.unmodifiableMap(m);
    }

    // ---- Multi-dimensional matching ----

    /**
     * Computes a weighted multi-dimensional match score (0–100) between a job and a TA profile.
     * Dimensions: skills (50), schedule compatibility (25), major relevance (15), profile completeness (10).
     */
    public MatchResult computeMatch(Job job, Profile profile) {
        if (job == null || profile == null) {
            return new MatchResult(0, 0, 0, 0, 0,
                    Collections.emptyList(), Collections.emptyList(), false, "N/A");
        }

        List<String> jobSkills = normalizeList(job.getRequiredSkills());
        List<String> userSkills = normalizeList(profile.getSkills());

        // 1. Skill score (max 50)
        int skillScore;
        List<String> matchedSkills;
        List<String> missingSkills;
        if (jobSkills.isEmpty()) {
            skillScore = 50;
            matchedSkills = Collections.emptyList();
            missingSkills = Collections.emptyList();
        } else {
            Set<String> userSkillsLower = userSkills.stream()
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            matchedSkills = new ArrayList<>();
            missingSkills = new ArrayList<>();
            for (String required : jobSkills) {
                if (userSkillsLower.contains(required.toLowerCase(Locale.ROOT))) {
                    matchedSkills.add(required);
                } else {
                    missingSkills.add(required);
                }
            }
            int baseScore = matchedSkills.size() * 40 / jobSkills.size();
            int extraCount = Math.max(0, userSkills.size() - matchedSkills.size());
            int bonus = Math.min(extraCount * 2, 10);
            skillScore = Math.min(baseScore + bonus, 50);
        }

        // 2. Schedule score (max 25)
        int scheduleScore;
        boolean scheduleMatch;
        String jobSchedule = job.getSchedule();
        if (jobSchedule == null || jobSchedule.isBlank()) {
            scheduleScore = 25;
            scheduleMatch = true;
        } else {
            List<String> availability = normalizeList(profile.getAvailability());
            Set<String> availLower = availability.stream()
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            if (availLower.contains(jobSchedule.toLowerCase(Locale.ROOT).trim())) {
                scheduleScore = 25;
                scheduleMatch = true;
            } else {
                String jobDay = dayPrefix(jobSchedule);
                boolean sameDay = availability.stream()
                        .anyMatch(a -> jobDay.equalsIgnoreCase(dayPrefix(a)));
                scheduleScore = sameDay ? 15 : 0;
                scheduleMatch = false;
            }
        }

        // 3. Major score (max 15)
        int majorScore;
        String titleLower = job.getTitle() != null ? job.getTitle().toLowerCase(Locale.ROOT) : "";
        String majorLower = profile.getMajor() != null ? profile.getMajor().toLowerCase(Locale.ROOT) : "";
        if (titleLower.isEmpty()) {
            majorScore = 5;
        } else {
            boolean fullMatch = false;
            boolean partialMatch = false;
            for (Map.Entry<String, String> entry : MAJOR_KEYWORDS.entrySet()) {
                if (titleLower.contains(entry.getKey())) {
                    if (entry.getValue().isEmpty()) {
                        fullMatch = true;
                    } else if (majorLower.contains(entry.getValue())) {
                        fullMatch = true;
                    } else {
                        partialMatch = true;
                    }
                }
            }
            if (fullMatch) {
                majorScore = 15;
            } else if (partialMatch) {
                majorScore = 10;
            } else {
                majorScore = 5;
            }
        }

        // 4. Completeness score (max 10)
        int completenessScore = 0;
        boolean hasCV = profile.getCvFileName() != null && !profile.getCvFileName().isBlank();
        if (hasCV) {
            completenessScore += 5;
        }
        boolean hasName = profile.getName() != null && !profile.getName().isBlank();
        boolean hasEmail = profile.getEmail() != null && !profile.getEmail().isBlank();
        boolean hasSkills = profile.getSkills() != null && !profile.getSkills().isEmpty();
        boolean hasAvailability = profile.getAvailability() != null && !profile.getAvailability().isEmpty();
        int filledInfoFields = (hasName ? 1 : 0) + (hasEmail ? 1 : 0) + (hasSkills ? 1 : 0) + (hasAvailability ? 1 : 0);
        if (filledInfoFields == 4) {
            completenessScore += 5;
        } else if (filledInfoFields > 0) {
            completenessScore += 3;
        }

        // 5. Total
        int totalScore = skillScore + scheduleScore + majorScore + completenessScore;

        // 6. Detail string
        StringBuilder sb = new StringBuilder();
        sb.append("Skills: ").append(skillScore).append("/50");
        if (!matchedSkills.isEmpty()) {
            sb.append(" (").append(String.join(", ", matchedSkills)).append(" matched");
        }
        if (!missingSkills.isEmpty()) {
            sb.append("; ").append(String.join(", ", missingSkills)).append(" missing");
        }
        sb.append(")");
        sb.append(" | Schedule: ").append(scheduleScore).append("/25");
        if (jobSchedule != null && !jobSchedule.isBlank()) {
            sb.append(scheduleMatch ? " (matched)" : " (no conflict)");
        }
        sb.append(" | Major: ").append(majorScore).append("/15");
        sb.append(" | Profile: ").append(completenessScore).append("/10");

        return new MatchResult(totalScore, skillScore, scheduleScore, majorScore, completenessScore,
                matchedSkills, missingSkills, scheduleMatch, sb.toString());
    }

    private List<String> normalizeList(List<String> list) {
        return list != null ? list : Collections.emptyList();
    }

    private static String dayPrefix(String schedule) {
        if (schedule == null || schedule.length() < 3) {
            return "";
        }
        return schedule.substring(0, 3);
    }

    // ---- Match result ----

    public static class MatchResult {
        private final int totalScore;
        private final int skillScore;
        private final int scheduleScore;
        private final int majorScore;
        private final int completenessScore;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final boolean scheduleMatch;
        private final String detail;

        public MatchResult(int totalScore, int skillScore, int scheduleScore, int majorScore,
                           int completenessScore, List<String> matchedSkills, List<String> missingSkills,
                           boolean scheduleMatch, String detail) {
            this.totalScore = totalScore;
            this.skillScore = skillScore;
            this.scheduleScore = scheduleScore;
            this.majorScore = majorScore;
            this.completenessScore = completenessScore;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.scheduleMatch = scheduleMatch;
            this.detail = detail;
        }

        public int getTotalScore() { return totalScore; }
        public int getSkillScore() { return skillScore; }
        public int getScheduleScore() { return scheduleScore; }
        public int getMajorScore() { return majorScore; }
        public int getCompletenessScore() { return completenessScore; }
        public List<String> getMatchedSkills() { return matchedSkills; }
        public List<String> getMissingSkills() { return missingSkills; }
        public boolean isScheduleMatch() { return scheduleMatch; }
        public String getDetail() { return detail; }
    }

    // ---- Conflict hints (existing) ----

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
