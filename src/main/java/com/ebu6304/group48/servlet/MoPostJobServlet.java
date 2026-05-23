package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "MoPostJobServlet", urlPatterns = "/mo/jobs/new")
public class MoPostJobServlet extends HttpServlet {

    private JobRepository jobRepository;

    @Override
    public void init() {
        jobRepository = new JobRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = trim(req.getParameter("title"));
        String type = trim(req.getParameter("type"));
        String semester = trim(req.getParameter("semester"));
        String schedule = trim(req.getParameter("schedule"));
        String capacityRaw = trim(req.getParameter("capacity"));
        String requiredSkillsRaw = trim(req.getParameter("requiredSkills"));

        Integer capacity = parseCapacity(capacityRaw);
        if (title.isEmpty() || type.isEmpty() || semester.isEmpty() || schedule.isEmpty() || capacity == null) {
            forwardDashboardWithForm(req, resp,
                    "Please fill all required fields. Capacity must be a positive integer.",
                    title, type, semester, schedule, capacityRaw, requiredSkillsRaw);
            return;
        }

        List<String> requiredSkills = Arrays.stream(requiredSkillsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        Job job = new Job();
        job.setTitle(title);
        job.setType(type);
        job.setSemester(semester);
        job.setSchedule(schedule);
        job.setCapacity(capacity);
        job.setRequiredSkills(requiredSkills);
        job.setPostedByUserId(userId);
        job.setStatus("OPEN");
        job.setCreatedAt(Instant.now().toString());

        boolean ok = jobRepository.save(job);
        if (!ok) {
            forwardDashboardWithForm(req, resp, "Failed to save job. Please retry.",
                    title, type, semester, schedule, capacityRaw, requiredSkillsRaw);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard?saved=1");
    }

    private void forwardDashboardWithForm(HttpServletRequest req, HttpServletResponse resp,
                                        String error, String title, String type, String semester,
                                        String schedule, String capacity, String requiredSkills)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("openPostJobModal", Boolean.TRUE);
        req.setAttribute("title", title);
        req.setAttribute("type", type);
        req.setAttribute("semester", semester);
        req.setAttribute("schedule", schedule);
        req.setAttribute("capacity", capacity);
        req.setAttribute("requiredSkills", requiredSkills);
        req.setAttribute("navCurrent", "dashboard");
        req.getRequestDispatcher("/mo/dashboard").forward(req, resp);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static Integer parseCapacity(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
