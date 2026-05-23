<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<main class="site-main site-main--auth auth-layout">
    <div class="auth-card">
        <h1 class="page-title">Sign in</h1>
        <p class="auth-card__subtitle">Use your course demo account or register a new user.</p>
        <% if (request.getAttribute("message") != null) {
            String m = String.valueOf(request.getAttribute("message"));
            String alertClass = "alert-info";
            if (m.contains("Invalid")) {
                alertClass = "alert-error";
            } else if (m.contains("successful") || m.contains("Registration")) {
                alertClass = "alert-success";
            } else if (m.contains("access") || m.contains("Access")) {
                alertClass = "alert-warning";
            } else if (m.toLowerCase().contains("banned")) {
                alertClass = "alert-warning";
            }
        %>
        <div class="alert <%= alertClass %>" role="status">
            <%= m %>
            <c:if test="${not empty appealPageHref}">
                <br/><a href="${appealPageHref}">Submit an appeal</a>
            </c:if>
        </div>
        <% } %>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <% if (request.getParameter("next") != null) { %>
            <input type="hidden" name="next" value="${param.next}"/>
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
