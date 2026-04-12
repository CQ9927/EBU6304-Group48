<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>TA Recruitment — Group 48</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="${pageContext.request.contextPath}/home">TA Recruitment</a>
    </div>
</header>
<p class="redirect-msg">Redirecting to home…</p>
<script>window.location.href = '${pageContext.request.contextPath}/home';</script>
<noscript><p class="redirect-msg"><a href="${pageContext.request.contextPath}/home">Go to home</a></p></noscript>
</body>
</html>
