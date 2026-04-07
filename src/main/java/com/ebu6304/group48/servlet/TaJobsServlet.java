/**
 * =============================================================================
 * TA Job Browsing and Filtering Servlet
 * =============================================================================
 * 
 * OWNER: zzzskl (231226772)
 * CREATED: 2026-04-07
 * VERSION: 1.0
 * STATUS: 🎯 CORE BUSINESS CODE - Production Ready
 * 
 * =============================================================================
 * 
 * 📋 FUNCTIONAL DESCRIPTION:
 * Provides job listing and filtering functionality for Teaching Assistants.
 * Displays open positions with match scoring, application status, and multi-criteria
 * filtering. Integrates with user profiles for personalized job recommendations.
 * 
 * =============================================================================
 * 
 * 🔄 INPUT/OUTPUT SPECIFICATION:
 * 
 * INPUTS (HTTP GET Requests):
 *   GET /ta/jobs:
 *     - Purpose: Display available TA positions
 *     - Query Parameters (optional filters):
 *       * type: Job type filter (MODULE, INVIGILATION)
 *       * semester: Semester filter (2026_SPRING, etc.)
 *       * skill: Skill keyword filter (partial match)
 *     - Session Requirements: userId must be set in session
 *     - Data Sources: jobs.json, applications.json, profiles.json
 * 
 * OUTPUTS:
 *   HTTP Response:
 *     - Forwards to: /WEB-INF/jsp/ta/jobs.jsp
 *     - Status: 200 OK with job listing, 302 redirect if not authenticated
 *   
 *   Request Attributes:
 *     - jobs (List<Job>): Filtered list of open positions
 *     - appliedJobIds (Set<String>): IDs of jobs user has already applied to
 *     - userProfile (Profile): Current user's profile for match calculation
 *     - typeFilter, semesterFilter, skillFilter: Current filter values
 *   
 *   Data Processing:
 *     - Filtering: Applies type, semester, and skill filters
 *     - Match Calculation: (user skills ∩ job skills) / job skills × 100%
 *     - Status Checking: Identifies already applied positions
 * 
 * =============================================================================
 * 
 * ⚠️ CONSTRAINTS AND DEPENDENCIES:
 * 
 * 1. DATA DEPENDENCIES:
 *    - JobRepository: Must provide findAllOpenJobs() method
 *    - ApplicationRepository: Must provide findByApplicantUserId() method
 *    - ProfileRepository: Must provide findByUserId() method
 *    - All repositories require actual JSON persistence implementation
 * 
 * 2. PERFORMANCE CONSTRAINTS:
 *    - No pagination in current implementation (all jobs loaded)
 *    - Match calculation O(n×m) where n=jobs, m=skills
 *    - Filtering done in-memory after loading all data
 * 
 * 3. BUSINESS RULES:
 *    - Only shows jobs with status="OPEN"
 *    - Match score based on exact skill name matching
 *    - Case-insensitive skill filtering
 *    - Users cannot apply to same job multiple times
 * 
 * 4. USER EXPERIENCE CONSTRAINTS:
 *    - Requires complete profile for match scoring
 *    - Requires CV upload before applying
 *    - Real-time filtering without page reload
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. PERFORMANCE IMPROVEMENTS (Priority: High):
 *    - Add pagination for large job lists
 *    - Implement server-side filtering (reduce data transfer)
 *    - Add caching for frequently accessed job data
 *    - Precompute match scores for faster display
 * 
 * 2. FEATURE ENHANCEMENTS (Priority: Medium):
 *    - Advanced matching algorithm (weighted skills, experience)
 *    - Saved searches and job alerts
 *    - Job comparison tool
 *    - Integration with calendar for schedule conflicts
 * 
 * 3. USER INTERFACE (Priority: Low):
 *    - Interactive filtering with AJAX
 *    - Visual job matching indicators
 *    - Export job listings to PDF/CSV
 *    - Mobile-responsive job cards
 * 
 * 4. DATA ANALYSIS (Priority: Medium):
 *    - Job application statistics
 *    - Skill gap analysis for users
 *    - Popular job trends reporting
 *    - Recommendation engine based on user behavior
 * 
 * =============================================================================
 * 
 * 🐛 DEBUGGING AND TROUBLESHOOTING:
 * 
 * Common Issues:
 * 1. Empty Job List: Check jobs.json data and JobRepository implementation
 * 2. Match Scores Incorrect: Verify skill names match between profile and jobs
 * 3. Filter Not Working: Check parameter names and case sensitivity
 * 4. Performance Slow: Monitor job data size and consider pagination
 * 
 * Logging Recommendations:
 *   - Log filter usage statistics
 *   - Log match calculation times for performance monitoring
 *   - Track job view counts for popularity analysis
 *   - Monitor repository call performance
 * 
 * =============================================================================
 * 
 * 📁 CODE ORGANIZATION:
 *   Project Root
 *   └── src/main/java/com/ebu6304/group48/
 *       └── servlet/
 *           └── TaJobsServlet.java    ← This file
 * 
 * DATA FLOW:
 *   Request → Authentication Check → Data Loading → Filtering → 
 *   Match Calculation → JSP Forwarding → HTML Response
 * 
 * =============================================================================
 */

package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Job;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ApplicationRepository;
import com.ebu6304.group48.repository.JobRepository;
import com.ebu6304.group48.repository.ProfileRepository;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "TaJobsServlet", urlPatterns = "/ta/jobs")
public class TaJobsServlet extends HttpServlet {

    private JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private ProfileRepository profileRepository;

    @Override
    public void init() throws ServletException {
        jobRepository = new JobRepository(getServletContext());
        applicationRepository = new ApplicationRepository(getServletContext());
        profileRepository = new ProfileRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = (String) session.getAttribute("userId");
        String typeFilter = req.getParameter("type");
        String semesterFilter = req.getParameter("semester");
        String skillFilter = req.getParameter("skill");

        List<Job> allJobs = jobRepository.findAllOpenJobs();

        List<Job> filteredJobs = allJobs.stream()
            .filter(job -> typeFilter == null || typeFilter.isEmpty() || job.getType().equals(typeFilter))
            .filter(job -> semesterFilter == null || semesterFilter.isEmpty() || job.getSemester().equals(semesterFilter))
            .filter(job -> skillFilter == null || skillFilter.isEmpty() || 
                          job.getRequiredSkills().stream().anyMatch(s -> 
                              s.toLowerCase().contains(skillFilter.toLowerCase())))
            .collect(Collectors.toList());

        Set<String> appliedJobIds = applicationRepository.findByApplicantUserId(userId)
            .stream()
            .map(app -> app.getJobId())
            .collect(Collectors.toSet());

        Profile userProfile = profileRepository.findByUserId(userId);

        req.setAttribute("jobs", filteredJobs);
        req.setAttribute("appliedJobIds", appliedJobIds);
        req.setAttribute("userProfile", userProfile);
        req.setAttribute("typeFilter", typeFilter);
        req.setAttribute("semesterFilter", semesterFilter);
        req.setAttribute("skillFilter", skillFilter);
        req.getRequestDispatcher("/WEB-INF/jsp/ta/jobs.jsp").forward(req, resp);
    }
}
