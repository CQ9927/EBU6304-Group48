<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>CV Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main container-grid">
    <h1>CV Management</h1>

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
        <h4>File requirements</h4>
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
                            <form method="post" action="${pageContext.request.contextPath}/ta/cv" class="inline-form">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="filename" value="${cvFile}">
                                <button type="submit" class="btn btn-danger" 
                                        onclick="return confirm('Are you sure you want to delete ${cvFile}?')">
                                    Delete
                                </button>
                            </form>
                            <c:if test="${empty profile.cvFileName or profile.cvFileName ne cvFile}">
                                <form method="post" action="${pageContext.request.contextPath}/ta/cv" class="inline-form">
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
</main>

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