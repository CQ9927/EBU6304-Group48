package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Application;
import com.ebu6304.group48.model.Invitation;
import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.InvitationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.service.MatchingService;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TA views invitations and accepts (auto-creates Application) or declines.
 */
@WebServlet(name = "TaInviteServlet", urlPatterns = "/ta/invitations")
public class TaInviteServlet extends HttpServlet {

    private InvitationRepository invitationRepository;
    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private ProfileRepository profileRepository;
    private MatchingService matchingService;

    @Override
    public void init() {
        invitationRepository = new InvitationRepository(getServletContext());
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
        matchingService = new MatchingService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = String.valueOf(session.getAttribute(SessionKeys.USER_ID));
        List<Invitation> allInvitations = invitationRepository.findByTaUserId(userId);

        // Split: pending first, then processed (accepted/declined/expired)
        List<Invitation> pendingList = allInvitations.stream()
                .filter(i -> "PENDING".equalsIgnoreCase(
                        i.getStatus() != null ? i.getStatus().trim() : ""))
                .sorted(Comparator.comparing(Invitation::getCreatedAt,
                        Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        List<Invitation> processedList = allInvitations.stream()
                .filter(i -> !"PENDING".equalsIgnoreCase(
                        i.getStatus() != null ? i.getStatus().trim() : ""))
                .sorted(Comparator.comparing(Invitation::getUpdatedAt,
                        Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        // Build job and profile lookups
        Map<String, Job> jobMap = new HashMap<>();
        for (Job j : jobRepository.findAll()) {
            jobMap.put(j.getJobId(), j);
        }

        Profile profile = profileRepository.findByUserId(userId);

        // Match results for pending invitations
        Map<String, MatchingService.MatchResult> matchMap = new HashMap<>();
        for (Invitation inv : pendingList) {
            Job job = jobMap.get(inv.getJobId());
            if (job != null && profile != null) {
                matchMap.put(inv.getInvitationId(),
                        matchingService.computeMatch(job, profile));
            }
        }

        int pendingCount = pendingList.size();

        req.setAttribute("pendingInvitations", pendingList);
        req.setAttribute("processedInvitations", processedList);
        req.setAttribute("pendingCount", pendingCount);
        req.setAttribute("jobMap", jobMap);
        req.setAttribute("profile", profile);
        req.setAttribute("matchMap", matchMap);
        req.setAttribute("navCurrent", "invitations");
        req.getRequestDispatcher("/WEB-INF/jsp/ta/invitations.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = trim(req.getParameter("action"));
        String invitationId = trim(req.getParameter("invitationId"));
        String userId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        if (invitationId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=1");
            return;
        }

        Invitation inv = invitationRepository.findById(invitationId);
        if (inv == null || !userId.equals(inv.getTaUserId())
                || !"PENDING".equalsIgnoreCase(inv.getStatus() != null ? inv.getStatus().trim() : "")) {
            resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=1");
            return;
        }

        if ("accept".equals(action)) {
            // Check job still open
            Job job = jobRepository.findById(inv.getJobId());
            if (job == null || !"OPEN".equalsIgnoreCase(
                    job.getStatus() != null ? job.getStatus() : "")) {
                resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=closed");
                return;
            }

            // Check not already applied
            boolean alreadyApplied = applicationRepository.findByApplicantUserId(userId)
                    .stream()
                    .anyMatch(a -> inv.getJobId().equals(a.getJobId()));
            if (alreadyApplied) {
                invitationRepository.updateStatus(invitationId, "DECLINED");
                resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=applied");
                return;
            }

            // Check deadline
            if (job.getDeadline() != null && !job.getDeadline().isBlank()
                    && job.getDeadline().compareTo(Instant.now().toString()) < 0) {
                invitationRepository.updateStatus(invitationId, "EXPIRED");
                resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=deadline");
                return;
            }

            // Check profile + CV
            Profile profile = profileRepository.findByUserId(userId);
            if (profile == null || profile.getCvFileName() == null
                    || profile.getCvFileName().isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=noprofile");
                return;
            }

            // Auto-create application
            MatchingService.MatchResult result = matchingService.computeMatch(job, profile);
            String now = Instant.now().toString();
            Application app = new Application();
            app.setJobId(inv.getJobId());
            app.setApplicantUserId(userId);
            app.setMatchScore(result.getTotalScore());
            app.setMissingSkills(result.getMissingSkills());
            app.setStatus("SUBMITTED");
            app.setNote("Applied via MO invitation");
            app.setCreatedAt(now);
            app.setUpdatedAt(now);
            applicationRepository.save(app);

            invitationRepository.updateStatus(invitationId, "ACCEPTED");
            resp.sendRedirect(req.getContextPath() + "/ta/invitations?accepted=1");

        } else if ("decline".equals(action)) {
            invitationRepository.updateStatus(invitationId, "DECLINED");
            resp.sendRedirect(req.getContextPath() + "/ta/invitations?declined=1");
        } else {
            resp.sendRedirect(req.getContextPath() + "/ta/invitations?error=1");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
