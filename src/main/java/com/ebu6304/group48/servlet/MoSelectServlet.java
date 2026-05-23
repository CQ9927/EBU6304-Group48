package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.service.MatchingService.MatchResult;
import com.ebu6304.group48.util.SessionKeys;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "MoSelectServlet", urlPatterns = "/mo/jobs/select")
public class MoSelectServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_LIST_TYPE = new TypeToken<List<Map<String, String>>>() { }.getType();
    private static final Object FILE_LOCK = new Object();

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private ProfileRepository profileRepository;
    private MatchingService matchingService;
    private Path selectionFile;
    private Path cvsDir;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        matchingService = new MatchingService();
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        selectionFile = Path.of(dataDir, "selection.json");
        cvsDir = Path.of(dataDir, "cvs");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle CV download
        String download = trim(req.getParameter("download"));
        if (!download.isEmpty()) {
            handleCvDownload(req, resp, download);
            return;
        }

        List<Job> jobs = jobRepository.findAll().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        String selectedJobId = trim(req.getParameter("jobId"));
        if (selectedJobId.isEmpty() && !jobs.isEmpty()) {
            selectedJobId = jobs.get(0).getJobId();
        }

        final String selectedJobIdFinal = selectedJobId;
        List<Application> allApplications = applicationRepository.findAll();
        List<Application> filteredApplications = allApplications.stream()
                .filter(a -> selectedJobIdFinal.isEmpty() || selectedJobIdFinal.equals(a.getJobId()))
                .collect(Collectors.toList());

        // Build job map for quick lookup
        Map<String, Job> jobMap = new HashMap<>();
        for (Job job : jobs) {
            if (job != null && job.getJobId() != null) {
                jobMap.put(job.getJobId(), job);
            }
        }

        // Build applicant profile map
        Map<String, Profile> applicantProfileMap = new HashMap<>();
        for (Application app : filteredApplications) {
            if (app.getApplicantUserId() != null && !applicantProfileMap.containsKey(app.getApplicantUserId())) {
                Profile p = profileRepository.findByUserId(app.getApplicantUserId());
                if (p != null) {
                    applicantProfileMap.put(app.getApplicantUserId(), p);
                }
            }
        }

        // Build match result map
        Map<String, MatchResult> matchResultMap = new HashMap<>();
        for (Application app : filteredApplications) {
            Job job = jobMap.get(app.getJobId());
            Profile profile = applicantProfileMap.get(app.getApplicantUserId());
            if (job != null && profile != null) {
                matchResultMap.put(app.getApplicationId(), matchingService.computeMatch(job, profile));
            }
        }

        // Count selected per job
        Map<String, Integer> selectedCountByJob = new HashMap<>();
        for (Application app : allApplications) {
            if (app.getJobId() != null && "SELECTED".equalsIgnoreCase(trim(app.getStatus()))) {
                selectedCountByJob.merge(app.getJobId(), 1, Integer::sum);
            }
        }

        // Sort applications by match score descending
        List<Application> sortedApplications = new ArrayList<>(filteredApplications);
        sortedApplications.sort((a, b) -> {
            int scoreA = matchResultMap.containsKey(a.getApplicationId())
                    ? matchResultMap.get(a.getApplicationId()).getTotalScore() : 0;
            int scoreB = matchResultMap.containsKey(b.getApplicationId())
                    ? matchResultMap.get(b.getApplicationId()).getTotalScore() : 0;
            return Integer.compare(scoreB, scoreA);
        });

        req.setAttribute("jobs", jobs);
        req.setAttribute("selectedJobId", selectedJobId);
        req.setAttribute("applications", sortedApplications);
        req.setAttribute("applicantProfileMap", applicantProfileMap);
        req.setAttribute("matchResultMap", matchResultMap);
        req.setAttribute("selectedCountByJob", selectedCountByJob);
        req.setAttribute("jobMap", jobMap);
        req.setAttribute("navCurrent", "select");
        req.getRequestDispatcher("/WEB-INF/jsp/mo/select.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = trim(req.getParameter("action"));
        String selectedJobId = trim(req.getParameter("jobId"));
        String reviewerUserId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        // Batch action handling
        if ("batch".equals(action)) {
            handleBatch(req, resp, selectedJobId, reviewerUserId);
            return;
        }

        String applicationId = trim(req.getParameter("applicationId"));
        String decision = trim(req.getParameter("decision")).toUpperCase();

        if (applicationId.isEmpty() ||
                (!"UNDER_REVIEW".equals(decision) && !"SELECTED".equals(decision) && !"REJECTED".equals(decision))) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=1");
            return;
        }

        // Check status transition: terminal states cannot be changed
        Application existing = applicationRepository.findById(applicationId);
        if (existing != null) {
            String currentStatus = trim(existing.getStatus()).toUpperCase();
            if ("SELECTED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
                resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=final");
                return;
            }
        }

        // Capacity check for SELECT
        if ("SELECTED".equals(decision) && existing != null) {
            Job job = jobRepository.findById(existing.getJobId());
            if (job != null) {
                int capacity = job.getCapacity() != null ? job.getCapacity() : 0;
                long alreadySelected = applicationRepository.findAll().stream()
                        .filter(a -> existing.getJobId().equals(a.getJobId())
                                && "SELECTED".equalsIgnoreCase(trim(a.getStatus())))
                        .count();
                if (alreadySelected >= capacity) {
                    resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=capacity");
                    return;
                }
            }
        }

        boolean updated = updateApplicationAndSelection(applicationId, decision, reviewerUserId);

        // Auto-close job when capacity is reached after SELECT
        if ("SELECTED".equals(decision) && updated && existing != null) {
            Job job = jobRepository.findById(existing.getJobId());
            if (job != null && "OPEN".equalsIgnoreCase(
                    job.getStatus() != null ? job.getStatus() : "")) {
                int cap = job.getCapacity() != null ? Math.max(job.getCapacity(), 0) : 0;
                long selectedCount = applicationRepository.findAll().stream()
                        .filter(a -> existing.getJobId().equals(a.getJobId())
                                && "SELECTED".equalsIgnoreCase(trim(a.getStatus())))
                        .count();
                if (selectedCount >= cap) {
                    jobRepository.updateStatus(job.getJobId(), "CLOSED");
                }
            }
        }

        String suffix = updated ? "saved=1" : "error=1";
        resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&" + suffix);
    }

    private void handleCvDownload(HttpServletRequest req, HttpServletResponse resp, String filename)
            throws IOException {
        // Basic path traversal prevention
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Path file = cvsDir.resolve(filename);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        Files.copy(file, resp.getOutputStream());
    }

    private boolean updateApplicationAndSelection(String applicationId, String decision, String reviewerUserId) {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                Application target = applicationRepository.findById(applicationId);
                if (target == null) {
                    return false;
                }

                boolean statusUpdated = applicationRepository.updateStatus(applicationId, decision);
                if (!statusUpdated) {
                    return false;
                }

                List<Map<String, String>> selections = readList(selectionFile, MAP_LIST_TYPE);
                Map<String, String> log = new LinkedHashMap<>();
                log.put("selectionId", "S-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
                log.put("applicationId", target.getApplicationId());
                log.put("jobId", target.getJobId());
                log.put("reviewerUserId", reviewerUserId);
                log.put("decision", decision);
                log.put("decisionAt", Instant.now().toString());
                selections.add(log);
                Files.writeString(selectionFile, GSON.toJson(selections), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                return false;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    private <T> List<T> readList(Path file, Type listType) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        List<T> list = GSON.fromJson(json, listType);
        return list != null ? list : new ArrayList<>();
    }

    private void handleBatch(HttpServletRequest req, HttpServletResponse resp,
                              String selectedJobId, String reviewerUserId) throws IOException {
        String decision = trim(req.getParameter("decision")).toUpperCase();
        String idsParam = trim(req.getParameter("applicationIds"));

        if (!"UNDER_REVIEW".equals(decision) && !"REJECTED".equals(decision)) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=1");
            return;
        }

        if (idsParam.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=1");
            return;
        }

        String[] ids = idsParam.split(",");
        int successCount = 0;
        int failCount = 0;

        for (String id : ids) {
            String applicationId = id.trim();
            if (applicationId.isEmpty()) continue;

            // Skip terminal states
            Application existing = applicationRepository.findById(applicationId);
            if (existing != null) {
                String currentStatus = trim(existing.getStatus()).toUpperCase();
                if ("SELECTED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
                    // Skip already-finalized applications silently for batch
                    continue;
                }
            } else {
                failCount++;
                continue;
            }

            boolean updated = updateApplicationAndSelection(applicationId, decision, reviewerUserId);
            if (updated) {
                successCount++;
            } else {
                failCount++;
            }
        }

        String params = "saved=1";
        if (failCount > 0) {
            params += "&error=1";
        }
        resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&" + params);
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(selectionFile.getParent());
        if (!Files.exists(selectionFile)) {
            Files.writeString(selectionFile, "[]", StandardCharsets.UTF_8);
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
