<%@ page import="com.ebu6304.group48.util.SemesterFormat" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>My Invitations</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
<header class="page-header">
    <h1 class="page-title">My Invitations</h1>
    <p class="lead lead--tight text-muted">MO invitations to apply for specific positions.</p>
</header>

<c:if test="${param.accepted == '1'}">
    <div class="alert alert-success" role="status">Invitation accepted! Your application has been submitted.</div>
</c:if>
<c:if test="${param.declined == '1'}">
    <div class="alert alert-success" role="status">Invitation declined.</div>
</c:if>
<c:if test="${param.error == 'closed'}">
    <div class="alert alert-warning" role="alert">This job is no longer open.</div>
</c:if>
<c:if test="${param.error == 'applied'}">
    <div class="alert alert-warning" role="alert">You have already applied for this job.</div>
</c:if>
<c:if test="${param.error == 'deadline'}">
    <div class="alert alert-warning" role="alert">The application deadline for this job has passed.</div>
</c:if>
<c:if test="${param.error == 'noprofile'}">
    <div class="alert alert-warning" role="alert">Please complete your profile and upload a CV before accepting.</div>
</c:if>

<c:choose>
<c:when test="${empty pendingInvitations and empty processedInvitations}">
    <div class="empty-state-card">
        <h3>No invitations yet</h3>
        <p>When an MO sends you an invitation for a position, it will appear here. You can also browse and apply for jobs directly on the <a href="${pageContext.request.contextPath}/ta/jobs">Jobs page</a>.</p>
    </div>
</c:when>
<c:otherwise>

    <c:if test="${not empty pendingInvitations}">
    <h2 class="section__title">Pending (${pendingCount})</h2>
    <div class="invitation-cards">
        <c:forEach var="inv" items="${pendingInvitations}">
            <c:set var="job" value="${jobMap[inv.jobId]}"/>
            <c:set var="mr" value="${matchMap[inv.invitationId]}"/>
            <div class="invitation-card">
                <div class="invitation-card__header">
                    <h3 class="invitation-card__title">
                        <c:out value="${not empty job ? job.title : inv.jobId}"/>
                    </h3>
                    <span class="badge badge-pending">PENDING</span>
                </div>
                <div class="invitation-card__meta">
                    <c:if test="${not empty job}">
                        <span class="badge">${job.type}</span>
                        <span><%= SemesterFormat.label(((com.ebu6304.group48.model.Job)pageContext.getAttribute("job")).getSemester()) %></span>
                        <span>${job.schedule}</span>
                        <span>Cap: ${job.capacity}</span>
                    </c:if>
                </div>
                <c:if test="${not empty mr}">
                <div class="invitation-card__match">
                    <span class="match-score
                        <c:choose>
                            <c:when test="${mr.totalScore >= 70}">match-high</c:when>
                            <c:when test="${mr.totalScore >= 40}">match-medium</c:when>
                            <c:otherwise>match-low</c:otherwise>
                        </c:choose>">
                        Match: ${mr.totalScore}%
                    </span>
                    <small class="text-muted">
                        Skills: ${mr.skillScore}/50 | Schedule: ${mr.scheduleScore}/25 |
                        Major: ${mr.majorScore}/15 | Profile: ${mr.completenessScore}/10
                    </small>
                </div>
                <div class="invitation-card__skills">
                    <c:forEach var="sk" items="${mr.matchedSkills}">
                        <span class="skill-badge skill-match">${sk} &#x2713;</span>
                    </c:forEach>
                    <c:forEach var="sk" items="${mr.missingSkills}">
                        <span class="skill-badge skill-missing">${sk} &#x2717;</span>
                    </c:forEach>
                </div>
                </c:if>
                <div class="invitation-card__invited">
                    <small class="text-muted">Invited: ${inv.createdAt}</small>
                </div>
                <div class="invitation-card__actions">
                    <form method="post" action="${pageContext.request.contextPath}/ta/invitations" style="display:inline">
                        <input type="hidden" name="action" value="accept"/>
                        <input type="hidden" name="invitationId" value="${inv.invitationId}"/>
                        <button type="submit" class="btn btn-success">Accept</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/ta/invitations" style="display:inline" onsubmit="return confirm('Decline this invitation?');">
                        <input type="hidden" name="action" value="decline"/>
                        <input type="hidden" name="invitationId" value="${inv.invitationId}"/>
                        <button type="submit" class="btn btn-danger">Decline</button>
                    </form>
                </div>
            </div>
        </c:forEach>
    </div>
    </c:if>

    <c:if test="${not empty processedInvitations}">
    <h2 class="section__title" style="margin-top: 2rem;">History</h2>
    <div class="card card--flush">
    <div class="table-scroll">
    <table class="data-table">
        <thead>
        <tr>
            <th>Job</th>
            <th>Status</th>
            <th>Invited</th>
            <th>Updated</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="inv" items="${processedInvitations}">
            <c:set var="job" value="${jobMap[inv.jobId]}"/>
            <tr>
                <td><c:out value="${not empty job ? job.title : inv.jobId}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${inv.status == 'ACCEPTED'}"><span class="badge selected">ACCEPTED</span></c:when>
                        <c:when test="${inv.status == 'DECLINED'}"><span class="badge rejected">DECLINED</span></c:when>
                        <c:otherwise><span class="badge">${inv.status}</span></c:otherwise>
                    </c:choose>
                </td>
                <td>${inv.createdAt}</td>
                <td>${inv.updatedAt}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
    </div>
    </c:if>

</c:otherwise>
</c:choose>

<p class="footer-links">
    <a href="${pageContext.request.contextPath}/ta/dashboard">Back to Dashboard</a>
</p>
</main>
</body>
</html>
