<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Home — TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<main class="site-main">
    <h1 class="page-title">TA Recruitment System</h1>
    <p class="lead">EBU6304 Group 48 — Servlet/JSP application.</p>
    <p class="text-muted">Data directory (runtime): <code>${dataDirectory}</code></p>

    <div class="home-card">
<% if (Boolean.TRUE.equals(request.getAttribute("loggedIn"))) { %>
    <p>Signed in as <strong>${username}</strong> (${role}).</p>
    <nav>
    <% if ("TA".equals(request.getAttribute("role"))) { %>
        <a href="${pageContext.request.contextPath}/ta/dashboard">TA dashboard</a>
    <% } %>
    <% if ("MO".equals(request.getAttribute("role"))) { %>
        <a href="${pageContext.request.contextPath}/mo/dashboard">MO dashboard</a>
    <% } %>
    <% if ("ADMIN".equals(request.getAttribute("role"))) { %>
        <a href="${pageContext.request.contextPath}/admin/workload">Admin workload</a>
    <% } %>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </nav>
<% } else { %>
    <nav>
        <a href="${pageContext.request.contextPath}/login">Login</a>
        <a href="${pageContext.request.contextPath}/register">Register</a>
    </nav>
<% } %>
    </div>

    <p class="footer-links text-muted">See <code>docs/DATA_SCHEMA.md</code> and <code>docs/TEAM_TASKS.md</code> for module ownership.</p>
</main>
</body>
</html>
