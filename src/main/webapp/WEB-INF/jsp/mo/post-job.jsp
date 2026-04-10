<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Post New Job</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 60rem; margin: 2rem auto; padding: 0 1rem; }
        .hint { color: #666; font-size: .95rem; }
        .ok { color: #0a7f2e; }
        .error { color: #b00020; }
        table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        th, td { border: 1px solid #ddd; padding: .5rem; text-align: left; }
    </style>
</head>
<body>
<h1>Post New Job</h1>

<c:if test="${param.saved == '1'}">
    <p class="ok">Job has been saved.</p>
</c:if>
<c:if test="${not empty error}">
    <p class="error">${error}</p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/mo/jobs/new">
    <p>Title*<br/><input type="text" name="title" style="width: 100%;" required/></p>
    <p>
        Type*<br/>
        <select name="type" required>
            <option value="">-- choose --</option>
            <option value="MODULE">MODULE</option>
            <option value="INVIGILATION">INVIGILATION</option>
        </select>
    </p>
    <p>Semester*<br/><input type="text" name="semester" placeholder="e.g. 2026_SPRING" required/></p>
    <p>Schedule*<br/><input type="text" name="schedule" placeholder="e.g. WED_18_20" required/></p>
    <p>Capacity*<br/><input type="number" name="capacity" min="1" required/></p>
    <p>Required Skills (comma-separated)<br/><input type="text" name="requiredSkills" style="width: 100%;" placeholder="Java, Teaching, Algorithms"/></p>
    <p><button type="submit">Create Job</button></p>
</form>

<p class="hint">Jobs in storage (newest first):</p>
<table>
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

<p><a href="${pageContext.request.contextPath}/mo/dashboard">Back to MO dashboard</a></p>
</body>
</html>
