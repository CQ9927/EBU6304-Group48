package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.service.AiService;
import com.ebu6304.group48.util.SessionKeys;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AJAX endpoint for AI-powered workload insights.
 * GET /admin/workload/ai-insights → {"insights": "...", "error": null}
 */
@WebServlet(name = "AiWorkloadServlet", urlPatterns = "/admin/workload/ai-insights")
public class AiWorkloadServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private AiService aiService;

    @Override
    public void init() throws ServletException {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        aiService = new AiService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"insights\":null,\"error\":\"Not authenticated\"}");
            return;
        }

        JsonObject result = new JsonObject();
        try {
            String workloadJson = buildWorkloadJson();
            String insights = aiService.analyzeWorkload(workloadJson);
            if (insights != null && !insights.isBlank()) {
                result.addProperty("insights", insights.trim());
                result.add("error", null);
            } else {
                result.add("insights", null);
                result.addProperty("error", "AI workload analysis is currently unavailable. Please try again later.");
            }
        } catch (Exception e) {
            result.add("insights", null);
            result.addProperty("error", "Failed to build workload data: " + e.getMessage());
        }
        resp.getWriter().write(GSON.toJson(result));
    }

    private String buildWorkloadJson() {
        List<Job> jobs = jobRepository.findAll();
        List<Application> apps = applicationRepository.findAll();

        JsonObject data = new JsonObject();
        data.addProperty("totalJobs", jobs.size());

        // Job summary
        JsonArray jobsArr = new JsonArray();
        for (Job job : jobs) {
            if (job == null) continue;
            JsonObject j = new JsonObject();
            j.addProperty("jobId", job.getJobId());
            j.addProperty("title", job.getTitle());
            j.addProperty("type", job.getType());
            j.addProperty("status", job.getStatus());
            j.addProperty("capacity", job.getCapacity() != null ? job.getCapacity() : 0);

            // Count applications by status for this job
            long submitted = 0, underReview = 0, selected = 0, rejected = 0;
            for (Application app : apps) {
                if (app == null || !job.getJobId().equals(app.getJobId())) continue;
                String s = app.getStatus() != null ? app.getStatus().toUpperCase() : "";
                switch (s) {
                    case "SUBMITTED": submitted++; break;
                    case "UNDER_REVIEW": underReview++; break;
                    case "SELECTED": selected++; break;
                    case "REJECTED": rejected++; break;
                }
            }
            j.addProperty("submittedApps", (int) submitted);
            j.addProperty("underReviewApps", (int) underReview);
            j.addProperty("selectedApps", (int) selected);
            j.addProperty("rejectedApps", (int) rejected);
            j.addProperty("pendingCount", (int) (submitted + underReview));
            j.addProperty("overCapacity", selected > job.getCapacity());
            jobsArr.add(j);
        }
        data.add("jobs", jobsArr);

        // Overall stats
        long totalApps = apps.size();
        long totalSelected = apps.stream().filter(a -> a != null && "SELECTED".equalsIgnoreCase(
                a.getStatus() != null ? a.getStatus() : "")).count();

        data.addProperty("totalApplications", (int) totalApps);
        data.addProperty("totalSelected", (int) totalSelected);

        return GSON.toJson(data);
    }
}
