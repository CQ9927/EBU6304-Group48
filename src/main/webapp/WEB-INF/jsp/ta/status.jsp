<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Application Status</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 60rem; margin: 2rem auto; padding: 0 1rem; }
        .nav { display: flex; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid #dee2e6; }
        .nav a { color: #007bff; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
        table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        th, td { border-bottom: 1px solid #dee2e6; padding: 0.75rem; text-align: left; }
        th { background: #f8f9fa; }
        .badge { display: inline-block; padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; }
        .submitted { background: #e2e3e5; color: #383d41; }
        .under-review { background: #fff3cd; color: #856404; }
        .selected { background: #d4edda; color: #155724; }
        .rejected { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
<h1>My Application Status</h1>

<div class="nav">
    <a href="${pageContext.request.contextPath}/home">Home</a>
    <a href="${pageContext.request.contextPath}/ta/profile">Profile</a>
    <a href="${pageContext.request.contextPath}/ta/cv">CV</a>
    <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a>
    <a href="${pageContext.request.contextPath}/ta/status" style="font-weight: bold;">Status</a>
    <a href="${pageContext.request.contextPath}/logout" style="margin-left: auto;">Logout</a>
</div>

<p><a href="${pageContext.request.contextPath}/ta/status">Refresh</a> to get latest status updates.</p>

<c:choose>
    <c:when test="${not empty applications}">
        <table>
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
    </c:when>
    <c:otherwise>
        <p>No applications yet. Visit <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a> to apply.</p>
    </c:otherwise>
</c:choose>

</body>
</html>
