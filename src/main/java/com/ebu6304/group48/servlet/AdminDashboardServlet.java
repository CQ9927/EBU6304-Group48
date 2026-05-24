package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.repository.UserRepository;
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
    private UserRepository userRepository;

    @Override
    public void init() {
        this.workloadService = new WorkloadService(getServletContext(), new MatchingService());
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        this.userRepository = new UserRepository(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("navCurrent", "workload");
        req.setAttribute("snapshot", workloadService.buildSnapshot());

        // User count for admin metrics
        int totalUsers = 0;
        try {
            totalUsers = userRepository.findAll().size();
        } catch (Exception ignored) {
        }
        req.setAttribute("totalUsers", totalUsers);

        // Alert count for admin metrics
        int alertCount = 0;
        var snapshot = req.getAttribute("snapshot");
        if (snapshot instanceof WorkloadService.WorkloadSnapshot) {
            WorkloadService.WorkloadSnapshot s = (WorkloadService.WorkloadSnapshot) snapshot;
            alertCount = s.getHints().size();
            if (s.getOpenJobsWithoutSelection() > 0) alertCount++;
            for (var row : s.getRows()) {
                if (row.isOverCapacity()) alertCount++;
            }
        }
        req.setAttribute("alertCount", alertCount);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/workload.jsp").forward(req, resp);
    }
}
