<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Invite TAs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
<header class="page-header">
    <h1 class="page-title">Invite TAs to Job</h1>
    <p class="lead lead--tight text-muted">Browse all TAs ranked by match score and send invitations.</p>
</header>

<c:if test="${param.saved == '1'}">
    <div class="alert alert-success" role="status">Invitation sent successfully.</div>
</c:if>
<c:if test="${param.error == 'duplicate'}">
    <div class="alert alert-warning" role="alert">Invitation already sent to this TA for this job.</div>
</c:if>
<c:if test="${param.error == 'closed'}">
    <div class="alert alert-error" role="alert">This job is no longer open.</div>
</c:if>
<c:if test="${param.error == '1'}">
    <div class="alert alert-error" role="alert">Failed to send invitation. Please try again.</div>
</c:if>

<div class="card">
<form method="get" action="${pageContext.request.contextPath}/mo/jobs/invite" class="filter-bar">
    <div class="form-group">
        <label for="jobId">Choose job</label>
        <select id="jobId" name="jobId">
            <c:forEach var="job" items="${jobs}">
                <option value="${job.jobId}" ${selectedJobId == job.jobId ? 'selected' : ''}>
                    ${job.jobId} - ${job.title} (${job.status})
                </option>
            </c:forEach>
        </select>
    </div>
    <button type="submit" class="btn btn-primary">Filter</button>
</form>
</div>

<c:if test="${not empty selectedJob}">
<div class="stats stats--section">
    <div class="stat-item">
        <div class="stat-value">${selectedJob.title}</div>
        <div class="stat-label">Job</div>
    </div>
    <div class="stat-item">
        <div class="stat-value">${fn:length(taUsers)}</div>
        <div class="stat-label">TAs Available</div>
    </div>
    <div class="stat-item">
        <div class="stat-value">${selectedJob.capacity}</div>
        <div class="stat-label">Capacity</div>
    </div>
</div>
</c:if>

<div class="card card--flush">
<div class="table-scroll">
<table class="data-table invite-table">
    <thead>
    <tr>
        <th>TA</th>
        <th>Profile</th>
        <th>Match</th>
        <th>Skills</th>
        <th>Status</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="ta" items="${taUsers}">
        <c:set var="profile" value="${profileByUserId[ta.userId]}"/>
        <c:set var="mr" value="${matchResultMap[ta.userId]}"/>
        <c:set var="hasApplied" value="${appliedUserIds.contains(ta.userId)}"/>
        <c:set var="hasInvited" value="${invitedUserIds.contains(ta.userId)}"/>
        <c:set var="isSelected" value="${selectedUserIds.contains(ta.userId)}"/>

        <tr>
            <td>
                <strong>${ta.username}</strong>
                <br/><small class="text-muted">${ta.userId}</small>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty profile}">
                        <strong><c:out value="${profile.name}"/></strong><br/>
                        <small class="text-muted"><c:out value="${profile.major}"/></small><br/>
                        <small class="text-muted"><c:out value="${profile.email}"/></small>
                        <c:if test="${not empty profile.aiSummary}">
                            <div class="ai-summary-badge">
                                <strong>AI Summary:</strong> <c:out value="${profile.aiSummary}"/>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">No profile</span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty mr}">
                        <span class="match-score
                            <c:choose>
                                <c:when test="${mr.totalScore >= 70}">match-high</c:when>
                                <c:when test="${mr.totalScore >= 40}">match-medium</c:when>
                                <c:otherwise>match-low</c:otherwise>
                            </c:choose>">
                            <strong>${mr.totalScore}/100</strong>
                        </span>
                        <br/><small class="text-muted">
                            S:${mr.skillScore}/50 T:${mr.scheduleScore}/25
                            M:${mr.majorScore}/15 P:${mr.completenessScore}/10
                        </small>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">N/A</span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${not empty mr}">
                        <c:forEach var="sk" items="${mr.matchedSkills}">
                            <span class="skill-badge skill-match">${sk} &#x2713;</span>
                        </c:forEach>
                        <c:forEach var="sk" items="${mr.missingSkills}">
                            <span class="skill-badge skill-missing">${sk} &#x2717;</span>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">—</span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${isSelected}"><span class="badge selected">SELECTED</span></c:when>
                    <c:when test="${hasApplied}"><span class="badge submitted">APPLIED</span></c:when>
                    <c:when test="${hasInvited}"><span class="badge badge-pending">INVITED</span></c:when>
                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${isSelected or hasApplied or hasInvited}">
                        <span class="text-muted">—</span>
                    </c:when>
                    <c:when test="${empty profile}">
                        <span class="text-muted">No profile</span>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${pageContext.request.contextPath}/mo/jobs/invite" onsubmit="return confirm('Send invitation to this TA?');">
                            <input type="hidden" name="jobId" value="${selectedJobId}"/>
                            <input type="hidden" name="taUserId" value="${ta.userId}"/>
                            <button type="submit" class="btn btn-primary btn-sm">Send Invitation</button>
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

<p class="footer-links">
    <a href="${pageContext.request.contextPath}/mo/dashboard">Back to MO dashboard</a>
</p>
</main>
</body>
</html>
