<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Post New Job</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
<header class="page-header">
    <h1 class="page-title">Post new job</h1>
    <p class="lead lead--tight text-muted">Create a vacancy and review jobs in storage.</p>
</header>

<c:if test="${param.saved == '1'}">
    <div class="alert alert-success" role="status">Job has been saved.</div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-error" role="alert">${error}</div>
</c:if>

<div class="card">
<form method="post" action="${pageContext.request.contextPath}/mo/jobs/new" class="form-stack">
    <p>
        <label for="title">Title *</label><br/>
        <input type="text" id="title" name="title" required/>
    </p>
    <p>
        <label for="type">Type *</label><br/>
        <select id="type" name="type" required>
            <option value="">— choose —</option>
            <option value="MODULE">MODULE</option>
            <option value="INVIGILATION">INVIGILATION</option>
        </select>
    </p>
    <p>
        <label for="semester">Semester *</label><br/>
        <input type="text" id="semester" name="semester" placeholder="e.g. 2026_SPRING" required/>
    </p>
    <p>
        <label for="schedule">Schedule *</label><br/>
        <input type="text" id="schedule" name="schedule" placeholder="e.g. WED_18_20" required/>
    </p>
    <p>
        <label for="capacity">Capacity *</label><br/>
        <input type="number" id="capacity" name="capacity" min="1" required/>
    </p>
    <p>
        <label for="requiredSkills">Required skills (comma-separated)</label><br/>
        <input type="text" id="requiredSkills" name="requiredSkills" placeholder="Java, Teaching, Algorithms"/>
    </p>
    <p><button type="submit" class="btn btn-primary">Create job</button></p>
</form>
</div>

<p class="hint">Jobs in storage (newest first):</p>
<div class="card card--flush">
<div class="table-scroll">
<table class="data-table">
    <thead>
    <tr>
        <th>ID</th>
        <th>Title</th>
        <th>Type</th>
        <th>Semester</th>
        <th>Capacity</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="job" items="${jobs}">
        <tr>
            <td>${job.jobId}</td>
            <td>${job.title}</td>
            <td>${job.type}</td>
            <td>${job.semester}</td>
            <td>${job.capacity}</td>
            <td>${job.status}</td>
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
