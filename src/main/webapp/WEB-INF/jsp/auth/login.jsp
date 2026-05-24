<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp">
    <jsp:param name="guest" value="true"/>
</jsp:include>
<main class="site-main site-main--auth auth-layout" id="main-content">
    <div class="auth-card">
        <h1 class="page-title">Sign in</h1>
        <p class="auth-card__subtitle">Use your course demo account or register a new user.</p>
        <c:if test="${not empty message}">
        <c:set var="msgClass" value="alert-info"/>
        <c:set var="msgRole" value="status"/>
        <c:if test="${fn:containsIgnoreCase(message, 'Invalid') || messageType == 'error'}">
            <c:set var="msgClass" value="alert-error"/>
            <c:set var="msgRole" value="alert"/>
        </c:if>
        <c:if test="${fn:containsIgnoreCase(message, 'successful') || fn:containsIgnoreCase(message, 'Registration')}">
            <c:set var="msgClass" value="alert-success"/>
        </c:if>
        <c:if test="${fn:containsIgnoreCase(message, 'access') || fn:containsIgnoreCase(message, 'banned')}">
            <c:set var="msgClass" value="alert-warning"/>
            <c:set var="msgRole" value="alert"/>
        </c:if>
        <div class="alert ${msgClass}" role="${msgRole}">
            <c:out value="${message}"/>
            <c:if test="${not empty appealPageHref}">
                <br/><a href="${appealPageHref}">Submit an appeal</a>
            </c:if>
        </div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <% if (request.getParameter("next") != null) { %>
            <input type="hidden" name="next" value="<c:out value='${param.next}'/>"/>
            <% } %>
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" autocomplete="username" required/>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" autocomplete="current-password" required/>
            </div>
            <button type="submit" class="btn btn-primary btn-block">Sign in</button>
        </form>
        <div class="auth-footer">
            <a href="${pageContext.request.contextPath}/register">Create an account</a>
            <span class="auth-footer__sep">·</span>
            <a href="${pageContext.request.contextPath}/home">Back to home</a>
        </div>
    </div>
</main>
</body>
</html>
