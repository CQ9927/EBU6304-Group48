package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Application;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminApplicationsServlet", urlPatterns = "/admin/applications")
public class AdminApplicationsServlet extends HttpServlet {

    private ApplicationRepository applicationRepository;
    private JobRepository jobRepository;

    @Override
    public void init() {
        applicationRepository = new ApplicationRepository(getServletContext());
        jobRepository = new JobRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Application> applications = applicationRepository.findAll();
        Map<String, String> jobTitles = new HashMap<>();
        for (Job j : jobRepository.findAll()) {
            if (j.getJobId() != null) {
                jobTitles.put(j.getJobId(), j.getTitle() != null ? j.getTitle() : j.getJobId());
            }
        }
        req.setAttribute("applications", applications);
        req.setAttribute("jobTitles", jobTitles);
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("navCurrent", "applications");
        req.getRequestDispatcher("/WEB-INF/jsp/admin/applications.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String applicationId = trim(req.getParameter("applicationId"));
        String adminUserId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));
        String ctx = req.getContextPath();

        if (applicationId.isEmpty()) {
            resp.sendRedirect(ctx + "/admin/applications?error=invalid");
            return;
        }

        boolean ok = applicationRepository.rejectByAdmin(applicationId, adminUserId);
        if (ok) {
            resp.sendRedirect(ctx + "/admin/applications?saved=1");
        } else {
            resp.sendRedirect(ctx + "/admin/applications?error=revoke");
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
