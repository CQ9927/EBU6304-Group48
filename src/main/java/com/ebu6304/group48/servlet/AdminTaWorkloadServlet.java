package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.AdminSettings;
import com.ebu6304.group48.repository.AdminSettingsRepository;
import com.ebu6304.group48.service.TaWorkloadService;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminTaWorkloadServlet", urlPatterns = "/admin/ta-workload")
public class AdminTaWorkloadServlet extends HttpServlet {

    private TaWorkloadService taWorkloadService;
    private AdminSettingsRepository settingsRepository;

    @Override
    public void init() {
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        taWorkloadService = new TaWorkloadService(getServletContext(), dataDir);
        settingsRepository = new AdminSettingsRepository(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("navCurrent", "ta-workload");

        try {
            AdminSettings settings = settingsRepository.load();
            int threshold = settings.resolvedWeeklyHoursThreshold();
            req.setAttribute("weeklyHoursThreshold", threshold);

            TaWorkloadService.TaWorkloadSnapshot snapshot = taWorkloadService.buildSnapshot(threshold);
            req.setAttribute("snapshot", snapshot);

            String focusUserId = trim(req.getParameter("userId"));
            if (!focusUserId.isEmpty()) {
                snapshot.getRows().stream()
                        .filter(r -> focusUserId.equals(r.getUserId()))
                        .findFirst()
                        .ifPresent(row -> req.setAttribute("focusRow", row));
            }
        } catch (IOException e) {
            req.setAttribute("errorMessage", "Failed to load TA workload data.");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/ta-workload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String redirect = req.getContextPath() + "/admin/ta-workload";

        try {
            int threshold = parseThreshold(req.getParameter("weeklyHoursThreshold"));
            settingsRepository.saveWeeklyHoursThreshold(threshold);
            resp.sendRedirect(redirect + "?saved=threshold");
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(redirect + "?error=invalid-threshold");
        } catch (IOException e) {
            resp.sendRedirect(redirect + "?error=save-failed");
        }
    }

    private static int parseThreshold(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Threshold required");
        }
        return Integer.parseInt(raw.trim());
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}