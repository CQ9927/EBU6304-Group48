<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Available Jobs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<main class="site-main container-grid">
    <h1>Available TA Jobs</h1>
    
    <div class="nav">
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/ta/profile">Profile</a>
        <a href="${pageContext.request.contextPath}/ta/cv">CV</a>
        <a href="${pageContext.request.contextPath}/ta/jobs" class="nav-link--current">Jobs</a>
        <a href="${pageContext.request.contextPath}/ta/status">Status</a>
        <a href="${pageContext.request.contextPath}/logout" class="nav-spacer">Logout</a>
    </div>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-info">
            ${sessionScope.message}
            <c:remove var="message" scope="session"/>
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-warning">
            ${sessionScope.error}
            <c:remove var="error" scope="session"/>
        </div>
    </c:if>

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
            <strong>Profile incomplete.</strong> Please complete your <a href="${pageContext.request.contextPath}/ta/profile">profile</a> before applying for jobs.
            Your skills and availability will be used to match you with suitable positions.
        </div>
    </c:if>

    <c:if test="${empty userProfile.cvFileName}">
        <div class="profile-warning">
            <strong>CV missing.</strong> Please upload your <a href="${pageContext.request.contextPath}/ta/cv">CV</a> before applying for jobs.
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
                                    <small class="text-muted">${job.semester} • Capacity: ${job.capacity}</small>
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
                                            <span class="text-muted">N/A</span>
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
                                            <button type="button" class="btn btn-secondary btn-apply btn-disabled" disabled
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
        <p style="margin-top: 1rem; margin-bottom: 0;">
            <a href="${pageContext.request.contextPath}/ta/status" class="btn btn-secondary">
                View Application Status
            </a>
        </p>
    </div>
</main>

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