<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Register</title>
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
        <h1 class="page-title">Register</h1>
        <% if (request.getAttribute("error") != null) { %>
        <p class="err"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="form-group">
                <label for="reg-username">Username</label>
                <input type="text" id="reg-username" name="username" autocomplete="username" required/>
            </div>
            <div class="form-group">
                <label for="reg-password">Password</label>
                <input type="password" id="reg-password" name="password" autocomplete="new-password" required minlength="4"/>
            </div>
            <div class="form-group">
                <label for="reg-confirm">Confirm password</label>
                <input type="password" id="reg-confirm" name="confirm" autocomplete="new-password" required/>
            </div>
            <div class="form-group">
                <label for="role">Role</label>
                <select id="role" name="role" required>
                    <option value="TA">TA (applicant)</option>
                    <option value="MO">MO (module organiser)</option>
                    <option value="ADMIN">Admin</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;">Register</button>
        </form>
        <p class="footer-links" style="border:none;margin-top:1.25rem;padding-top:0;">
            <a href="${pageContext.request.contextPath}/login">Already have an account</a>
        </p>
    </div>
</main>
</body>
</html>
