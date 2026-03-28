<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Register</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 28rem; margin: 2rem auto; padding: 0 1rem; }
        label { display: block; margin-top: 0.75rem; }
        input[type=text], input[type=password], select { width: 100%; box-sizing: border-box; padding: 0.35rem; }
        button { margin-top: 1rem; padding: 0.4rem 0.8rem; }
        .err { color: #a30; margin-top: 1rem; }
    </style>
</head>
<body>
<h1>Register</h1>
<% if (request.getAttribute("error") != null) { %>
<p class="err"><%= request.getAttribute("error") %></p>
<% } %>
<form method="post" action="${pageContext.request.contextPath}/register">
    <label>Username <input type="text" name="username" autocomplete="username" required/></label>
    <label>Password <input type="password" name="password" autocomplete="new-password" required minlength="4"/></label>
    <label>Confirm password <input type="password" name="confirm" autocomplete="new-password" required/></label>
    <label>Role
        <select name="role" required>
            <option value="TA">TA (applicant)</option>
            <option value="MO">MO (module organiser)</option>
            <option value="ADMIN">Admin</option>
        </select>
    </label>
    <button type="submit">Register</button>
</form>
<p><a href="${pageContext.request.contextPath}/login">Already have an account</a></p>
</body>
</html>
