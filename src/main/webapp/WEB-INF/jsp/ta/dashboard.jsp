<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>TA Dashboard</title>
    <style>body { font-family: system-ui, sans-serif; max-width: 40rem; margin: 2rem auto; padding: 0 1rem; }</style>
</head>
<body>
<h1>TA dashboard</h1>
<p>Welcome, <strong>${username}</strong>.</p>
<p><a href="${pageContext.request.contextPath}/ta/profile">Profile</a> ·
   <a href="${pageContext.request.contextPath}/ta/cv">CV</a> ·
   <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a> ·
   <a href="${pageContext.request.contextPath}/ta/status">Status</a></p>
<p>More detail: <code>docs/ROUTES_AND_MODULES.md</code>.</p>
<p><a href="${pageContext.request.contextPath}/home">Home</a> · <a href="${pageContext.request.contextPath}/logout">Logout</a></p>
</body>
</html>
