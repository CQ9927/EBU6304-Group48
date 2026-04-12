<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Application Status</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<main class="site-main">
<h1 class="page-title">My Application Status</h1>

<div class="nav">
    <a href="${pageContext.request.contextPath}/home">Home</a>
    <a href="${pageContext.request.contextPath}/ta/profile">Profile</a>
    <a href="${pageContext.request.contextPath}/ta/cv">CV</a>
    <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a>
    <a href="${pageContext.request.contextPath}/ta/status" class="nav-link--current">Status</a>
    <a href="${pageContext.request.contextPath}/logout" class="nav-spacer">Logout</a>
</div>

<p class="text-muted"><a href="${pageContext.request.contextPath}/ta/status">Refresh</a> for the latest status.</p>

<c:choose>
    <c:when test="${not empty applications}">
        <div class="card" style="padding:0;overflow:hidden;">
        <table class="job-table">
            <thead>
            <tr>
                <th>Application ID</th>
                <th>Job</th>
                <th>Match Score</th>
                <th>Status</th>
                <th>Last Updated</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="app" items="${applications}">
                <tr>
                    <td>${app.applicationId}</td>
                    <td>
                        ${app.jobId}
                        <c:if test="${not empty jobTitles[app.jobId]}"> - ${jobTitles[app.jobId]}</c:if>
                    </td>
                    <td>${app.matchScore}%</td>
                    <td>
                        <c:choose>
                            <c:when test="${app.status == 'SUBMITTED'}"><span class="badge submitted">SUBMITTED</span></c:when>
                            <c:when test="${app.status == 'UNDER_REVIEW'}"><span class="badge under-review">UNDER_REVIEW</span></c:when>
                            <c:when test="${app.status == 'SELECTED'}"><span class="badge selected">SELECTED</span></c:when>
                            <c:otherwise><span class="badge rejected">REJECTED</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>${app.updatedAt}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
    </c:when>
    <c:otherwise>
        <p>No applications yet. Visit <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a> to apply.</p>
    </c:otherwise>
</c:choose>

</main>
</body>
</html>
