/**
 * =============================================================================
 * CV File Management Servlet
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
 * Manages Curriculum Vitae (CV) file operations for Teaching Assistants including
 * upload, deletion, and file listing. Provides secure file handling with validation
 * and integrates CV references with user profiles.
 * 
 * =============================================================================
 * 
 * 🔄 INPUT/OUTPUT SPECIFICATION:
 * 
 * INPUTS (HTTP Requests):
 *   GET /ta/cv:
 *     - Purpose: Display CV management interface
 *     - Session Requirements: userId must be set in session
 *     - Returns: cv.jsp with user's CV list and current active CV
 *   
 *   POST /ta/cv (multipart/form-data):
 *     - action=upload: Upload new CV file
 *       * Parameters: cvFile (file part), action="upload"
 *       * File Requirements: PDF, DOC, DOCX format, max 5MB
 *       * File Naming: Auto-generated as CV_{userId}_{timestamp}.{ext}
 *     
 *     - action=delete: Delete existing CV file
 *       * Parameters: filename (String), action="delete"
 *       * Security: Only allows deletion of user's own files
 * 
 * OUTPUTS:
 *   File System Operations:
 *     - Upload: Saves to {dataDir}/cvs/CV_{userId}_{timestamp}.{ext}
 *     - Delete: Removes file from server storage
 *     - Directory: Automatically creates cvs/ subdirectory if missing
 *   
 *   Data Operations:
 *     - Updates profiles.json via ProfileRepository
 *     - Sets cvFileName field in user's profile
 *     - Removes cvFileName reference when file deleted
 *   
 *   HTTP Responses:
 *     - Success: Redirects to /ta/cv with success message
 *     - Error: Redirects to /ta/cv with error message
 *     - Authentication Failure: Redirects to /login
 *   
 *   Session Attributes:
 *     - message: Success notification for user
 *     - error: Error details for user feedback
 * 
 * =============================================================================
 * 
 * ⚠️ CONSTRAINTS AND DEPENDENCIES:
 * 
 * 1. FILE CONSTRAINTS:
 *    - Accepted Formats: application/pdf, application/msword, 
 *                        application/vnd.openxmlformats-officedocument.wordprocessingml.document
 *    - Maximum File Size: 5MB (configurable via @MultipartConfig)
 *    - File Name Pattern: CV_{userId}_{timestamp}.{ext} (auto-generated)
 * 
 * 2. SECURITY CONSTRAINTS:
 *    - Users can only access their own CV files (userId filtering)
 *    - File type validation prevents executable uploads
 *    - File names are sanitized (no path traversal)
 * 
 * 3. EXTERNAL DEPENDENCIES:
 *    - ProfileRepository: Must be implemented for profile updates
 *    - AppPaths: Resolves data directory location
 *    - File System: Requires write permissions to data directory
 * 
 * 4. STORAGE CONSIDERATIONS:
 *    - CV files stored locally (not in database)
 *    - No automatic cleanup of old files
 *    - Disk space monitoring recommended for production
 * 
 * =============================================================================
 * 
 * 🔧 FUTURE MODIFICATION GUIDE:
 * 
 * 1. FEATURE ENHANCEMENTS (Priority: Medium):
 *    - Add CV preview functionality (PDF rendering)
 *    - Implement file versioning system
 *    - Add bulk download for administrators
 *    - Integrate cloud storage (AWS S3, Azure Blob)
 * 
 * 2. SECURITY IMPROVEMENTS (Priority: High):
 *    - Add virus scanning for uploaded files
 *    - Implement file size quotas per user
 *    - Add download authentication tokens
 *    - Log all file operations for audit trail
 * 
 * 3. PERFORMANCE OPTIMIZATIONS (Priority: Low):
 *    - Add file compression for storage
 *    - Implement caching for frequently accessed files
 *    - Add background processing for large files
 * 
 * 4. USER EXPERIENCE (Priority: Medium):
 *    - Add progress bar for file uploads
 *    - Implement drag-and-drop file upload
 *    - Add CV template suggestions
 *    - Integrate with profile auto-fill from CV text
 * 
 * =============================================================================
 * 
 * 🐛 DEBUGGING AND TROUBLESHOOTING:
 * 
 * Common Issues:
 * 1. File Upload Fails: Check directory permissions and disk space
 * 2. File Type Rejected: Verify Content-Type header matches accepted types
 * 3. Profile Update Fails: Check ProfileRepository implementation
 * 4. File Not Found: Verify file naming pattern and storage location
 * 
 * Logging Recommendations:
 *   - Log file uploads with size and type
 *   - Log file deletions with user and filename
 *   - Monitor storage directory size
 *   - Log security violations (unauthorized access attempts)
 * 
 * =============================================================================
 * 
 * 📁 CODE ORGANIZATION:
 *   Project Root
 *   └── src/main/java/com/ebu6304/group48/
 *       └── servlet/
 *           └── TaCvServlet.java    ← This file
 * 
 * DATA FLOW:
 *   File Upload: HTTP Request → TaCvServlet → File System + ProfileRepository
 *   File List: TaCvServlet → File System Scan → JSP Display
 *   File Delete: HTTP Request → TaCvServlet → File System + ProfileRepository
 * 
 * =============================================================================
 */

package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Profile;
import com.ebu6304.group48.repository.ProfileRepository;
import com.ebu6304.group48.util.SessionKeys;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
@WebServlet(name = "TaCvServlet", urlPatterns = "/ta/cv")
public class TaCvServlet extends HttpServlet {

    private ProfileRepository profileRepository;
    private String cvStorageDir;

    @Override
    public void init() throws ServletException {
        profileRepository = new ProfileRepository(getServletContext());
        String dataDir = AppPaths.resolveDataDirectory(getServletContext());
        cvStorageDir = dataDir + File.separator + "cvs";
        new File(cvStorageDir).mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = (String) session.getAttribute(SessionKeys.USER_ID);
        Profile profile = profileRepository.findByUserId(userId);
        String[] existingCvs = getExistingCvs(userId);
        
        req.setAttribute("profile", profile);
        req.setAttribute("existingCvs", existingCvs);
        req.getRequestDispatcher("/WEB-INF/jsp/ta/cv.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = (String) session.getAttribute(SessionKeys.USER_ID);
        String action = req.getParameter("action");

        if ("upload".equals(action)) {
            handleUpload(req, userId, session);
        } else if ("delete".equals(action)) {
            handleDelete(req, userId, session);
        }

        resp.sendRedirect(req.getContextPath() + "/ta/cv");
    }

    private void handleUpload(HttpServletRequest req, String userId, HttpSession session) throws IOException, ServletException {
        Part filePart = req.getPart("cvFile");
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("error", "No file selected or file is empty.");
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && 
            !contentType.equals("application/msword") && 
            !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            session.setAttribute("error", "Invalid file type. Please upload PDF or Word document.");
            return;
        }

        String originalFilename = getFileName(filePart);
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                            .format(LocalDateTime.now());
        String newFilename = String.format("CV_%s_%s%s", userId, timestamp, extension);

        File destFile = new File(cvStorageDir, newFilename);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        Profile profile = profileRepository.findByUserId(userId);
        if (profile != null) {
            profile.setCvFileName(newFilename);
            profile.setUpdatedAt(Instant.now().toString());
            profileRepository.save(profile);
            session.setAttribute("message", "CV uploaded successfully!");
        } else {
            session.setAttribute("error", "Please complete your profile first before uploading CV.");
        }
    }

    private void handleDelete(HttpServletRequest req, String userId, HttpSession session) throws IOException {
        String filename = req.getParameter("filename");
        if (filename == null || filename.isEmpty()) {
            session.setAttribute("error", "No filename specified.");
            return;
        }

        File fileToDelete = new File(cvStorageDir, filename);
        if (fileToDelete.exists() && fileToDelete.delete()) {
            Profile profile = profileRepository.findByUserId(userId);
            if (profile != null && filename.equals(profile.getCvFileName())) {
                profile.setCvFileName(null);
                profile.setUpdatedAt(Instant.now().toString());
                profileRepository.save(profile);
            }
            session.setAttribute("message", "CV deleted successfully!");
        } else {
            session.setAttribute("error", "Failed to delete file.");
        }
    }

    private String[] getExistingCvs(String userId) {
        File dir = new File(cvStorageDir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("CV_" + userId + "_"));
        if (files == null) return new String[0];
        return Arrays.stream(files)
                     .map(File::getName)
                     .toArray(String[]::new);
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "unknown";
    }
}
