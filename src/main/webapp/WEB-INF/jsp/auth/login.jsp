<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Login</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 28rem; margin: 2rem auto; padding: 0 1rem; }
        label { display: block; margin-top: 0.75rem; }
        input[type=text], input[type=password] { width: 100%; box-sizing: border-box; padding: 0.35rem; }
        button { margin-top: 1rem; padding: 0.4rem 0.8rem; }
        .msg { color: #666; margin-top: 1rem; }
    </style>
</head>
<body>
<h1>Login</h1>
<form method="post" action="${pageContext.request.contextPath}/login">
    <label>Username <input type="text" name="username" autocomplete="username"/></label>
    <label>Password <input type="password" name="password" autocomplete="current-password"/></label>
    <button type="submit">Sign in</button>
</form>
<% if (request.getAttribute("message") != null) { %>
<p class="msg"><%= request.getAttribute("message") %></p>
<% } %>
<p><a href="${pageContext.request.contextPath}/home">Back</a></p>
</body>
</html>
