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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "MoDashboardServlet", urlPatterns = "/mo/dashboard")
public class MoDashboardServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private ProfileRepository profileRepository;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = trim(req.getParameter("action"));
        String jobId = trim(req.getParameter("jobId"));
        String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        if ("close".equals(action) && !jobId.isEmpty()) {
            Job job = jobRepository.findById(jobId);
            if (job != null && userId.equals(job.getPostedByUserId())
                    && "OPEN".equalsIgnoreCase(job.getStatus() != null ? job.getStatus() : "")) {
                jobRepository.updateStatus(jobId, "CLOSED");
                resp.sendRedirect(req.getContextPath() + "/mo/dashboard?closed=1");
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard?error=1");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object flashError = req.getSession().getAttribute(SessionKeys.MO_POST_JOB_ERROR);
        if (flashError != null) {
            req.setAttribute("error", flashError);
            req.getSession().removeAttribute(SessionKeys.MO_POST_JOB_ERROR);
        }

        String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("navCurrent", "dashboard");

        List<Job> myJobs = jobRepository.findAll().stream()
                .filter(j -> userId.equals(j.getPostedByUserId()))
                .collect(Collectors.toList());
        int myJobsTotal = myJobs.size();
        int myOpenJobs = (int) myJobs.stream()
                .filter(j -> "OPEN".equalsIgnoreCase(j.getStatus()))
                .count();

        Set<String> myJobIds = myJobs.stream().map(Job::getJobId).collect(Collectors.toSet());
        int pendingApplications = (int) applicationRepository.findAll().stream()
                .filter(a -> myJobIds.contains(a.getJobId()))
                .filter(a -> {
                    String s = a.getStatus();
                    return "SUBMITTED".equalsIgnoreCase(s) || "UNDER_REVIEW".equalsIgnoreCase(s);
                })
                .count();

        // Per-job applicant breakdown
        Map<String, Integer> submittedByJob = new HashMap<>();
        Map<String, Integer> underReviewByJob = new HashMap<>();
        Map<String, Integer> selectedByJob = new HashMap<>();
        Map<String, Integer> rejectedByJob = new HashMap<>();
        Map<String, Integer> totalByJob = new HashMap<>();
        for (Application app : applicationRepository.findAll()) {
            if (app == null || !myJobIds.contains(app.getJobId())) continue;
            String s = app.getStatus() != null ? app.getStatus().toUpperCase().trim() : "";
            totalByJob.merge(app.getJobId(), 1, Integer::sum);
            if ("SUBMITTED".equals(s)) submittedByJob.merge(app.getJobId(), 1, Integer::sum);
            else if ("UNDER_REVIEW".equals(s)) underReviewByJob.merge(app.getJobId(), 1, Integer::sum);
            else if ("SELECTED".equals(s)) selectedByJob.merge(app.getJobId(), 1, Integer::sum);
            else if ("REJECTED".equals(s)) rejectedByJob.merge(app.getJobId(), 1, Integer::sum);
        }

        // Build map: jobId -> list of selected TA profiles
        Map<String, List<Profile>> selectedProfilesByJob = new HashMap<>();
        for (Application app : applicationRepository.findAll()) {
            if (app == null || !myJobIds.contains(app.getJobId())) continue;
            if (!"SELECTED".equalsIgnoreCase(app.getStatus() != null ? app.getStatus() : "")) continue;
            Profile p = profileRepository.findByUserId(app.getApplicantUserId());
            if (p != null) {
                selectedProfilesByJob.computeIfAbsent(app.getJobId(), k -> new ArrayList<>()).add(p);
            }
        }

        req.setAttribute("myJobsTotal", myJobsTotal);
        req.setAttribute("myOpenJobs", myOpenJobs);
        req.setAttribute("pendingApplications", pendingApplications);
        req.setAttribute("jobs", myJobs);
        req.setAttribute("submittedByJob", submittedByJob);
        req.setAttribute("underReviewByJob", underReviewByJob);
        req.setAttribute("selectedByJob", selectedByJob);
        req.setAttribute("rejectedByJob", rejectedByJob);
        req.setAttribute("totalByJob", totalByJob);
        req.setAttribute("selectedProfilesByJob", selectedProfilesByJob);

        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
