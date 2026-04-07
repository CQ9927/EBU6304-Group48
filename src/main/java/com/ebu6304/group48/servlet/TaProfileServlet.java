/**
 * =============================================================================
 * TA Profile Management Servlet
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
 * Handles creation and updating of Teaching Assistant (TA) personal profiles.
 * Provides RESTful endpoints for profile management with proper authentication
 * and data validation.
 * 
 * =============================================================================
 * 
 * 🔄 INPUT/OUTPUT SPECIFICATION:
 * 
 * INPUTS (HTTP Requests):
 *   GET /ta/profile:
 *     - Purpose: Display profile edit form
 *     - Session Requirements: userId must be set in session
 *     - Returns: profile.jsp with existing profile data (if any)
 *   
 *   POST /ta/profile:
 *     - Purpose: Save or update TA profile
 *     - Parameters:
 *       * name (String, required): TA's full name
 *       * email (String, required): TA's email address
 *       * major (String, required): TA's academic major
 *       * skills[] (String array): List of skills (checkboxes)
 *       * availability[] (String array): Available time slots (checkboxes)
 *       * notes (String): Additional notes (text area)
 *     - Session Requirements: userId must be set in session
 *     - Data Persistence: Saves to profiles.json via ProfileRepository
 * 
 * OUTPUTS:
 *   HTTP Responses:
 *     - Success: Redirects to /ta/profile with success message in session
 *     - Error: Redirects to /ta/profile with error message in session
 *     - Authentication Failure: Redirects to /login
 *   
 *   Data Operations:
 *     - Reads/Writes: data/profiles.json
 *     - Session Attributes: profile (Profile object), message, error
 *   
 *   Page Forwarding:
 *     - Always forwards to: /WEB-INF/jsp/ta/profile.jsp
 * 
 * =============================================================================
 * 
 * ⚠️ CONSTRAINTS AND DEPENDENCIES:
 * 
 * 1. AUTHENTICATION REQUIREMENTS:
 *    - User must be logged in (session contains userId)
 *    - User role should be "TA" (checked by AuthFilter)
 * 
 * 2. DATA VALIDATION:
 *    - Required fields: name, email, major
 *    - Email format validation (client-side)
 *    - Array parameters handled as empty arrays if null
 * 
 * 3. EXTERNAL DEPENDENCIES:
 *    - ProfileRepository: Must be implemented for actual JSON persistence
 *    - AuthFilter: Should handle authentication checks
 *    - Session Management: LoginServlet must set userId in session
 * 
 * 4. PERFORMANCE CONSIDERATIONS:
 *    - No pagination needed (single user profile)
 *    - JSON file size should be monitored for large user bases
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. ENHANCED VALIDATION (Priority: Medium):
 *    - Add server-side email format validation
 *    - Validate skill count limits (min/max)
 *    - Add availability conflict checking
 * 
 * 2. FEATURE EXTENSIONS (Priority: Low):
 *    - Profile import/export functionality
 *    - Profile version history/audit trail
 *    - API endpoints for mobile applications
 *    - Profile sharing between similar TAs
 * 
 * 3. PERFORMANCE OPTIMIZATIONS (Priority: Low):
 *    - Add caching for frequently accessed profiles
 *    - Implement batch operations for admin use
 *    - Add compression for large profile data
 * 
 * 4. SECURITY ENHANCEMENTS (Priority: High):
 *    - Add CSRF protection tokens
 *    - Implement rate limiting for profile updates
 *    - Add input sanitization for notes field
 *    - Log profile modification attempts
 * 
 * =============================================================================
 * 
 * 🐛 DEBUGGING AND TROUBLESHOOTING:
 * 
 * Common Issues:
 * 1. NullPointerException: Check if session or userId is null
 * 2. Profile not saving: Verify ProfileRepository implementation
 * 3. Form data missing: Check parameter names match JSP form
 * 4. Redirect loops: Ensure AuthFilter is properly configured
 * 
 * Logging Recommendations:
 *   - Log profile creation/updates with timestamp
 *   - Log validation failures for debugging
 *   - Monitor file write successes/failures
 * 
 * =============================================================================
 * 
 * 📁 CODE ORGANIZATION:
 *   Project Root
 *   └── src/main/java/com/ebu6304/group48/
 *       └── servlet/
 *           └── TaProfileServlet.java    ← This file
 * 
 * DATA FLOW:
 *   HTTP Request → TaProfileServlet → ProfileRepository → JSON File
 *   JSON File → ProfileRepository → TaProfileServlet → JSP Response
 * 
 * =============================================================================
 */

package com.ebu6304.group48.servlet;

import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ProfileRepository;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@WebServlet(name = "TaProfileServlet", urlPatterns = "/ta/profile")
public class TaProfileServlet extends HttpServlet {

    private ProfileRepository profileRepository;

    @Override
    public void init() throws ServletException {
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
        Profile profile = profileRepository.findByUserId(userId);
        req.setAttribute("profile", profile);
        req.getRequestDispatcher("/WEB-INF/jsp/ta/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = (String) session.getAttribute("userId");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String major = req.getParameter("major");
        String[] skills = req.getParameterValues("skills");
        String[] availability = req.getParameterValues("availability");
        String notes = req.getParameter("notes");

        Profile existingProfile = profileRepository.findByUserId(userId);
        Profile profile;
        
        if (existingProfile != null) {
            profile = existingProfile;
            profile.setName(name);
            profile.setEmail(email);
            profile.setMajor(major);
            profile.setSkills(Arrays.asList(skills != null ? skills : new String[0]));
            profile.setAvailability(Arrays.asList(availability != null ? availability : new String[0]));
            profile.setNotes(notes);
            profile.setUpdatedAt(Instant.now().toString());
        } else {
            profile = new Profile();
            profile.setProfileId("P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            profile.setUserId(userId);
            profile.setName(name);
            profile.setEmail(email);
            profile.setMajor(major);
            profile.setSkills(Arrays.asList(skills != null ? skills : new String[0]));
            profile.setAvailability(Arrays.asList(availability != null ? availability : new String[0]));
            profile.setNotes(notes);
            profile.setUpdatedAt(Instant.now().toString());
            profile.setCreatedAt(Instant.now().toString());
        }

        boolean success = profileRepository.save(profile);
        if (success) {
            session.setAttribute("message", "Profile saved successfully!");
        } else {
            session.setAttribute("error", "Failed to save profile. Please try again.");
        }

        resp.sendRedirect(req.getContextPath() + "/ta/profile");
    }
}
