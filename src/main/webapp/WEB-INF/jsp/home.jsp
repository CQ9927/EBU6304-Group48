<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Home — TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp">
    <jsp:param name="guest" value="true"/>
</jsp:include>
<main class="site-main site-main--landing">
    <div class="home-layout">
        <div class="home-layout__intro">
            <header class="page-header">
                <h1 class="page-title">TA Recruitment System</h1>
                <p class="lead">Teaching assistant hiring for module organisers and applicants — sign in to continue.</p>
            </header>
            <ul class="home-features" aria-label="What you can do">
                <li><strong class="home-features__strong">Teaching assistants</strong> browse open jobs, upload a CV, and track application status.</li>
                <li><strong class="home-features__strong">Module organisers</strong> post vacancies and review applicants for their modules.</li>
                <li><strong class="home-features__strong">Course demo</strong> — accounts and data are for local demonstration only.</li>
            </ul>
        </div>
        <div class="home-card">
            <p class="text-muted home-card__hint">Sign in with your account or register as a new teaching assistant.</p>
            <div class="action-row">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Login</a>
                <a class="btn btn-ghost" href="${pageContext.request.contextPath}/register">Register</a>
            </div>
        </div>
    </div>
</main>
</body>
</html>
