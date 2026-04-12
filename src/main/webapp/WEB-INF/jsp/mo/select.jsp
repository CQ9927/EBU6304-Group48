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

<div class="card card--flush">
<div class="table-scroll">
<table class="data-table">
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
                        <button type="submit" class="btn btn-success">Select</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/mo/jobs/select" class="inline-form">
                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                        <input type="hidden" name="jobId" value="${selectedJobId}"/>
                        <input type="hidden" name="decision" value="REJECTED"/>
                        <button type="submit" class="btn btn-danger">Reject</button>
                    </form>
                </c:if>
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
