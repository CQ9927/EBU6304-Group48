<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Select Applicants</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
<header class="page-header">
    <h1 class="page-title">MO selection</h1>
    <p class="lead lead--tight text-muted">Filter applications and update status.</p>
</header>

<c:if test="${param.saved == '1'}">
    <div class="alert alert-success" role="status">Decision saved.</div>
</c:if>
<c:if test="${param.error == '1'}">
    <div class="alert alert-error" role="alert">Failed to save decision. Please retry.</div>
</c:if>
<c:if test="${param.error == 'capacity'}">
    <div class="alert alert-warning" role="alert">
        Cannot select this applicant: the job has reached its full capacity.
    </div>
</c:if>
<c:if test="${param.error == 'final'}">
    <div class="alert alert-warning" role="alert">
        Cannot change decision: this application is already finalised (SELECTED or REJECTED).
    </div>
</c:if>

<div class="card">
<form method="get" action="${pageContext.request.contextPath}/mo/jobs/select" class="filter-bar">
    <div class="form-group">
        <label for="jobId">Choose job</label>
        <select id="jobId" name="jobId">
            <option value="">— all jobs —</option>
            <c:forEach var="job" items="${jobs}">
                <option value="${job.jobId}" ${selectedJobId == job.jobId ? 'selected' : ''}>
                        ${job.jobId} - ${job.title}
                </option>
            </c:forEach>
        </select>
    </div>
    <button type="submit" class="btn btn-primary">Filter</button>
</form>
</div>

<%-- Capacity info bar --%>
<c:if test="${not empty selectedJobId}">
    <c:set var="currentJob" value="${jobMap[selectedJobId]}"/>
    <c:if test="${not empty currentJob}">
        <c:set var="cap" value="${currentJob.capacity != null ? currentJob.capacity : 0}"/>
        <c:set var="selCount" value="${selectedCountByJob[selectedJobId] != null ? selectedCountByJob[selectedJobId] : 0}"/>
        <div class="stats" style="margin-top:1rem;">
            <div class="stat-item">
                <div class="stat-value">${cap}</div>
                <div class="stat-label">Capacity</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${selCount}</div>
                <div class="stat-label">Selected</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${cap - selCount}</div>
                <div class="stat-label">Remaining</div>
            </div>
        </div>
    </c:if>
</c:if>

<div class="card card--flush">
<div class="table-scroll">
<table class="data-table">
    <thead>
    <tr>
        <th>Application ID</th>
        <th>Applicant</th>
        <th>CV</th>
        <th>Match Breakdown</th>
        <th>Missing Skills</th>
        <th>Status</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="app" items="${applications}">
        <c:set var="profile" value="${applicantProfileMap[app.applicantUserId]}"/>
        <c:set var="mr" value="${matchResultMap[app.applicationId]}"/>
        <c:set var="isFinal" value="${app.status == 'SELECTED' || app.status == 'REJECTED'}"/>

        <%-- Capacity-aware SELECT button --%>
        <c:set var="capacityFull" value="false"/>
        <c:if test="${not empty selectedJobId}">
            <c:set var="cap" value="${currentJob.capacity != null ? currentJob.capacity : 0}"/>
            <c:set var="selCount" value="${selectedCountByJob[selectedJobId] != null ? selectedCountByJob[selectedJobId] : 0}"/>
            <c:if test="${cap > 0 && selCount >= cap}">
                <c:set var="capacityFull" value="true"/>
            </c:if>
        </c:if>

        <tr>
            <td>${app.applicationId}</td>
            <td>
                <c:choose>
                    <c:when test="${not empty profile}">
                        <strong>${profile.name}</strong><br/>
                        <small class="text-muted">${profile.major}</small><br/>
                        <small class="text-muted">${profile.email}</small>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">${app.applicantUserId}</span>
                        <br/><small class="text-muted">(Profile not found)</small>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:if test="${not empty profile and not empty profile.cvFileName}">
                    <a href="${pageContext.request.contextPath}/mo/jobs/select?download=${profile.cvFileName}&jobId=${selectedJobId}"
                       class="btn btn-secondary" style="font-size:0.75rem;padding:0.2rem 0.5rem;">
                        Download CV
                    </a>
                </c:if>
                <c:if test="${empty profile or empty profile.cvFileName}">
                    <span class="text-muted">—</span>
                </c:if>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty mr}">
                        <div style="display:flex;flex-direction:column;gap:1px;font-size:0.8rem;line-height:1.4;">
                            <span class="match-score
                                <c:choose>
                                    <c:when test="${mr.totalScore >= 70}">match-high</c:when>
                                    <c:when test="${mr.totalScore >= 40}">match-medium</c:when>
                                    <c:otherwise>match-low</c:otherwise>
                                </c:choose>">
                                <strong>Total: ${mr.totalScore}/100</strong>
                            </span>
                            <span class="text-muted">Skills: ${mr.skillScore}/50</span>
                            <span class="text-muted">Schedule: ${mr.scheduleScore}/25 ${mr.scheduleMatch ? '(matched)' : ''}</span>
                            <span class="text-muted">Major: ${mr.majorScore}/15</span>
                            <span class="text-muted">Profile: ${mr.completenessScore}/10</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">N/A</span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty mr}">
                        <div class="skills-list">
                            <c:forEach var="skill" items="${mr.matchedSkills}">
                                <span class="skill-badge skill-match">${skill} ✓</span>
                            </c:forEach>
                            <c:forEach var="skill" items="${mr.missingSkills}">
                                <span class="skill-badge skill-missing">${skill} ✗</span>
                            </c:forEach>
                        </div>
                        <c:if test="${not empty mr.detail}">
                            <br/><small class="text-muted">${mr.detail}</small>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${not empty app.missingSkills}">
                            <c:forEach var="skill" items="${app.missingSkills}">
                                <span class="skill-badge skill-missing">${skill} ✗</span>
                            </c:forEach>
                        </c:if>
                        <c:if test="${empty app.missingSkills}">
                            <span class="text-muted">—</span>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${app.status == 'SUBMITTED'}"><span class="badge submitted">SUBMITTED</span></c:when>
                    <c:when test="${app.status == 'UNDER_REVIEW'}"><span class="badge under-review">UNDER_REVIEW</span></c:when>
                    <c:when test="${app.status == 'SELECTED'}"><span class="badge selected">SELECTED</span></c:when>
                    <c:otherwise><span class="badge rejected">REJECTED</span></c:otherwise>
                </c:choose>
            </td>
            <td class="actions">
                <c:choose>
                    <c:when test="${isFinal}">
                        <span class="text-muted">Finalised</span>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select" class="inline-form">
                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                            <input type="hidden" name="jobId" value="${selectedJobId}"/>
                            <input type="hidden" name="decision" value="UNDER_REVIEW"/>
                            <button type="submit" class="btn btn-secondary">Under review</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select" class="inline-form">
                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                            <input type="hidden" name="jobId" value="${selectedJobId}"/>
                            <input type="hidden" name="decision" value="SELECTED"/>
                            <button type="submit" class="btn btn-success"
                                ${capacityFull && app.status != 'SELECTED' ? 'disabled title="Capacity full"' : ''}>
                                Select
                            </button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select" class="inline-form">
                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                            <input type="hidden" name="jobId" value="${selectedJobId}"/>
                            <input type="hidden" name="decision" value="REJECTED"/>
                            <button type="submit" class="btn btn-danger">Reject</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</div>

<p class="text-muted">Status flow: SUBMITTED → UNDER_REVIEW → SELECTED / REJECTED</p>
<p class="footer-links">
    <a href="${pageContext.request.contextPath}/mo/dashboard">Back to MO dashboard</a>
</p>
</main>
</body>
</html>
