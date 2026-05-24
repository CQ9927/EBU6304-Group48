package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.service.AiService;
import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.util.SessionKeys;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "MascotChatServlet", urlPatterns = "/api/mascot/chat")
public class MascotChatServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().create();

    private JobRepository jobRepository;
    private ProfileRepository profileRepository;
    private ApplicationRepository applicationRepository;
    private MatchingService matchingService;
    private AiService aiService;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        matchingService = new MatchingService();
        aiService = new AiService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Auth check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"reply\":null,\"cards\":[],\"intent\":\"chat\",\"error\":\"Not authenticated\"}");
            return;
        }

        String userId = String.valueOf(session.getAttribute(SessionKeys.USER_ID));
        String role = String.valueOf(session.getAttribute(SessionKeys.ROLE));

        // Read request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String message;
        try {
            JsonObject reqJson = JsonParser.parseString(sb.toString()).getAsJsonObject();
            message = reqJson.has("message") ? reqJson.get("message").getAsString().trim() : "";
        } catch (Exception e) {
            resp.getWriter().write("{\"reply\":\"I didn't catch that. Can you say it again?\",\"cards\":[],\"intent\":\"chat\",\"error\":null}");
            return;
        }

        if (message.isEmpty()) {
            resp.getWriter().write("{\"reply\":\"Hi! What can I help you with today?\",\"cards\":[],\"intent\":\"chat\",\"error\":null}");
            return;
        }

        try {
            // Build context
            Profile profile = profileRepository.findByUserId(userId);
            List<Job> openJobs = jobRepository.findAllOpenJobs();
            List<Application> myApps = applicationRepository.findByApplicantUserId(userId);
            List<Application> allApps = applicationRepository.findAll();

            // Compute matches for all open jobs
            Map<String, MatchingService.MatchResult> matchMap = new LinkedHashMap<>();
            if (profile != null) {
                for (Job job : openJobs) {
                    matchMap.put(job.getJobId(), matchingService.computeMatch(job, profile));
                }
            }

            // Build context JSON for AI
            JsonObject context = new JsonObject();
            context.addProperty("userMessage", message);
            context.addProperty("userRole", role);

            // Profile
            JsonObject profileJson = new JsonObject();
            if (profile != null) {
                profileJson.addProperty("name", nvl(profile.getName()));
                profileJson.addProperty("major", nvl(profile.getMajor()));
                JsonArray skillsArr = new JsonArray();
                if (profile.getSkills() != null) profile.getSkills().forEach(skillsArr::add);
                profileJson.add("skills", skillsArr);
                JsonArray availArr = new JsonArray();
                if (profile.getAvailability() != null) profile.getAvailability().forEach(availArr::add);
                profileJson.add("availability", availArr);
                profileJson.addProperty("hasCV", profile.getCvFileName() != null && !profile.getCvFileName().isBlank());
            } else {
                profileJson.addProperty("name", "(no profile)");
                profileJson.addProperty("major", "");
                profileJson.add("skills", new JsonArray());
                profileJson.add("availability", new JsonArray());
                profileJson.addProperty("hasCV", false);
            }
            context.add("userProfile", profileJson);

            // Open jobs (send max 10 for context size)
            JsonArray jobsArr = new JsonArray();
            int count = 0;
            for (Job job : openJobs) {
                if (count++ >= 10) break;
                JsonObject jobObj = new JsonObject();
                jobObj.addProperty("jobId", job.getJobId());
                jobObj.addProperty("title", nvl(job.getTitle()));
                jobObj.addProperty("type", nvl(job.getType()));
                jobObj.addProperty("schedule", nvl(job.getSchedule()));
                jobObj.addProperty("capacity", job.getCapacity() != null ? job.getCapacity() : 0);
                JsonArray reqSkills = new JsonArray();
                if (job.getRequiredSkills() != null) job.getRequiredSkills().forEach(reqSkills::add);
                jobObj.add("requiredSkills", reqSkills);
                MatchingService.MatchResult mr = matchMap.get(job.getJobId());
                jobObj.addProperty("matchScore", mr != null ? mr.getTotalScore() : 0);
                jobsArr.add(jobObj);
            }
            context.add("openJobs", jobsArr);

            // My applications
            JsonArray appsArr = new JsonArray();
            Set<String> appliedJobIds = new HashSet<>();
            for (Application app : myApps) {
                JsonObject appObj = new JsonObject();
                appObj.addProperty("jobId", nvl(app.getJobId()));
                appObj.addProperty("status", nvl(app.getStatus()));
                appObj.addProperty("matchScore", app.getMatchScore() != null ? app.getMatchScore() : 0);
                appsArr.add(appObj);
                if (app.getJobId() != null) appliedJobIds.add(app.getJobId());
            }
            context.add("myApplications", appsArr);

            // MO context: their own jobs with applicant counts
            if ("MO".equals(role)) {
                List<Job> allJobs = jobRepository.findAll();
                List<Job> moJobs = allJobs.stream()
                        .filter(j -> userId.equals(j.getPostedByUserId()))
                        .collect(Collectors.toList());
                JsonArray moJobsArr = new JsonArray();
                for (Job j : moJobs) {
                    JsonObject jo = new JsonObject();
                    jo.addProperty("jobId", j.getJobId());
                    jo.addProperty("title", nvl(j.getTitle()));
                    jo.addProperty("status", nvl(j.getStatus()));
                    long appCount = allApps.stream().filter(a -> j.getJobId().equals(a.getJobId())).count();
                    jo.addProperty("applicantCount", (int) appCount);
                    moJobsArr.add(jo);
                }
                context.add("myJobs", moJobsArr);
            }

            // Admin context: workload snapshot
            if ("ADMIN".equals(role)) {
                List<Job> allJobs = jobRepository.findAll();
                int overCap = 0;
                int noSelection = 0;
                for (Job j : allJobs) {
                    long selected = allApps.stream().filter(a -> j.getJobId().equals(a.getJobId()) && "SELECTED".equalsIgnoreCase(nvl(a.getStatus()))).count();
                    if (selected > (j.getCapacity() != null ? j.getCapacity() : 0)) overCap++;
                    if ("OPEN".equalsIgnoreCase(nvl(j.getStatus())) && selected == 0) noSelection++;
                }
                JsonObject adminCtx = new JsonObject();
                adminCtx.addProperty("totalJobs", allJobs.size());
                adminCtx.addProperty("totalApplications", allApps.size());
                adminCtx.addProperty("overCapacityCount", overCap);
                adminCtx.addProperty("openWithoutSelection", noSelection);
                context.add("adminSummary", adminCtx);
            }

            // Call AI
            AiService.ChatResult chatResult = aiService.chatWithMascot(GSON.toJson(context));

            // Build response
            JsonObject response = new JsonObject();
            response.addProperty("error", (String) null);

            if (chatResult == null) {
                response.addProperty("reply",
                        "Hi! I'm Tabi the owl \uD83E\uDD89 I can help you find TA jobs, check your status, review applicants, and more. "
                                + "What can I do for you today?");
                response.add("cards", new JsonArray());
                response.addProperty("intent", "chat");
            } else {
                response.addProperty("reply", chatResult.getReply());
                String intent = chatResult.getIntent();
                response.addProperty("intent", intent);

                JsonArray cards = new JsonArray();

                switch (intent) {
                    case "find_jobs":
                        for (String jobId : chatResult.getJobIds()) {
                            JsonObject card = buildJobCard(jobId, openJobs, matchMap, appliedJobIds);
                            if (card != null) { card.addProperty("cardType", "job_card"); cards.add(card); }
                        }
                        break;

                    case "check_status":
                        for (String jobId : chatResult.getJobIds()) {
                            JsonObject card = buildStatusCard(jobId, myApps, openJobs);
                            if (card != null) { card.addProperty("cardType", "status_card"); cards.add(card); }
                        }
                        break;

                    case "check_profile":
                        cards.add(buildProfileCard(profile));
                        break;

                    case "review_applicants":
                        for (String jobId : chatResult.getJobIds()) {
                            JsonObject card = buildApplicantCard(jobId, allApps, openJobs);
                            if (card != null) { card.addProperty("cardType", "applicant_card"); cards.add(card); }
                        }
                        break;

                    case "admin_alerts":
                        for (JsonObject card : buildAdminAlertCards(allApps, openJobs)) {
                            card.addProperty("cardType", "alert_card");
                            cards.add(card);
                        }
                        break;
                }
                response.add("cards", cards);
            }

            resp.getWriter().write(GSON.toJson(response));

        } catch (Exception e) {
            // Degraded fallback
            resp.getWriter().write("{\"reply\":\""
                    + "Oops! Something went wrong on my end. Please try asking me again in a moment."
                    + "\",\"cards\":[],\"intent\":\"chat\",\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private JsonObject buildJobCard(String jobId, List<Job> openJobs, Map<String, MatchingService.MatchResult> matchMap, Set<String> appliedJobIds) {
        Job job = findJob(openJobs, jobId);
        if (job == null) job = findJob(jobRepository.findAll(), jobId);
        if (job == null) return null;
        MatchingService.MatchResult mr = matchMap.get(jobId);
        JsonObject card = new JsonObject();
        card.addProperty("jobId", job.getJobId());
        card.addProperty("title", nvl(job.getTitle()));
        card.addProperty("type", nvl(job.getType()));
        card.addProperty("schedule", nvl(job.getSchedule()));
        card.addProperty("semester", nvl(job.getSemester()));
        card.addProperty("capacity", job.getCapacity() != null ? job.getCapacity() : 0);
        card.addProperty("matchScore", mr != null ? mr.getTotalScore() : 0);
        card.addProperty("applied", appliedJobIds.contains(jobId));
        card.addProperty("status", nvl(job.getStatus()));
        JsonArray matched = new JsonArray();
        if (mr != null && mr.getMatchedSkills() != null) mr.getMatchedSkills().forEach(matched::add);
        card.add("matchedSkills", matched);
        JsonArray missing = new JsonArray();
        if (mr != null && mr.getMissingSkills() != null) mr.getMissingSkills().forEach(missing::add);
        card.add("missingSkills", missing);
        return card;
    }

    private JsonObject buildStatusCard(String jobId, List<Application> myApps, List<Job> openJobs) {
        Application app = myApps.stream().filter(a -> jobId.equals(a.getJobId())).findFirst().orElse(null);
        Job job = findJob(openJobs, jobId);
        if (job == null) job = findJob(jobRepository.findAll(), jobId);
        if (job == null) return null;
        JsonObject card = new JsonObject();
        card.addProperty("jobId", job.getJobId());
        card.addProperty("title", nvl(job.getTitle()));
        card.addProperty("appStatus", app != null ? nvl(app.getStatus()) : "NOT_APPLIED");
        card.addProperty("matchScore", app != null && app.getMatchScore() != null ? app.getMatchScore() : 0);
        return card;
    }

    private JsonObject buildProfileCard(Profile profile) {
        JsonObject card = new JsonObject();
        card.addProperty("cardType", "profile_card");
        boolean hasName = profile != null && profile.getName() != null && !profile.getName().isBlank();
        boolean hasEmail = profile != null && profile.getEmail() != null && !profile.getEmail().isBlank();
        boolean hasMajor = profile != null && profile.getMajor() != null && !profile.getMajor().isBlank();
        boolean hasSkills = profile != null && profile.getSkills() != null && !profile.getSkills().isEmpty();
        boolean hasAvail = profile != null && profile.getAvailability() != null && !profile.getAvailability().isEmpty();
        boolean hasCV = profile != null && profile.getCvFileName() != null && !profile.getCvFileName().isBlank();
        int filled = (hasName?1:0)+(hasEmail?1:0)+(hasMajor?1:0)+(hasSkills?1:0)+(hasAvail?1:0)+(hasCV?1:0);
        int pct = filled * 100 / 6;
        card.addProperty("pct", pct);
        card.addProperty("missingName", !hasName);
        card.addProperty("missingEmail", !hasEmail);
        card.addProperty("missingMajor", !hasMajor);
        card.addProperty("missingSkills", !hasSkills);
        card.addProperty("missingAvail", !hasAvail);
        card.addProperty("missingCV", !hasCV);
        card.addProperty("hasCV", hasCV);
        return card;
    }

    private JsonObject buildApplicantCard(String jobId, List<Application> allApps, List<Job> openJobs) {
        Job job = findJob(openJobs, jobId);
        if (job == null) job = findJob(jobRepository.findAll(), jobId);
        if (job == null) return null;
        JsonObject card = new JsonObject();
        card.addProperty("jobId", job.getJobId());
        card.addProperty("title", nvl(job.getTitle()));
        card.addProperty("status", nvl(job.getStatus()));
        card.addProperty("capacity", job.getCapacity() != null ? job.getCapacity() : 0);
        long total = allApps.stream().filter(a -> jobId.equals(a.getJobId())).count();
        long submitted = allApps.stream().filter(a -> jobId.equals(a.getJobId()) && "SUBMITTED".equalsIgnoreCase(nvl(a.getStatus()))).count();
        long underReview = allApps.stream().filter(a -> jobId.equals(a.getJobId()) && "UNDER_REVIEW".equalsIgnoreCase(nvl(a.getStatus()))).count();
        card.addProperty("totalApplicants", (int) total);
        card.addProperty("submittedCount", (int) submitted);
        card.addProperty("underReviewCount", (int) underReview);
        return card;
    }

    private List<JsonObject> buildAdminAlertCards(List<Application> allApps, List<Job> openJobs) {
        List<JsonObject> cards = new ArrayList<>();
        List<Job> allJobs = jobRepository.findAll();
        for (Job j : allJobs) {
            long selected = allApps.stream().filter(a -> j.getJobId().equals(a.getJobId()) && "SELECTED".equalsIgnoreCase(nvl(a.getStatus()))).count();
            int cap = j.getCapacity() != null ? j.getCapacity() : 0;
            if (selected > cap) {
                JsonObject card = new JsonObject();
                card.addProperty("jobId", j.getJobId());
                card.addProperty("title", nvl(j.getTitle()));
                card.addProperty("alertType", "over_capacity");
                card.addProperty("selected", (int) selected);
                card.addProperty("capacity", cap);
                cards.add(card);
            }
            if ("OPEN".equalsIgnoreCase(nvl(j.getStatus())) && selected == 0) {
                JsonObject card = new JsonObject();
                card.addProperty("jobId", j.getJobId());
                card.addProperty("title", nvl(j.getTitle()));
                card.addProperty("alertType", "no_selection");
                card.addProperty("capacity", cap);
                cards.add(card);
                if (cards.size() >= 5) break;
            }
        }
        return cards;
    }

    private Job findJob(List<Job> jobs, String jobId) {
        if (jobId == null || jobs == null) return null;
        return jobs.stream()
                .filter(j -> jobId.equals(j.getJobId()))
                .findFirst()
                .orElse(null);
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
