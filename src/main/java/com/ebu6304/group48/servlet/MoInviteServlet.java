package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Invitation;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.model.User;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.InvitationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.repository.UserRepository;
import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.service.MatchingService.MatchResult;
import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MO browses all TAs (with profiles) for a selected job, sorted by match score,
 * and sends invitations. Already-applied, already-invited, and already-selected TAs
 * are marked and cannot be re-invited.
 */
@WebServlet(name = "MoInviteServlet", urlPatterns = "/mo/jobs/invite")
public class MoInviteServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private InvitationRepository invitationRepository;
    private ProfileRepository profileRepository;
    private UserRepository userRepository;
    private MatchingService matchingService;

    @Override
    public void init() {
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        invitationRepository = new InvitationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        userRepository = new UserRepository(dataDir);
        matchingService = new MatchingService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String selectedJobId = trim(req.getParameter("jobId"));

        // All jobs for the dropdown
        List<Job> allJobs = jobRepository.findAll().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt,
                        Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        if (selectedJobId.isEmpty() && !allJobs.isEmpty()) {
            selectedJobId = allJobs.get(0).getJobId();
        }

        Job selectedJob = jobRepository.findById(selectedJobId);

        // Find all TA users and their profiles
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (IOException e) {
            allUsers = new ArrayList<>();
        }
        List<User> taUsers = allUsers.stream()
                .filter(u -> "TA".equalsIgnoreCase(u.getRole())
                        && !Boolean.TRUE.equals(u.getBanned()))
                .collect(Collectors.toList());

        // Build profile lookup
        Map<String, Profile> profileByUserId = new HashMap<>();
        for (User ta : taUsers) {
            Profile p = profileRepository.findByUserId(ta.getUserId());
            if (p != null) {
                profileByUserId.put(ta.getUserId(), p);
            }
        }

        // Build match results for the selected job
        Map<String, MatchResult> matchResultMap = new HashMap<>();
        if (selectedJob != null) {
            for (User ta : taUsers) {
                Profile p = profileByUserId.get(ta.getUserId());
                if (p != null) {
                    matchResultMap.put(ta.getUserId(),
                            matchingService.computeMatch(selectedJob, p));
                }
            }
        }

        // Already applied TA userIds for this job
        Set<String> appliedUserIds = applicationRepository.findByJobId(selectedJobId)
                .stream()
                .map(a -> a.getApplicantUserId())
                .collect(Collectors.toSet());

        // Already invited (PENDING) TA userIds for this job
        Set<String> invitedUserIds = invitationRepository.findByJobId(selectedJobId)
                .stream()
                .filter(i -> "PENDING".equalsIgnoreCase(
                        i.getStatus() != null ? i.getStatus().trim() : ""))
                .map(Invitation::getTaUserId)
                .collect(Collectors.toSet());

        // Already selected TA userIds for this job
        Set<String> selectedUserIds = applicationRepository.findByJobId(selectedJobId)
                .stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(
                        a.getStatus() != null ? a.getStatus().trim() : ""))
                .map(a -> a.getApplicantUserId())
                .collect(Collectors.toSet());

        // Sort TAs by match score descending (those without profile at bottom)
        List<User> sortedTas = new ArrayList<>(taUsers);
        sortedTas.sort((a, b) -> {
            MatchResult mrA = matchResultMap.get(a.getUserId());
            MatchResult mrB = matchResultMap.get(b.getUserId());
            int scoreA = mrA != null ? mrA.getTotalScore() : -1;
            int scoreB = mrB != null ? mrB.getTotalScore() : -1;
            return Integer.compare(scoreB, scoreA);
        });

        // Invitations sent by this MO for the selected job (to show status)
        List<Invitation> sentInvitations = new ArrayList<>();
        if (!selectedJobId.isEmpty()) {
            sentInvitations = invitationRepository.findByJobId(selectedJobId);
        }

        req.setAttribute("jobs", allJobs);
        req.setAttribute("selectedJobId", selectedJobId);
        req.setAttribute("selectedJob", selectedJob);
        req.setAttribute("taUsers", sortedTas);
        req.setAttribute("profileByUserId", profileByUserId);
        req.setAttribute("matchResultMap", matchResultMap);
        req.setAttribute("appliedUserIds", appliedUserIds);
        req.setAttribute("invitedUserIds", invitedUserIds);
        req.setAttribute("selectedUserIds", selectedUserIds);
        req.setAttribute("sentInvitations", sentInvitations);
        req.setAttribute("navCurrent", "invite");
        req.getRequestDispatcher("/WEB-INF/jsp/mo/invite.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jobId = trim(req.getParameter("jobId"));
        String taUserId = trim(req.getParameter("taUserId"));
        String moUserId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        if (jobId.isEmpty() || taUserId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/invite?error=1");
            return;
        }

        // Validate job exists and is OPEN
        Job job = jobRepository.findById(jobId);
        if (job == null || !"OPEN".equalsIgnoreCase(job.getStatus() != null ? job.getStatus() : "")) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/invite?jobId=" + jobId + "&error=closed");
            return;
        }

        // Check duplicate PENDING
        if (invitationRepository.existsPending(jobId, taUserId)) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs/invite?jobId=" + jobId + "&error=duplicate");
            return;
        }

        Invitation inv = new Invitation();
        inv.setJobId(jobId);
        inv.setMoUserId(moUserId);
        inv.setTaUserId(taUserId);
        inv.setStatus("PENDING");

        boolean ok = invitationRepository.save(inv);
        String suffix = ok ? "saved=1" : "error=1";
        resp.sendRedirect(req.getContextPath() + "/mo/jobs/invite?jobId=" + jobId + "&" + suffix);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
