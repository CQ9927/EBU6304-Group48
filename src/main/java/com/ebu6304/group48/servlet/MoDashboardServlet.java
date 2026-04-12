package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "MoDashboardServlet", urlPatterns = "/mo/dashboard")
public class MoDashboardServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        req.setAttribute("myJobsTotal", myJobsTotal);
        req.setAttribute("myOpenJobs", myOpenJobs);
        req.setAttribute("pendingApplications", pendingApplications);

        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }
}
