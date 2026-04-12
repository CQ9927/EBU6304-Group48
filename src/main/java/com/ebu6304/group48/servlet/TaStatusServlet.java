package com.ebu6304.group48.servlet;

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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name = "TaStatusServlet", urlPatterns = "/ta/status")
public class TaStatusServlet extends HttpServlet {

    private ApplicationRepository applicationRepository;
    private JobRepository jobRepository;

    @Override
    public void init() throws ServletException {
        applicationRepository = new ApplicationRepository(getServletContext());
        jobRepository = new JobRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = String.valueOf(session.getAttribute(SessionKeys.USER_ID));
        List<Application> applications = applicationRepository.findByApplicantUserId(userId).stream()
                .sorted(Comparator.comparing(Application::getUpdatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        Map<String, String> jobTitles = new HashMap<>();
        for (Job job : jobRepository.findAll()) {
            jobTitles.put(job.getJobId(), job.getTitle());
        }

        req.setAttribute("applications", applications);
        req.setAttribute("jobTitles", jobTitles);
        req.setAttribute("navCurrent", "status");
        req.getRequestDispatcher("/WEB-INF/jsp/ta/status.jsp").forward(req, resp);
    }
}
