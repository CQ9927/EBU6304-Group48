<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Application Status</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
<header class="page-header">
    <h1 class="page-title">My application status</h1>
    <p class="lead lead--tight text-muted"><a href="${pageContext.request.contextPath}/ta/status">Refresh</a> for the latest updates.</p>
</header>

<c:if test="${param.withdrawn == '1'}">
    <div class="alert alert-success" role="status">Application withdrawn successfully.</div>
</c:if>

<c:choose>
    <c:when test="${not empty applications}">
        <div class="card card--flush">
        <div class="table-scroll">
        <table class="job-table">
            <thead>
            <tr>
                <th>Application ID</th>
                <th>Job</th>
                <th>Match</th>
                <th>Missing Skills</th>
                <th>Status</th>
                <th>Feedback</th>
                <th>Last Updated</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="app" items="${applications}">
                <c:set var="mr" value="${matchResultMap[app.applicationId]}"/>
                <tr>
                    <td>${app.applicationId}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/ta/jobs">${jobTitles[app.jobId]}</a>
                    </td>
                    <td>
                        <c:choose>
                        <c:when test="${app.matchScore != null}">
                            ${app.matchScore}%
                            <c:if test="${not empty mr}">
                                <br><small class="text-muted">${mr.detail}</small>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">N/A</span>
                        </c:otherwise>
                    </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty app.missingSkills}">
                                <c:forEach var="skill" items="${app.missingSkills}">
                                    <span class="skill-badge skill-missing">${skill}</span>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">None — all skills matched</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${app.status == 'SUBMITTED'}"><span class="badge submitted">SUBMITTED</span></c:when>
                            <c:when test="${app.status == 'UNDER_REVIEW'}"><span class="badge under-review">UNDER_REVIEW</span></c:when>
                            <c:when test="${app.status == 'SELECTED'}"><span class="badge selected">SELECTED</span></c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${app.status == 'REJECTED' && app.adminRevoked}">
                                        <span class="badge rejected" title="Revoked by administrator">REJECTED (admin)</span>
                                    </c:when>
                                    <c:otherwise><span class="badge rejected">REJECTED</span></c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty app.note}">
                                <c:out value="${app.note}"/>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${fn:substring(app.updatedAt, 0, 10)} ${fn:substring(app.updatedAt, 11, 16)}</td>
                    <td>
                        <c:if test="${app.status == 'SUBMITTED'}">
                            <form method="post" action="${pageContext.request.contextPath}/ta/status" onsubmit="return confirm('Withdraw this application? This action cannot be undone.');">
                                <input type="hidden" name="action" value="withdraw"/>
                                <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                <button type="submit" class="btn btn-ghost btn-sm" style="color:#d32f2f;">Withdraw</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
        </div>
    </c:when>
    <c:otherwise>
        <p>No applications yet. Visit <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a> to apply.</p>
    </c:otherwise>
</c:choose>

</main>
</body>
</html>
