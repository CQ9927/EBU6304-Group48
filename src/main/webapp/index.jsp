<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>TA Recruitment — Group 48</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp">
    <jsp:param name="guest" value="true"/>
</jsp:include>
<p class="redirect-msg">Redirecting to home…</p>
<script>window.location.href = '${pageContext.request.contextPath}/home';</script>
<noscript><p class="redirect-msg"><a href="${pageContext.request.contextPath}/home">Go to home</a></p></noscript>
</body>
</html>
