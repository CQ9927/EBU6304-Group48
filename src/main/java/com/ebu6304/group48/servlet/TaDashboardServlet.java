package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.InvitationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "TaDashboardServlet", urlPatterns = "/ta/dashboard")
public class TaDashboardServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private ProfileRepository profileRepository;
    private InvitationRepository invitationRepository;
    private MatchingService matchingService;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        invitationRepository = new InvitationRepository(getServletContext());
        matchingService = new MatchingService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("navCurrent", "dashboard");

        // Stats
        int openJobsCount = jobRepository.findAllOpenJobs().size();
        List<Application> mine = applicationRepository.findByApplicantUserId(userId);
        int appTotal = mine.size();
        int appSubmitted = (int) mine.stream().filter(a -> "SUBMITTED".equalsIgnoreCase(a.getStatus())).count();
        int appUnderReview = (int) mine.stream().filter(a -> "UNDER_REVIEW".equalsIgnoreCase(a.getStatus())).count();
        int appSelected = (int) mine.stream().filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus())).count();
        int appRejected = (int) mine.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count();

        int pendingInvites = invitationRepository.countPendingByTaUserId(userId);
        req.setAttribute("pendingInvites", pendingInvites);

        req.setAttribute("openJobsCount", openJobsCount);
        req.setAttribute("myApplicationsTotal", appTotal);
        req.setAttribute("myApplicationsSubmitted", appSubmitted);
        req.setAttribute("myApplicationsUnderReview", appUnderReview);
        req.setAttribute("myApplicationsSelected", appSelected);
        req.setAttribute("myApplicationsRejected", appRejected);

        // Profile completeness
        Profile profile = profileRepository.findByUserId(userId);
        req.setAttribute("profile", profile);
        int profilePct = computeProfileCompleteness(profile);
        req.setAttribute("profileCompleteness", profilePct);

        // Featured jobs: open jobs with match scores, top 3
        List<Job> openJobs = jobRepository.findAllOpenJobs();
        Map<String, MatchingService.MatchResult> matchMap = new LinkedHashMap<>();
        if (profile != null) {
            for (Job job : openJobs) {
                matchMap.put(job.getJobId(), matchingService.computeMatch(job, profile));
            }
        }
        List<Job> featuredJobs = openJobs.stream()
                .sorted(Comparator.comparingInt(j -> {
                    MatchingService.MatchResult mr = matchMap.get(j.getJobId());
                    return mr != null ? -mr.getTotalScore() : 0;
                }))
                .limit(3)
                .collect(Collectors.toList());
        req.setAttribute("featuredJobs", featuredJobs);
        req.setAttribute("matchMap", matchMap);

        // Recent applications (latest 3)
        List<Application> recentApps = mine.stream()
                .sorted(Comparator.comparing(Application::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .limit(3)
                .collect(Collectors.toList());
        req.setAttribute("recentApplications", recentApps);

        // Job lookup for recent apps
        Map<String, Job> jobMap = new HashMap<>();
        for (Job j : openJobs) {
            jobMap.put(j.getJobId(), j);
        }
        for (Job j : jobRepository.findAll()) {
            jobMap.putIfAbsent(j.getJobId(), j);
        }
        req.setAttribute("jobMap", jobMap);

        req.getRequestDispatcher("/WEB-INF/jsp/ta/dashboard.jsp").forward(req, resp);
    }

    private int computeProfileCompleteness(Profile p) {
        if (p == null) return 0;
        int fields = 0;
        int filled = 0;
        // name
        fields++; if (p.getName() != null && !p.getName().isBlank()) filled++;
        // email
        fields++; if (p.getEmail() != null && !p.getEmail().isBlank()) filled++;
        // major
        fields++; if (p.getMajor() != null && !p.getMajor().isBlank()) filled++;
        // skills
        fields++; if (p.getSkills() != null && !p.getSkills().isEmpty()) filled++;
        // availability
        fields++; if (p.getAvailability() != null && !p.getAvailability().isEmpty()) filled++;
        // CV
        fields++; if (p.getCvFileName() != null && !p.getCvFileName().isBlank()) filled++;
        return fields == 0 ? 0 : filled * 100 / fields;
    }
}
