<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>TA Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">TA dashboard</h1>
        <p class="lead">Welcome, <strong>${username}</strong>. Here is a quick snapshot of open roles and your applications.</p>
    </header>

    <div class="stats-grid">
        <div class="card"><div class="label">Open positions</div><div class="value">${openJobsCount}</div></div>
        <div class="card"><div class="label">My applications</div><div class="value">${myApplicationsTotal}</div></div>
        <div class="card"><div class="label">Submitted</div><div class="value">${myApplicationsSubmitted}</div></div>
        <div class="card"><div class="label">Under review</div><div class="value">${myApplicationsUnderReview}</div></div>
        <div class="card"><div class="label">Selected</div><div class="value">${myApplicationsSelected}</div></div>
        <div class="card"><div class="label">Rejected</div><div class="value">${myApplicationsRejected}</div></div>
    </div>

    <div class="card dashboard-actions">
        <p class="card__label">Next steps</p>
        <div class="action-row">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/ta/jobs">Browse jobs</a>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/ta/status">View application status</a>
        </div>
    </div>
</main>
</body>
</html>
