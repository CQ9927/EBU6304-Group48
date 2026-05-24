package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.service.AiService;
import com.ebu6304.group48.service.MatchingService;

import com.ebu6304.group48.util.SessionKeys;
import com.google.gson.Gson;
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
 * AJAX endpoint for on-demand AI analysis.
 * GET /ta/jobs/ai-analysis?jobId=xxx → match explanation
 * GET /ta/jobs/ai-analysis?type=skills&jobId=xxx → skill gap analysis
 */
@WebServlet(name = "AiAnalysisServlet", urlPatterns = "/ta/jobs/ai-analysis")
public class AiAnalysisServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private JobRepository jobRepository;
    private ProfileRepository profileRepository;
    private AiService aiService;
    private MatchingService matchingService;

    @Override
    public void init() throws ServletException {
        jobRepository = new JobRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        aiService = new AiService();
        matchingService = new MatchingService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"explanation\":null,\"error\":\"Not authenticated\"}");
            return;
        }

        String userId = (String) session.getAttribute(SessionKeys.USER_ID);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isBlank()) {
            resp.getWriter().write("{\"explanation\":null,\"error\":\"Missing jobId parameter\"}");
            return;
        }

        Job job = jobRepository.findById(jobId.trim());
        if (job == null) {
            resp.getWriter().write("{\"explanation\":null,\"error\":\"Job not found\"}");
            return;
        }

        Profile profile = profileRepository.findByUserId(userId);
        if (profile == null) {
            resp.getWriter().write("{\"explanation\":null,\"error\":\"Profile not found. Please complete your profile first.\"}");
            return;
        }

        String type = req.getParameter("type");
        JsonObject result = new JsonObject();

        if ("skills".equals(type)) {
            // Skill gap analysis
            MatchingService.MatchResult mr = matchingService.computeMatch(job, profile);
            List<String> missingSkills = mr != null ? mr.getMissingSkills() : java.util.Collections.emptyList();
            String analysis = aiService.explainMissingSkills(job, profile, missingSkills);
            if (analysis != null && !analysis.isBlank()) {
                result.addProperty("explanation", analysis.trim());
                result.add("error", null);
            } else {
                result.add("explanation", null);
                result.addProperty("error", "Skill gap analysis is currently unavailable. Please try again later.");
            }
        } else {
            // Match explanation (default)
            String explanation = aiService.explainMatch(job, profile);
            if (explanation != null && !explanation.isBlank()) {
                result.addProperty("explanation", explanation.trim());
                result.add("error", null);
            } else {
                result.add("explanation", null);
                result.addProperty("error", "AI analysis is currently unavailable. Please try again later.");
            }
        }
        resp.getWriter().write(GSON.toJson(result));
    }
}
