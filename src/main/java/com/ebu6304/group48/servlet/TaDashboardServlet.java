package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
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

@WebServlet(name = "TaDashboardServlet", urlPatterns = "/ta/dashboard")
public class TaDashboardServlet extends HttpServlet {

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

        int openJobsCount = jobRepository.findAllOpenJobs().size();
        List<Application> mine = applicationRepository.findByApplicantUserId(userId);
        int appTotal = mine.size();
        int appSubmitted = (int) mine.stream().filter(a -> "SUBMITTED".equalsIgnoreCase(a.getStatus())).count();
        int appUnderReview = (int) mine.stream().filter(a -> "UNDER_REVIEW".equalsIgnoreCase(a.getStatus())).count();
        int appSelected = (int) mine.stream().filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus())).count();
        int appRejected = (int) mine.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count();

        req.setAttribute("openJobsCount", openJobsCount);
        req.setAttribute("myApplicationsTotal", appTotal);
        req.setAttribute("myApplicationsSubmitted", appSubmitted);
        req.setAttribute("myApplicationsUnderReview", appUnderReview);
        req.setAttribute("myApplicationsSelected", appSelected);
        req.setAttribute("myApplicationsRejected", appRejected);

        req.getRequestDispatcher("/WEB-INF/jsp/ta/dashboard.jsp").forward(req, resp);
    }
}
