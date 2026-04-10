<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Select Applicants</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 70rem; margin: 2rem auto; padding: 0 1rem; }
        .ok { color: #0a7f2e; }
        .error { color: #b00020; }
        table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        th, td { border: 1px solid #ddd; padding: .5rem; text-align: left; vertical-align: top; }
        .actions form { display: inline; margin-right: .5rem; }
    </style>
</head>
<body>
<h1>MO Selection</h1>

<c:if test="${param.saved == '1'}">
    <p class="ok">Decision saved.</p>
</c:if>
<c:if test="${param.error == '1'}">
    <p class="error">Failed to save decision. Please retry.</p>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/mo/jobs/select">
    <label>Choose Job:</label>
    <select name="jobId">
        <option value="">-- all jobs --</option>
        <c:forEach var="job" items="${jobs}">
            <option value="${job.jobId}" ${selectedJobId == job.jobId ? 'selected' : ''}>
                    ${job.jobId} - ${job.title}
            </option>
        </c:forEach>
    </select>
    <button type="submit">Filter</button>
</form>

<table>
    <thead>
    <tr>
        <th>Application ID</th>
        <th>Job ID</th>
        <th>Applicant</th>
        <th>Match Score</th>
        <th>Missing Skills</th>
        <th>Status</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="app" items="${applications}">
        <tr>
            <td>${app.applicationId}</td>
            <td>${app.jobId}</td>
            <td>${app.applicantUserId}</td>
            <td>${app.matchScore}</td>
            <td>${app.missingSkills}</td>
            <td>${app.status}</td>
            <td class="actions">
                <c:if test="${app.status != 'SELECTED' && app.status != 'REJECTED'}">
                    <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select">
                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                        <input type="hidden" name="jobId" value="${selectedJobId}"/>
                        <input type="hidden" name="decision" value="SELECTED"/>
                        <button type="submit">Select</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select">
                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                        <input type="hidden" name="jobId" value="${selectedJobId}"/>
                        <input type="hidden" name="decision" value="REJECTED"/>
                        <button type="submit">Reject</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<p><a href="${pageContext.request.contextPath}/mo/dashboard">Back to MO dashboard</a></p>
</body>
</html>
