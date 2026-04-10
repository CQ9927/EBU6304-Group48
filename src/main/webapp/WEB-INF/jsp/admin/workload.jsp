<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Admin - Workload</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 75rem; margin: 2rem auto; padding: 0 1rem; color: #1f2937; }
        .nav { display: flex; gap: 1rem; margin-bottom: 1rem; border-bottom: 1px solid #e5e7eb; padding-bottom: .75rem; }
        .nav a { color: #2563eb; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
        .stats { display: grid; gap: .75rem; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); margin: 1rem 0 1.5rem; }
        .card { border: 1px solid #e5e7eb; border-radius: .5rem; background: #f8fafc; padding: .85rem; }
        .label { font-size: .82rem; color: #475569; }
        .value { font-size: 1.4rem; font-weight: 700; margin-top: .25rem; }
        .panel { border: 1px solid #e5e7eb; border-radius: .5rem; padding: 1rem; margin-top: 1rem; }
        .hint-list { margin: .5rem 0 0; }
        .hint-list li { margin: .25rem 0; }
        .ok { color: #166534; }
        .warn { color: #b45309; }
        table { width: 100%; border-collapse: collapse; margin-top: .75rem; }
        th, td { border: 1px solid #e5e7eb; padding: .5rem; text-align: left; vertical-align: top; }
        th { background: #f1f5f9; }
        .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; }
        .danger { color: #b91c1c; font-weight: 700; }
    </style>
</head>
<body>
<h1>Admin Workload Dashboard</h1>
<p>Welcome, <strong>${username}</strong>.</p>

<div class="nav">
    <a href="${pageContext.request.contextPath}/home">Home</a>
    <a href="${pageContext.request.contextPath}/admin/workload" style="font-weight: 700;">Refresh workload</a>
    <a href="${pageContext.request.contextPath}/logout" style="margin-left: auto;">Logout</a>
</div>

<div class="stats">
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
    <h2>Quick Rule Signals</h2>
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
    <h2>Per-job Workload</h2>
    <c:choose>
        <c:when test="${not empty snapshot.rows}">
            <table>
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
                                    -
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>No job data yet. Ask MO to create at least one job in <span class="mono">jobs.json</span>.</p>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
