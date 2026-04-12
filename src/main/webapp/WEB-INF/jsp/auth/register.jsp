<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Register</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp">
    <jsp:param name="guest" value="true"/>
</jsp:include>
<main class="site-main site-main--auth auth-layout">
    <div class="auth-card">
        <h1 class="page-title">Register</h1>
        <p class="auth-card__subtitle">Choose a role for the coursework demo. Passwords are stored as a simple hash only.</p>
        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error" role="alert"><%= request.getAttribute("error") %></div>
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
            <button type="submit" class="btn btn-primary btn-block">Create account</button>
        </form>
        <div class="auth-footer">
            <a href="${pageContext.request.contextPath}/login">Already have an account</a>
        </div>
    </div>
</main>
</body>
</html>
