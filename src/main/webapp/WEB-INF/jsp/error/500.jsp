<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>500 - Server Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">500 - Server Error</h1>
    </header>
    <div class="card">
        <p>An unexpected error occurred on the server. Please try again later.</p>
        <p>
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Back to Home</a>
        </p>
    </div>
</main>
</body>
</html>
