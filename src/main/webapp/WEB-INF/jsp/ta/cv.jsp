<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>CV Management</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 42rem; margin: 2rem auto; padding: 0 1rem; }
        .container { display: grid; gap: 1.5rem; }
        .card { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0.375rem; padding: 1.5rem; }
        .card h2 { margin-top: 0; color: #2c3e50; }
        .card h3 { margin-top: 1.5rem; color: #495057; font-size: 1.25rem; }
        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; margin-bottom: 0.375rem; font-weight: 500; color: #495057; }
        .form-control { width: 100%; padding: 0.5rem 0.75rem; font-size: 1rem; line-height: 1.5; border: 1px solid #ced4da; border-radius: 0.25rem; box-sizing: border-box; }
        .form-control:focus { border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }
        .file-input { padding: 0.5rem 0; }
        .btn { display: inline-block; font-weight: 400; text-align: center; vertical-align: middle; user-select: none; padding: 0.5rem 1rem; font-size: 1rem; line-height: 1.5; border-radius: 0.25rem; border: 1px solid transparent; cursor: pointer; margin-right: 0.5rem; margin-bottom: 0.5rem; }
        .btn-primary { color: #fff; background-color: #007bff; border-color: #007bff; }
        .btn-primary:hover { background-color: #0069d9; border-color: #0062cc; }
        .btn-danger { color: #fff; background-color: #dc3545; border-color: #dc3545; }
        .btn-danger:hover { background-color: #c82333; border-color: #bd2130; }
        .btn-secondary { color: #fff; background-color: #6c757d; border-color: #6c757d; }
        .btn-secondary:hover { background-color: #5a6268; border-color: #545b62; }
        .alert { padding: 0.75rem 1.25rem; margin-bottom: 1rem; border: 1px solid transparent; border-radius: 0.25rem; }
        .alert-success { color: #155724; background-color: #d4edda; border-color: #c3e6cb; }
        .alert-danger { color: #721c24; background-color: #f8d7da; border-color: #f5c6cb; }
        .alert-info { color: #0c5460; background-color: #d1ecf1; border-color: #bee5eb; }
        .nav { display: flex; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid #dee2e6; }
        .nav a { color: #007bff; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
        .file-list { margin-top: 1rem; }
        .file-item { display: flex; justify-content: space-between; align-items: center; padding: 0.75rem; border: 1px solid #e9ecef; border-radius: 0.25rem; margin-bottom: 0.5rem; background: white; }
        .file-name { font-family: monospace; font-size: 0.875rem; }
        .file-actions { display: flex; gap: 0.5rem; }
        .current-cv { background-color: #e8f5e8; border-color: #c3e6c3; }
        .help-text { font-size: 0.875rem; color: #6c757d; margin-top: 0.25rem; }
        .status-badge { display: inline-block; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; border-radius: 0.25rem; margin-left: 0.5rem; }
        .status-current { background-color: #d4edda; color: #155724; }
        .status-old { background-color: #e2e3e5; color: #383d41; }
        .requirements { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 0.25rem; padding: 1rem; margin-bottom: 1.5rem; }
        .requirements h4 { margin-top: 0; color: #856404; }
        .requirements ul { margin-bottom: 0; }
    </style>
</head>
<body>
<div class="container">
    <h1>CV Management</h1>
    
    <div class="nav">
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/ta/profile">Profile</a>
        <a href="${pageContext.request.contextPath}/ta/cv" style="font-weight: bold;">CV</a>
        <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a>
        <a href="${pageContext.request.contextPath}/logout" style="margin-left: auto;">Logout</a>
    </div>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-success">
            ${sessionScope.message}
            <c:remove var="message" scope="session"/>
        </div>
    </c:if>
    
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger">
            ${sessionScope.error}
            <c:remove var="error" scope="session"/>
        </div>
    </c:if>

    <div class="requirements">
        <h4>📄 File Requirements</h4>
        <ul>
            <li>Accepted formats: PDF, DOC, DOCX</li>
            <li>Maximum file size: 5MB</li>
            <li>File naming: CV_UserID_Timestamp.Extension (auto-generated)</li>
            <li>You can upload multiple CVs, but only one can be "active" for applications</li>
        </ul>
    </div>

    <div class="card">
        <h2>Upload New CV</h2>
        <form method="post" action="${pageContext.request.contextPath}/ta/cv" enctype="multipart/form-data">
            <input type="hidden" name="action" value="upload">
            
            <div class="form-group">
                <label for="cvFile">Select CV File *</label>
                <input type="file" id="cvFile" name="cvFile" class="form-control file-input" accept=".pdf,.doc,.docx" required>
                <div class="help-text">PDF, Word documents (.doc, .docx) only. Max 5MB.</div>
            </div>
            
            <button type="submit" class="btn btn-primary">Upload CV</button>
        </form>
    </div>

    <div class="card">
        <h2>Your CVs</h2>
        
        <c:choose>
            <c:when test="${not empty profile and not empty profile.cvFileName}">
                <div class="alert alert-info">
                    <strong>Current active CV:</strong> ${profile.cvFileName}
                    <br>
                    <small>This CV will be used for new job applications.</small>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-info">
                    <strong>No active CV selected.</strong> Please upload a CV to apply for jobs.
                </div>
            </c:otherwise>
        </c:choose>
        
        <c:if test="${not empty existingCvs}">
            <h3>All CV Files</h3>
            <div class="file-list">
                <c:forEach var="cvFile" items="${existingCvs}">
                    <div class="file-item ${not empty profile.cvFileName and profile.cvFileName eq cvFile ? 'current-cv' : ''}">
                        <div class="file-name">
                            ${cvFile}
                            <c:if test="${not empty profile.cvFileName and profile.cvFileName eq cvFile}">
                                <span class="status-badge status-current">Active</span>
                            </c:if>
                            <c:if test="${empty profile.cvFileName or profile.cvFileName ne cvFile}">
                                <span class="status-badge status-old">Archive</span>
                            </c:if>
                        </div>
                        <div class="file-actions">
                            <form method="post" action="${pageContext.request.contextPath}/ta/cv" style="display: inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="filename" value="${cvFile}">
                                <button type="submit" class="btn btn-danger" 
                                        onclick="return confirm('Are you sure you want to delete ${cvFile}?')">
                                    Delete
                                </button>
                            </form>
                            <c:if test="${empty profile.cvFileName or profile.cvFileName ne cvFile}">
                                <form method="post" action="${pageContext.request.contextPath}/ta/cv" style="display: inline;">
                                    <input type="hidden" name="action" value="setactive">
                                    <input type="hidden" name="filename" value="${cvFile}">
                                    <button type="submit" class="btn btn-secondary">
                                        Set as Active
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>
        
        <c:if test="${empty existingCvs}">
            <p>No CV files uploaded yet.</p>
        </c:if>
    </div>

    <div class="card">
        <h2>CV Tips</h2>
        <ul>
            <li>Keep your CV updated with recent experience and skills</li>
            <li>Highlight relevant teaching or tutoring experience</li>
            <li>Include specific programming languages and tools you're proficient in</li>
            <li>Make sure contact information matches your profile</li>
            <li>Consider having different CV versions for different course types (programming vs. theory)</li>
        </ul>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('cvFile');
    if (fileInput) {
        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const fileSize = file.size / 1024 / 1024; // MB
                const validTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
                
                if (!validTypes.includes(file.type)) {
                    alert('Invalid file type. Please select a PDF or Word document.');
                    this.value = '';
                } else if (fileSize > 5) {
                    alert('File size exceeds 5MB limit. Please select a smaller file.');
                    this.value = '';
                }
            }
        });
    }
});
</script>
</body>
</html>