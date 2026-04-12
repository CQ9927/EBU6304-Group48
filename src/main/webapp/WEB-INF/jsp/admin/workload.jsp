<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Admin — Workload</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
<header class="page-header">
    <h1 class="page-title">Admin workload</h1>
    <p class="lead">Welcome, <strong>${username}</strong>.</p>
</header>

<div class="stats-grid">
    <div class="card"><div class="label">Total Jobs</div><div class="value">${snapshot.totalJobs}</div></div>
    <div class="card"><div class="label">Open Jobs</div><div class="value">${snapshot.openJobs}</div></div>
    <div class="card"><div class="label">Closed Jobs</div><div class="value">${snapshot.closedJobs}</div></div>
    <div class="card"><div class="label">Total Applications</div><div class="value">${snapshot.totalApplications}</div></div>
    <div class="card"><div class="label">Submitted</div><div class="value">${snapshot.submittedApplications}</div></div>
    <div class="card"><div class="label">Under Review</div><div class="value">${snapshot.underReviewApplications}</div></div>
    <div class="card"><div class="label">Selected</div><div class="value">${snapshot.selectedApplications}</div></div>
    <div class="card"><div class="label">Rejected</div><div class="value">${snapshot.rejectedApplications}</div></div>
</div>

<div class="panel">
    <h2>Quick rule signals</h2>
    <p class="${snapshot.openJobsWithoutSelection > 0 ? 'warn' : 'ok'}">
        Open jobs without any selected TA: <strong>${snapshot.openJobsWithoutSelection}</strong>
    </p>
    <c:choose>
        <c:when test="${not empty snapshot.hints}">
            <ul class="hint-list">
                <c:forEach var="hint" items="${snapshot.hints}">
                    <li>${hint}</li>
                </c:forEach>
            </ul>
        </c:when>
        <c:otherwise>
            <p class="ok">No conflicts detected from current jobs/applications data.</p>
        </c:otherwise>
    </c:choose>
</div>

<div class="panel">
    <h2>Per-job workload</h2>
    <c:choose>
        <c:when test="${not empty snapshot.rows}">
            <div class="table-scroll">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Job</th>
                    <th>Status</th>
                    <th>Capacity</th>
                    <th>Total Apps</th>
                    <th>Submitted</th>
                    <th>Under Review</th>
                    <th>Selected</th>
                    <th>Rejected</th>
                    <th>Remaining</th>
                    <th>Risk</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="row" items="${snapshot.rows}">
                    <tr>
                        <td><span class="mono">${row.jobId}</span><br/>${row.title}</td>
                        <td>${row.status}</td>
                        <td>${row.capacity}</td>
                        <td>${row.totalApplications}</td>
                        <td>${row.submittedCount}</td>
                        <td>${row.underReviewCount}</td>
                        <td>${row.selectedCount}</td>
                        <td>${row.rejectedCount}</td>
                        <td>${row.remainingSlots}</td>
                        <td>
                            <c:choose>
                                <c:when test="${row.overCapacity}">
                                    <span class="danger">OVER CAPACITY</span>
                                </c:when>
                                <c:otherwise>
                                    —
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            </div>
        </c:when>
        <c:otherwise>
            <p>No job data yet. Ask MO to create at least one job in <span class="mono">jobs.json</span>.</p>
        </c:otherwise>
    </c:choose>
</div>
</main>
</body>
</html>
