package com.ebu6304.group48.servlet;

import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.service.WorkloadService;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = "/admin/workload")
public class AdminDashboardServlet extends HttpServlet {

    private WorkloadService workloadService;

    @Override
    public void init() {
        this.workloadService = new WorkloadService(getServletContext(), new MatchingService());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("snapshot", workloadService.buildSnapshot());
        req.getRequestDispatcher("/WEB-INF/jsp/admin/workload.jsp").forward(req, resp);
    }
}
