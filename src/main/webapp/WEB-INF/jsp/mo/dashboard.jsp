<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>MO Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<main class="site-main">
    <h1 class="page-title">MO dashboard</h1>
    <p class="lead">Welcome, <strong>${username}</strong>.</p>
    <div class="card">
        <p style="margin-top:0;">
            <a href="${pageContext.request.contextPath}/mo/jobs/new">Post a new job</a>
            ·
            <a href="${pageContext.request.contextPath}/mo/jobs/select">Review applications</a>
        </p>
    </div>
    <p class="footer-links">
        <a href="${pageContext.request.contextPath}/home">Home</a>
        ·
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </p>
</main>
</body>
</html>
