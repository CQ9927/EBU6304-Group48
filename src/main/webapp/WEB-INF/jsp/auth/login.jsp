<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<main class="site-main site-main--auth">
    <div class="auth-card">
        <h1 class="page-title">Sign in</h1>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <% if (request.getParameter("next") != null) { %>
            <input type="hidden" name="next" value="${param.next}"/>
            <% } %>
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" autocomplete="username" required/>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" autocomplete="current-password" required/>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;margin-top:0.25rem;">Sign in</button>
        </form>
        <% if (request.getAttribute("message") != null) { %>
        <p class="msg"><%= request.getAttribute("message") %></p>
        <% } %>
        <p class="footer-links" style="border:none;margin-top:1.25rem;padding-top:0;">
            <a href="${pageContext.request.contextPath}/register">Create an account</a>
            ·
            <a href="${pageContext.request.contextPath}/home">Back to home</a>
        </p>
    </div>
</main>
</body>
</html>
