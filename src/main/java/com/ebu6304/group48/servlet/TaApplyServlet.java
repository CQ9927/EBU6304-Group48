package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "TaApplyServlet", urlPatterns = "/ta/apply")
public class TaApplyServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ProfileRepository profileRepository;
    private ApplicationRepository applicationRepository;

    @Override
    public void init() throws ServletException {
        jobRepository = new JobRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        submitApplication(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        submitApplication(req, resp);
    }

    private void submitApplication(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = String.valueOf(session.getAttribute(SessionKeys.USER_ID));
        String jobId = trim(req.getParameter("jobId"));
        String note = trim(req.getParameter("note"));

        Job job = jobRepository.findById(jobId);
        if (job == null || !"OPEN".equalsIgnoreCase(job.getStatus())) {
            session.setAttribute("error", "Job not found or no longer open.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }

        Profile profile = profileRepository.findByUserId(userId);
        if (profile == null || profile.getCvFileName() == null || profile.getCvFileName().isBlank()) {
            session.setAttribute("error", "Please complete profile and upload CV before applying.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }

        boolean alreadyApplied = applicationRepository.findByApplicantUserId(userId).stream()
                .anyMatch(app -> jobId.equals(app.getJobId()));
        if (alreadyApplied) {
            session.setAttribute("message", "You have already applied for this job.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }

        Application application = buildApplication(userId, job, profile, note);
        boolean success = applicationRepository.save(application);
        if (success) {
            session.setAttribute("message", "Application submitted successfully.");
        } else {
            session.setAttribute("error", "Failed to submit application. Please retry.");
        }
        resp.sendRedirect(req.getContextPath() + "/ta/jobs");
    }

    private static Application buildApplication(String userId, Job job, Profile profile, String note) {
        List<String> userSkills = profile.getSkills() != null ? profile.getSkills() : new ArrayList<>();
        List<String> requiredSkills = job.getRequiredSkills() != null ? job.getRequiredSkills() : new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        int matchingCount = 0;
        for (String required : requiredSkills) {
            if (userSkills.contains(required)) {
                matchingCount++;
            } else {
                missingSkills.add(required);
            }
        }
        int matchScore = requiredSkills.isEmpty() ? 100 : (matchingCount * 100 / requiredSkills.size());

        String now = Instant.now().toString();
        Application application = new Application();
        application.setJobId(job.getJobId());
        application.setApplicantUserId(userId);
        application.setMatchScore(matchScore);
        application.setMissingSkills(missingSkills);
        application.setStatus("SUBMITTED");
        application.setNote(note);
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        return application;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
