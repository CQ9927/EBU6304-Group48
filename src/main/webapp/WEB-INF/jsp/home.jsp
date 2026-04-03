<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Home — TA Recruitment</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 42rem; margin: 2rem auto; padding: 0 1rem; }
        code { background: #f4f4f4; padding: 0.1rem 0.35rem; }
        nav a { margin-right: 1rem; }
    </style>
</head>
<body>
<h1>TA Recruitment System</h1>
<p>EBU6304 Group 48 — Servlet/JSP application.</p>
<p>Data directory (runtime): <code>${dataDirectory}</code></p>

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

<p style="margin-top:1.5rem">See <code>docs/DATA_SCHEMA.md</code> and <code>docs/TEAM_TASKS.md</code> for module ownership.</p>
</body>
</html>
