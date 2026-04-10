package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.util.SessionKeys;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
    private Path selectionFile;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        selectionFile = Path.of(dataDir, "selection.json");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Job> jobs = jobRepository.findAll().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        String selectedJobId = trim(req.getParameter("jobId"));
        if (selectedJobId.isEmpty() && !jobs.isEmpty()) {
            selectedJobId = jobs.get(0).getJobId();
        }

        final String selectedJobIdFinal = selectedJobId;
        List<Application> applications = applicationRepository.findAll();
        List<Application> filteredApplications = applications.stream()
                .filter(a -> selectedJobIdFinal.isEmpty() || selectedJobIdFinal.equals(a.getJobId()))
                .collect(Collectors.toList());

        req.setAttribute("jobs", jobs);
        req.setAttribute("selectedJobId", selectedJobId);
        req.setAttribute("applications", filteredApplications);
        req.getRequestDispatcher("/WEB-INF/jsp/mo/select.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String applicationId = trim(req.getParameter("applicationId"));
        String decision = trim(req.getParameter("decision")).toUpperCase();
        String selectedJobId = trim(req.getParameter("jobId"));
        String reviewerUserId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        if (applicationId.isEmpty() ||
                (!"UNDER_REVIEW".equals(decision) && !"SELECTED".equals(decision) && !"REJECTED".equals(decision))) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&error=1");
            return;
        }

        boolean updated = updateApplicationAndSelection(applicationId, decision, reviewerUserId);
        String suffix = updated ? "saved=1" : "error=1";
        resp.sendRedirect(req.getContextPath() + "/mo/jobs/select?jobId=" + selectedJobId + "&" + suffix);
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
