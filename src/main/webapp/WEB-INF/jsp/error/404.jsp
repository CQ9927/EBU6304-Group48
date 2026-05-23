<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>404 - Page Not Found</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">404 - Page Not Found</h1>
    </header>
    <div class="card">
        <p>The page you are looking for does not exist or has been moved.</p>
        <p>
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Back to Home</a>
        </p>
    </div>
</main>
</body>
</html>
