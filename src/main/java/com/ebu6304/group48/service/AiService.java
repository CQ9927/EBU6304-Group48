package com.ebu6304.group48.service;

import com.ebu6304.group48.config.AiConfig;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Integrates with DeepSeek LLM API for CV analysis and job-match explanation.
 * All public methods return null on failure — the system degrades gracefully
 * when the API key is missing or the network is unavailable.
 */
public class AiService {

    private static final Gson GSON = new Gson();
    private final HttpClient httpClient;

    public AiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ---- Public API ----

    /**
     * Analyses CV text and returns structured information.
     *
     * @param cvText plain-text CV content (UTF-8)
     * @return analysis result, or null on failure
     */
    public CvAnalysisResult analyzeCv(String cvText) {
        if (!AiConfig.isAvailable() || cvText == null || cvText.isBlank()) {
            return null;
        }

        // Truncate very long CVs to avoid token limits
        String text = cvText.length() > 6000 ? cvText.substring(0, 6000) : cvText;

        String systemPrompt = "You are a CV analyzer for a university TA recruitment system. "
                + "Extract key information from the CV text. "
                + "Focus on technical skills, teaching experience, academic background. "
                + "Return ONLY valid JSON (no markdown fences, no explanation): "
                + "{\"skills\":[\"skill1\",\"skill2\"],\"summary\":\"2-3 sentence professional summary\"}";

        String userPrompt = "CV Text:\n" + text;

        try {
            String raw = callDeepSeek(systemPrompt, userPrompt, 400, 30);
            if (raw == null) return null;
            return parseCvResult(raw);
        } catch (Exception e) {
            System.err.println("[AiService] CV analysis failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Explains in natural language why a candidate matches (or doesn't match) a job.
     *
     * @return explanation text, or null on failure
     */
    public String explainMatch(Job job, Profile profile) {
        if (!AiConfig.isAvailable() || job == null || profile == null) {
            return null;
        }

        String systemPrompt = "You are a TA recruitment advisor. "
                + "Explain the fit between a job and a candidate. "
                + "Prepend each line with a status icon based on match quality:\n"
                + "✅ for good match, ⚠️ for partial match, ❌ for poor match.\n"
                + "Format EXACTLY like this:\n"
                + "✅ Skills: <analysis of skill match, mention **matched skills**>\n"
                + "✅/⚠️ Schedule: <schedule compatibility>\n"
                + "✅/⚠️ Major: <major relevance>\n"
                + "✅/⚠️/❌ Verdict: <1-sentence overall, bold the recommendation>\n"
                + "Keep each to 1 sentence. Use ** around key skills. Return ONLY these lines.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Job: ").append(job.getTitle())
                .append(" (").append(job.getType() != null ? job.getType() : "N/A").append(")\n");
        userPrompt.append("Required Skills: ").append(formatList(job.getRequiredSkills())).append("\n");
        userPrompt.append("Schedule: ").append(job.getSchedule() != null ? job.getSchedule() : "Flexible").append("\n");
        userPrompt.append("Semester: ").append(job.getSemester() != null ? job.getSemester() : "N/A").append("\n");
        userPrompt.append("\nCandidate Profile:\n");
        userPrompt.append("Major: ").append(profile.getMajor() != null ? profile.getMajor() : "N/A").append("\n");
        userPrompt.append("Skills: ").append(formatList(profile.getSkills())).append("\n");
        userPrompt.append("Availability: ").append(formatList(profile.getAvailability())).append("\n");

        try {
            return callDeepSeek(systemPrompt, userPrompt.toString(), 200, 15);
        } catch (Exception e) {
            System.err.println("[AiService] Match explanation failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Analyses skill gaps and gives actionable advice on how to fill them.
     * Combines the rule-based missing-skills list with AI-powered learning suggestions.
     *
     * @param job           the target job
     * @param profile       the candidate profile
     * @param missingSkills pre-computed list of missing skill names (from MatchingService)
     * @return natural-language gap analysis with learning suggestions, or null on failure
     */
    public String explainMissingSkills(Job job, Profile profile, List<String> missingSkills) {
        if (!AiConfig.isAvailable() || job == null || profile == null) {
            return null;
        }

        String systemPrompt = "You are a career advisor for university teaching assistants. "
                + "Given a job's required skills and a candidate's skill gaps, "
                + "give specific, actionable suggestions. Format EXACTLY like this:\n"
                + "1. **MissingSkill** — One sentence on how to acquire it (course, project, certification).\n"
                + "2. **MissingSkill** — One sentence on how to demonstrate it.\n"
                + "Return ONLY the numbered list, no intro, no outro.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Job: ").append(job.getTitle())
                .append(" (").append(job.getType() != null ? job.getType() : "N/A").append(")\n");
        userPrompt.append("Job requires: ").append(formatList(job.getRequiredSkills())).append("\n");
        userPrompt.append("\nCandidate: ").append(profile.getMajor() != null ? profile.getMajor() : "N/A")
                .append(" major\n");
        userPrompt.append("Candidate has: ").append(formatList(profile.getSkills())).append("\n");
        userPrompt.append("\nMissing skills: ").append(missingSkills != null && !missingSkills.isEmpty()
                ? String.join(", ", missingSkills) : "(none — candidate meets all requirements)");

        try {
            return callDeepSeek(systemPrompt, userPrompt.toString(), 250, 15);
        } catch (Exception e) {
            System.err.println("[AiService] Missing skills analysis failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Analyses workload data across all jobs and applications, returning natural-language
     * insights and recommendations for the admin.
     *
     * @param workloadJson a JSON summary of jobs, applications, and selection counts
     * @return insights text, or null on failure
     */
    public String analyzeWorkload(String workloadJson) {
        if (!AiConfig.isAvailable() || workloadJson == null || workloadJson.isBlank()) {
            return null;
        }

        String systemPrompt = "You are a workload analyst for a university TA recruitment system. "
                + "Analyse the workload data and identify risks, bottlenecks, and imbalances. "
                + "Format EXACTLY like this, one insight per line:\n"
                + "🔴 <critical issue>: <specific job/TA name>, <numbers>, <1-sentence fix>\n"
                + "🟡 <warning>: <specific issue>, <1-sentence suggestion>\n"
                + "💡 <recommendation>: <actionable 1-sentence advice>\n"
                + "Use 🔴/🟡/💡 prefixes. Keep each line to 1 sentence. Return ONLY the insights, no intro.";

        try {
            return callDeepSeek(systemPrompt, workloadJson, 350, 20);
        } catch (Exception e) {
            System.err.println("[AiService] Workload analysis failed: " + e.getMessage());
            return null;
        }
    }

    // ---- Internal ----

    private String callDeepSeek(String systemPrompt, String userPrompt, int maxTokens, int timeoutSeconds) {
        String apiKey = AiConfig.getApiKey();
        if (apiKey == null) return null;

        JsonObject body = new JsonObject();
        body.addProperty("model", AiConfig.getModel());
        body.addProperty("temperature", 0.3);
        body.addProperty("max_tokens", maxTokens);

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);

        body.add("messages", GSON.toJsonTree(new JsonObject[]{sysMsg, userMsg}));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AiConfig.getApiUrl()))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[AiService] API returned status " + response.statusCode()
                        + ": " + response.body());
                return null;
            }
            JsonObject respJson = JsonParser.parseString(response.body()).getAsJsonObject();
            if (respJson == null || !respJson.has("choices")) return null;
            var choices = respJson.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) return null;
            var first = choices.get(0).getAsJsonObject();
            if (first == null || !first.has("message")) return null;
            var msg = first.getAsJsonObject("message");
            if (msg == null || !msg.has("content")) return null;
            return msg.get("content").getAsString();
        } catch (IOException | InterruptedException e) {
            System.err.println("[AiService] API call failed: " + e.getMessage());
            return null;
        }
    }

    private CvAnalysisResult parseCvResult(String raw) {
        String json = raw.trim();

        // Strip markdown code fences if present
        if (json.startsWith("```")) {
            int start = json.indexOf('\n');
            int end = json.lastIndexOf("```");
            if (start >= 0 && end > start) {
                json = json.substring(start + 1, end).trim();
            }
        }

        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            List<String> skills = new ArrayList<>();
            if (obj.has("skills") && obj.get("skills").isJsonArray()) {
                obj.getAsJsonArray("skills").forEach(e -> {
                    String s = e.getAsString().trim();
                    if (!s.isEmpty()) skills.add(s);
                });
            }
            String summary = obj.has("summary") ? obj.get("summary").getAsString().trim() : "";
            if (skills.isEmpty() && summary.isEmpty()) return null;
            return new CvAnalysisResult(skills, summary);
        } catch (Exception e) {
            System.err.println("[AiService] Failed to parse CV result: " + e.getMessage());
            return null;
        }
    }

    private static String formatList(List<String> list) {
        if (list == null || list.isEmpty()) return "(none listed)";
        return String.join(", ", list);
    }

    // ---- Result types ----

    public static class CvAnalysisResult {
        private final List<String> skills;
        private final String summary;

        public CvAnalysisResult(List<String> skills, String summary) {
            this.skills = Collections.unmodifiableList(new ArrayList<>(skills));
            this.summary = summary;
        }

        public List<String> getSkills() {
            return skills;
        }

        public String getSummary() {
            return summary;
        }

        public boolean isEmpty() {
            return skills.isEmpty() && (summary == null || summary.isEmpty());
        }
    }
}
