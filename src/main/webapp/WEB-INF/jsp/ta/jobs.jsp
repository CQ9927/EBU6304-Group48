<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Available Jobs</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 52rem; margin: 2rem auto; padding: 0 1rem; }
        .container { display: grid; gap: 1.5rem; }
        .card { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0.375rem; padding: 1.5rem; }
        .card h2 { margin-top: 0; color: #2c3e50; }
        .filter-form { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; padding: 1rem; background: white; border: 1px solid #dee2e6; border-radius: 0.25rem; }
        .filter-group { display: flex; flex-direction: column; }
        .filter-group label { margin-bottom: 0.375rem; font-weight: 500; color: #495057; font-size: 0.875rem; }
        .form-control { width: 100%; padding: 0.5rem 0.75rem; font-size: 0.875rem; line-height: 1.5; border: 1px solid #ced4da; border-radius: 0.25rem; box-sizing: border-box; }
        .form-control:focus { border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }
        .btn { display: inline-block; font-weight: 400; text-align: center; vertical-align: middle; user-select: none; padding: 0.5rem 1rem; font-size: 0.875rem; line-height: 1.5; border-radius: 0.25rem; border: 1px solid transparent; cursor: pointer; }
        .btn-primary { color: #fff; background-color: #007bff; border-color: #007bff; }
        .btn-primary:hover { background-color: #0069d9; border-color: #0062cc; }
        .btn-success { color: #fff; background-color: #28a745; border-color: #28a745; }
        .btn-success:hover { background-color: #218838; border-color: #1e7e34; }
        .btn-secondary { color: #fff; background-color: #6c757d; border-color: #6c757d; }
        .btn-secondary:hover { background-color: #5a6268; border-color: #545b62; }
        .btn-disabled { color: #fff; background-color: #6c757d; border-color: #6c757d; opacity: 0.65; cursor: not-allowed; }
        .btn-apply { min-width: 80px; }
        .nav { display: flex; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid #dee2e6; }
        .nav a { color: #007bff; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
        .alert { padding: 0.75rem 1.25rem; margin-bottom: 1rem; border: 1px solid transparent; border-radius: 0.25rem; }
        .alert-info { color: #0c5460; background-color: #d1ecf1; border-color: #bee5eb; }
        .alert-warning { color: #856404; background-color: #fff3cd; border-color: #ffeaa7; }
        .job-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        .job-table th, .job-table td { padding: 0.75rem; text-align: left; border-bottom: 1px solid #dee2e6; }
        .job-table th { background-color: #f8f9fa; font-weight: 600; color: #495057; }
        .job-table tr:hover { background-color: #f8f9fa; }
        .badge { display: inline-block; padding: 0.25rem 0.5rem; font-size: 0.75rem; font-weight: 600; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: 0.25rem; }
        .badge-module { background-color: #d4edda; color: #155724; }
        .badge-invigilation { background-color: #cce5ff; color: #004085; }
        .badge-open { background-color: #d4edda; color: #155724; }
        .badge-closed { background-color: #f8d7da; color: #721c24; }
        .skills-list { display: flex; flex-wrap: wrap; gap: 0.25rem; }
        .skill-badge { background-color: #e9ecef; color: #495057; padding: 0.15rem 0.4rem; border-radius: 0.25rem; font-size: 0.75rem; }
        .skill-match { background-color: #d4edda; color: #155724; }
        .skill-missing { background-color: #f8d7da; color: #721c24; }
        .match-score { font-weight: 600; }
        .match-high { color: #28a745; }
        .match-medium { color: #fd7e14; }
        .match-low { color: #dc3545; }
        .no-jobs { text-align: center; padding: 2rem; color: #6c757d; }
        .pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #dee2e6; }
        .pagination-info { font-size: 0.875rem; color: #6c757d; }
        .stats { display: flex; gap: 1.5rem; margin-bottom: 1rem; }
        .stat-item { background: white; border: 1px solid #dee2e6; border-radius: 0.25rem; padding: 0.75rem; min-width: 120px; text-align: center; }
        .stat-value { font-size: 1.5rem; font-weight: 600; color: #007bff; }
        .stat-label { font-size: 0.875rem; color: #6c757d; }
        .profile-warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 0.25rem; padding: 1rem; margin-bottom: 1rem; }
    </style>
</head>
<body>
<div class="container">
    <h1>Available TA Jobs</h1>
    
    <div class="nav">
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/ta/profile">Profile</a>
        <a href="${pageContext.request.contextPath}/ta/cv">CV</a>
        <a href="${pageContext.request.contextPath}/ta/jobs" style="font-weight: bold;">Jobs</a>
        <a href="${pageContext.request.contextPath}/logout" style="margin-left: auto;">Logout</a>
    </div>

    <c:if test="${not empty userProfile}">
        <div class="stats">
            <div class="stat-item">
                <div class="stat-value">${not empty userProfile.skills ? userProfile.skills.size() : 0}</div>
                <div class="stat-label">Skills Listed</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${not empty userProfile.availability ? userProfile.availability.size() : 0}</div>
                <div class="stat-label">Time Slots</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${appliedJobIds.size()}</div>
                <div class="stat-label">Applications</div>
            </div>
        </div>
    </c:if>

    <c:if test="${empty userProfile}">
        <div class="profile-warning">
            <strong>⚠️ Profile Incomplete!</strong> Please complete your <a href="${pageContext.request.contextPath}/ta/profile">profile</a> before applying for jobs.
            Your skills and availability will be used to match you with suitable positions.
        </div>
    </c:if>

    <c:if test="${empty userProfile.cvFileName}">
        <div class="profile-warning">
            <strong>⚠️ CV Missing!</strong> Please upload your <a href="${pageContext.request.contextPath}/ta/cv">CV</a> before applying for jobs.
            Most applications require a current CV.
        </div>
    </c:if>

    <div class="card">
        <h2>Job Search Filters</h2>
        <form method="get" action="${pageContext.request.contextPath}/ta/jobs" class="filter-form">
            <div class="filter-group">
                <label for="type">Job Type</label>
                <select id="type" name="type" class="form-control">
                    <option value="">All Types</option>
                    <option value="MODULE" ${typeFilter eq 'MODULE' ? 'selected' : ''}>Module Assistant</option>
                    <option value="INVIGILATION" ${typeFilter eq 'INVIGILATION' ? 'selected' : ''}>Invigilation</option>
                </select>
            </div>
            
            <div class="filter-group">
                <label for="semester">Semester</label>
                <select id="semester" name="semester" class="form-control">
                    <option value="">All Semesters</option>
                    <option value="2026_SPRING" ${semesterFilter eq '2026_SPRING' ? 'selected' : ''}>2026 Spring</option>
                    <option value="2025_FALL" ${semesterFilter eq '2025_FALL' ? 'selected' : ''}>2025 Fall</option>
                    <option value="2025_SPRING" ${semesterFilter eq '2025_SPRING' ? 'selected' : ''}>2025 Spring</option>
                </select>
            </div>
            
            <div class="filter-group">
                <label for="skill">Required Skill</label>
                <input type="text" id="skill" name="skill" class="form-control" 
                       placeholder="e.g., Java, Teaching" value="${skillFilter}">
            </div>
            
            <div class="filter-group" style="grid-column: 1 / -1; display: flex; gap: 0.5rem; align-items: flex-end;">
                <button type="submit" class="btn btn-primary">Apply Filters</button>
                <a href="${pageContext.request.contextPath}/ta/jobs" class="btn btn-secondary">Clear Filters</a>
            </div>
        </form>
        
        <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
            <div class="alert alert-info">
                <strong>Active filters:</strong>
                <c:if test="${not empty typeFilter}">Type: ${typeFilter}</c:if>
                <c:if test="${not empty semesterFilter}"> | Semester: ${semesterFilter}</c:if>
                <c:if test="${not empty skillFilter}"> | Skill: ${skillFilter}</c:if>
            </div>
        </c:if>
    </div>

    <div class="card">
        <h2>Available Positions (${jobs.size()})</h2>
        
        <c:choose>
            <c:when test="${not empty jobs}">
                <table class="job-table">
                    <thead>
                        <tr>
                            <th>Job Title</th>
                            <th>Type</th>
                            <th>Schedule</th>
                            <th>Skills Required</th>
                            <th>Match</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="job" items="${jobs}">
                            <c:set var="isApplied" value="${appliedJobIds.contains(job.jobId)}" />
                            <c:set var="userSkills" value="${not empty userProfile ? userProfile.skills : []}" />
                            <c:set var="matchingSkills" value="" />
                            <c:set var="missingSkills" value="" />
                            <c:set var="matchScore" value="0" />
                            
                            <c:if test="${not empty userSkills and not empty job.requiredSkills}">
                                <c:set var="matchingCount" value="0" />
                                <c:forEach var="requiredSkill" items="${job.requiredSkills}">
                                    <c:if test="${userSkills.contains(requiredSkill)}">
                                        <c:set var="matchingCount" value="${matchingCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                <c:set var="matchScore" value="${matchingCount * 100 / job.requiredSkills.size()}" />
                            </c:if>
                            
                            <tr>
                                <td>
                                    <strong>${job.title}</strong><br>
                                    <small style="color: #6c757d;">${job.semester} • Capacity: ${job.capacity}</small>
                                </td>
                                <td>
                                    <span class="badge badge-${job.type.toLowerCase()}">
                                        ${job.type}
                                    </span>
                                </td>
                                <td>${job.schedule}</td>
                                <td>
                                    <div class="skills-list">
                                        <c:forEach var="skill" items="${job.requiredSkills}">
                                            <c:choose>
                                                <c:when test="${userSkills.contains(skill)}">
                                                    <span class="skill-badge skill-match" title="You have this skill">${skill} ✓</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="skill-badge skill-missing" title="Missing skill">${skill} ✗</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty userProfile}">
                                            <span class="match-score 
                                                <c:choose>
                                                    <c:when test="${matchScore >= 80}">match-high</c:when>
                                                    <c:when test="${matchScore >= 50}">match-medium</c:when>
                                                    <c:otherwise>match-low</c:otherwise>
                                                </c:choose>">
                                                ${matchScore.intValue()}%
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #6c757d;">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${isApplied}">
                                            <span class="badge badge-secondary">Applied</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-open">OPEN</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${isApplied}">
                                            <button class="btn btn-secondary btn-apply" disabled>Applied</button>
                                        </c:when>
                                        <c:when test="${empty userProfile or empty userProfile.cvFileName}">
                                            <button class="btn btn-disabled btn-apply" disabled 
                                                    title="Complete profile and upload CV first">Apply</button>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/ta/apply?jobId=${job.jobId}" 
                                               class="btn btn-success btn-apply">Apply</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <div class="pagination">
                    <div class="pagination-info">
                        Showing ${jobs.size()} job<c:if test="${jobs.size() != 1}">s</c:if>
                        <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
                            (filtered)
                        </c:if>
                    </div>
                    <div>
                        <c:if test="${jobs.size() > 0}">
                            <small>Last updated: ${jobs[0].createdAt}</small>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-jobs">
                    <h3>No jobs found</h3>
                    <p>There are currently no open positions matching your criteria.</p>
                    <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
                        <p>Try clearing your filters or check back later.</p>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <h2>Application Tips</h2>
        <ul>
            <li><strong>Match your skills:</strong> Highlight skills that match the job requirements in your profile</li>
            <li><strong>Check availability:</strong> Ensure your availability matches the job schedule</li>
            <li><strong>Update CV:</strong> Make sure your CV is current and highlights relevant experience</li>
            <li><strong>Apply early:</strong> Popular positions fill up quickly</li>
            <li><strong>Track applications:</strong> Check your application status regularly</li>
        </ul>
        <p style="margin-top: 1rem;">
            <a href="${pageContext.request.contextPath}/ta/status" class="btn btn-secondary">
                View Application Status
            </a>
        </p>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Auto-refresh job list every 30 seconds
    setInterval(function() {
        if (document.visibilityState === 'visible') {
            window.location.reload();
        }
    }, 30000);
    
    // Add tooltips for match scores
    const matchElements = document.querySelectorAll('.match-score');
    matchElements.forEach(el => {
        const score = parseInt(el.textContent);
        let tooltip = '';
        if (score >= 80) {
            tooltip = 'Excellent match! Strongly recommended to apply.';
        } else if (score >= 50) {
            tooltip = 'Good match. Consider applying if interested.';
        } else if (score > 0) {
            tooltip = 'Partial match. You might want to improve your skills first.';
        } else {
            tooltip = 'No skills match. Consider other positions or update your skills.';
        }
        el.title = tooltip;
    });
});
</script>
</body>
</html>