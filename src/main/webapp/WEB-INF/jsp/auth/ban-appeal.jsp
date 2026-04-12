<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Account appeal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp">
    <jsp:param name="guest" value="true"/>
</jsp:include>
<main class="site-main site-main--auth auth-layout">
    <div class="auth-card" style="max-width: 32rem;">
        <h1 class="page-title">Account appeal</h1>

        <c:if test="${step == 'done'}">
            <p class="alert alert-success" role="status">Your appeal has been submitted. An administrator will review it. Please check back for updates.</p>
            <p><a href="${pageContext.request.contextPath}/login">Back to sign in</a></p>
        </c:if>

        <c:if test="${step == 'identify'}">
            <p class="auth-card__subtitle">Verify your identity to see the ban reason and submit an appeal.</p>
            <c:if test="${not empty identifyError}">
                <p class="alert alert-error" role="alert"><c:out value="${identifyError}"/></p>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/ban-appeal">
                <input type="hidden" name="action" value="identify"/>
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" class="form-control" autocomplete="username" required/>
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control" autocomplete="current-password" required/>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Verify and continue</button>
            </form>
            <p class="auth-footer" style="margin-top: 1rem;">
                <a href="${pageContext.request.contextPath}/login">Back to sign in</a>
            </p>
        </c:if>

        <c:if test="${step == 'appeal'}">
            <p class="auth-card__subtitle">Your account has been banned. Read the reason below, then submit your appeal.</p>
            <div class="card" style="margin: 1rem 0; padding: 1rem; text-align: left;">
                <strong>Ban reason</strong>
                <p style="margin: 0.5rem 0 0; white-space: pre-wrap;"><c:out value="${empty appealUser.banReason ? '(none)' : appealUser.banReason}"/></p>
            </div>
            <c:if test="${not empty appealUser.appealMessage}">
                <p class="text-muted" style="font-size: 0.875rem;">You already submitted an appeal on <c:out value="${appealUser.appealSubmittedAt}"/>. Submitting again will replace your previous message.</p>
            </c:if>
            <c:if test="${not empty appealError}">
                <p class="alert alert-error" role="alert"><c:out value="${appealError}"/></p>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/ban-appeal">
                <input type="hidden" name="action" value="submitAppeal"/>
                <div class="form-group">
                    <label for="appealText">Appeal message</label>
                    <textarea id="appealText" name="appealText" class="form-control" rows="6" required placeholder="Describe your situation…"></textarea>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Submit appeal</button>
            </form>
            <p class="auth-footer" style="margin-top: 1rem;">
                <a href="${pageContext.request.contextPath}/login">Back to sign in</a>
            </p>
        </c:if>
    </div>
</main>
</body>
</html>
