<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>MO Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">MO dashboard</h1>
        <p class="lead">Welcome, <strong>${username}</strong>. Summary of jobs you posted and applications awaiting action.</p>
    </header>

    <div class="stats-grid stats-grid--narrow">
        <div class="card"><div class="label">My job posts</div><div class="value">${myJobsTotal}</div></div>
        <div class="card"><div class="label">My open jobs</div><div class="value">${myOpenJobs}</div></div>
        <div class="card"><div class="label">Applications to review</div><div class="value">${pendingApplications}</div></div>
    </div>

    <div class="card dashboard-actions">
        <p class="card__label">Actions</p>
        <div class="action-row">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/mo/jobs/new">Post a new job</a>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/mo/jobs/select">Review applications</a>
        </div>
    </div>
</main>
</body>
</html>
