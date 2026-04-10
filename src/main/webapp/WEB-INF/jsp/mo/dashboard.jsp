<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>MO Dashboard</title>
    <style>body { font-family: system-ui, sans-serif; max-width: 40rem; margin: 2rem auto; padding: 0 1rem; }</style>
</head>
<body>
<h1>MO dashboard</h1>
<p>Welcome, <strong>${username}</strong>.</p>
<p>
    <a href="${pageContext.request.contextPath}/mo/jobs/new">Post a new job</a>
    ·
    <a href="${pageContext.request.contextPath}/mo/jobs/select">Review applications</a>
</p>
<p><a href="${pageContext.request.contextPath}/home">Home</a> · <a href="${pageContext.request.contextPath}/logout">Logout</a></p>
</body>
</html>
