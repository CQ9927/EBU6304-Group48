package com.ebu6304.group48.service;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.model.User;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.repository.UserRepository;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaWorkloadService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public TaWorkloadService(ServletContext context, String dataDirectory) {
        this.userRepository = new UserRepository(dataDirectory);
        this.profileRepository = new ProfileRepository(context);
        this.applicationRepository = new ApplicationRepository(context);
        this.jobRepository = new JobRepository(context);
    }

    public TaWorkloadSnapshot buildSnapshot(int weeklyHoursThreshold) throws IOException {
        int threshold = weeklyHoursThreshold < 1 ? 10 : weeklyHoursThreshold;
        userRepository.ensureStorage();

        Map<String, Profile> profilesByUserId = new HashMap<>();
        for (Profile profile : profileRepository.findAll()) {
            if (profile != null && profile.getUserId() != null) {
                profilesByUserId.put(profile.getUserId(), profile);
            }
        }

        Map<String, Job> jobsById = new HashMap<>();
        for (Job job : jobRepository.findAll()) {
            if (job != null && job.getJobId() != null) {
                jobsById.put(job.getJobId(), job);
            }
        }

        Map<String, List<Application>> appsByTa = new HashMap<>();
        for (Application app : applicationRepository.findAll()) {
            if (app == null || app.getApplicantUserId() == null) {
                continue;
            }
            appsByTa.computeIfAbsent(app.getApplicantUserId(), k -> new ArrayList<>()).add(app);
        }

        List<TaWorkloadRow> rows = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user == null || !"TA".equals(user.getRole())) {
                continue;
            }
            Profile profile = profilesByUserId.get(user.getUserId());
            List<Application> apps = appsByTa.getOrDefault(user.getUserId(), List.of());
            rows.add(buildRow(user, profile, apps, jobsById, threshold));
        }

        rows.sort(Comparator
                .comparingInt(TaWorkloadRow::getSelectedCount).reversed()
                .thenComparingInt(TaWorkloadRow::getPendingCount).reversed()
                .thenComparing(r -> r.getDisplayName().toLowerCase(Locale.ROOT)));

        int totalSelectedAssignments = rows.stream().mapToInt(TaWorkloadRow::getSelectedCount).sum();
        int tasWithConflicts = (int) rows.stream().filter(TaWorkloadRow::isScheduleConflict).count();
        int tasOverWeeklyThreshold = (int) rows.stream().filter(TaWorkloadRow::isHighLoad).count();

        return new TaWorkloadSnapshot(rows.size(), totalSelectedAssignments, tasWithConflicts,
                tasOverWeeklyThreshold, threshold, rows);
    }

    private TaWorkloadRow buildRow(User user, Profile profile, List<Application> apps, Map<String, Job> jobsById,
                                   int weeklyHoursThreshold) {
        int submitted = 0;
        int underReview = 0;
        int selected = 0;
        int rejected = 0;
        List<SelectedJobAssignment> selectedJobs = new ArrayList<>();

        for (Application app : apps) {
            String status = safeUpper(app.getStatus());
            switch (status) {
                case "SUBMITTED":
                    submitted++;
                    break;
                case "UNDER_REVIEW":
                    underReview++;
                    break;
                case "SELECTED":
                    selected++;
                    Job job = jobsById.get(app.getJobId());
                    if (job != null) {
                        selectedJobs.add(new SelectedJobAssignment(
                                job.getJobId(),
                                defaultText(job.getTitle()),
                                defaultText(job.getSchedule()),
                                defaultText(job.getSemester()),
                                scheduleHours(job.getSchedule())
                        ));
                    }
                    break;
                case "REJECTED":
                    rejected++;
                    break;
                default:
                    break;
            }
        }

        int weeklyHours = selectedJobs.stream().mapToInt(SelectedJobAssignment::getWeeklyHours).sum();
        boolean highLoad = weeklyHours > weeklyHoursThreshold;
        boolean scheduleConflict = hasScheduleConflict(selectedJobs);

        String displayName = profile != null && profile.getName() != null && !profile.getName().isBlank()
                ? profile.getName()
                : user.getUsername();

        return new TaWorkloadRow(
                user.getUserId(),
                user.getUsername(),
                displayName,
                profile != null ? profile.getEmail() : null,
                profile != null ? profile.getMajor() : null,
                profile != null && profile.getSkills() != null ? profile.getSkills().size() : 0,
                profile != null && profile.getCvFileName() != null && !profile.getCvFileName().isBlank(),
                Boolean.TRUE.equals(user.getBanned()),
                apps.size(),
                submitted,
                underReview,
                selected,
                rejected,
                submitted + underReview,
                weeklyHours,
                highLoad,
                scheduleConflict,
                selectedJobs
        );
    }

    static boolean hasScheduleConflict(List<SelectedJobAssignment> selectedJobs) {
        for (int i = 0; i < selectedJobs.size(); i++) {
            for (int j = i + 1; j < selectedJobs.size(); j++) {
                if (schedulesOverlap(selectedJobs.get(i).getSchedule(), selectedJobs.get(j).getSchedule())) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean schedulesOverlap(String a, String b) {
        if (a == null || b == null || "-".equals(a) || "-".equals(b)) {
            return false;
        }
        if (a.equalsIgnoreCase(b)) {
            return true;
        }
        TimeSlot slotA = parseSchedule(a);
        TimeSlot slotB = parseSchedule(b);
        if (slotA == null || slotB == null) {
            return false;
        }
        if (!slotA.day.equalsIgnoreCase(slotB.day)) {
            return false;
        }
        return slotA.startHour < slotB.endHour && slotB.startHour < slotA.endHour;
    }

    static int scheduleHours(String schedule) {
        TimeSlot slot = parseSchedule(schedule);
        if (slot == null) {
            return 0;
        }
        return Math.max(0, slot.endHour - slot.startHour);
    }

    static TimeSlot parseSchedule(String schedule) {
        if (schedule == null || schedule.isBlank()) {
            return null;
        }
        String[] parts = schedule.trim().split("_");
        if (parts.length != 3) {
            return null;
        }
        try {
            int start = Integer.parseInt(parts[1]);
            int end = Integer.parseInt(parts[2]);
            if (end <= start) {
                return null;
            }
            return new TimeSlot(parts[0], start, end);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    static final class TimeSlot {
        final String day;
        final int startHour;
        final int endHour;

        TimeSlot(String day, int startHour, int endHour) {
            this.day = day;
            this.startHour = startHour;
            this.endHour = endHour;
        }
    }

    public static class TaWorkloadSnapshot {
        private final int totalTas;
        private final int totalSelectedAssignments;
        private final int tasWithScheduleConflicts;
        private final int tasOverWeeklyThreshold;
        private final int weeklyHoursThreshold;
        private final List<TaWorkloadRow> rows;

        public TaWorkloadSnapshot(int totalTas, int totalSelectedAssignments, int tasWithScheduleConflicts,
                                  int tasOverWeeklyThreshold, int weeklyHoursThreshold, List<TaWorkloadRow> rows) {
            this.totalTas = totalTas;
            this.totalSelectedAssignments = totalSelectedAssignments;
            this.tasWithScheduleConflicts = tasWithScheduleConflicts;
            this.tasOverWeeklyThreshold = tasOverWeeklyThreshold;
            this.weeklyHoursThreshold = weeklyHoursThreshold;
            this.rows = rows;
        }

        public int getTotalTas() { return totalTas; }
        public int getTotalSelectedAssignments() { return totalSelectedAssignments; }
        public int getTasWithScheduleConflicts() { return tasWithScheduleConflicts; }
        public int getTasOverWeeklyThreshold() { return tasOverWeeklyThreshold; }
        public int getWeeklyHoursThreshold() { return weeklyHoursThreshold; }
        public List<TaWorkloadRow> getRows() { return rows; }
    }

    public static class TaWorkloadRow {
        private final String userId;
        private final String username;
        private final String displayName;
        private final String email;
        private final String major;
        private final int skillCount;
        private final boolean hasCv;
        private final boolean banned;
        private final int totalApplications;
        private final int submittedCount;
        private final int underReviewCount;
        private final int selectedCount;
        private final int rejectedCount;
        private final int pendingCount;
        private final int weeklyHours;
        private final boolean highLoad;
        private final boolean scheduleConflict;
        private final List<SelectedJobAssignment> selectedJobs;

        public TaWorkloadRow(String userId, String username, String displayName, String email, String major,
                             int skillCount, boolean hasCv, boolean banned,
                             int totalApplications, int submittedCount, int underReviewCount,
                             int selectedCount, int rejectedCount, int pendingCount,
                             int weeklyHours, boolean highLoad, boolean scheduleConflict,
                             List<SelectedJobAssignment> selectedJobs) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
            this.major = major;
            this.skillCount = skillCount;
            this.hasCv = hasCv;
            this.banned = banned;
            this.totalApplications = totalApplications;
            this.submittedCount = submittedCount;
            this.underReviewCount = underReviewCount;
            this.selectedCount = selectedCount;
            this.rejectedCount = rejectedCount;
            this.pendingCount = pendingCount;
            this.weeklyHours = weeklyHours;
            this.highLoad = highLoad;
            this.scheduleConflict = scheduleConflict;
            this.selectedJobs = selectedJobs;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public String getMajor() { return major; }
        public int getSkillCount() { return skillCount; }
        public boolean isHasCv() { return hasCv; }
        public boolean isBanned() { return banned; }
        public int getTotalApplications() { return totalApplications; }
        public int getSubmittedCount() { return submittedCount; }
        public int getUnderReviewCount() { return underReviewCount; }
        public int getSelectedCount() { return selectedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public int getPendingCount() { return pendingCount; }
        public int getWeeklyHours() { return weeklyHours; }
        public boolean isHighLoad() { return highLoad; }
        public boolean isScheduleConflict() { return scheduleConflict; }
        public List<SelectedJobAssignment> getSelectedJobs() { return selectedJobs; }
    }

    public static class SelectedJobAssignment {
        private final String jobId;
        private final String title;
        private final String schedule;
        private final String semester;
        private final int weeklyHours;

        public SelectedJobAssignment(String jobId, String title, String schedule, String semester, int weeklyHours) {
            this.jobId = jobId;
            this.title = title;
            this.schedule = schedule;
            this.semester = semester;
            this.weeklyHours = weeklyHours;
        }

        public String getJobId() { return jobId; }
        public String getTitle() { return title; }
        public String getSchedule() { return schedule; }
        public String getSemester() { return semester; }
        public int getWeeklyHours() { return weeklyHours; }
    }
}