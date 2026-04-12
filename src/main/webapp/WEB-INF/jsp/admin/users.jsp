<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Admin — Users</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">User management</h1>
        <p class="lead text-muted">Ban / unban accounts and reset passwords. Password hashes are never shown.</p>
    </header>

    <c:if test="${param.saved == 'ban'}"><p class="app-notice app-notice--ok" role="status">User banned.</p></c:if>
    <c:if test="${param.saved == 'unban'}"><p class="app-notice app-notice--ok" role="status">User unbanned.</p></c:if>
    <c:if test="${param.saved == 'password'}"><p class="app-notice app-notice--ok" role="status">Password reset.</p></c:if>
    <c:if test="${param.error == 'self'}"><p class="app-notice app-notice--warn" role="alert">You cannot ban your own account.</p></c:if>
    <c:if test="${param.error == 'notfound'}"><p class="app-notice app-notice--warn" role="alert">User not found.</p></c:if>
    <c:if test="${param.error == 'invalid'}"><p class="app-notice app-notice--warn" role="alert">Invalid request.</p></c:if>

    <div class="card card--flush">
        <div class="table-scroll">
            <table class="job-table">
                <thead>
                <tr>
                    <th>User ID</th>
                    <th>Username</th>
                    <th>Role</th>
                    <th>Banned</th>
                    <th>Ban reason</th>
                    <th>Appeal</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td class="mono">${u.userId}</td>
                        <td>${u.username}</td>
                        <td>${u.role}</td>
                        <td>
                            <c:choose>
                                <c:when test="${u.banned}"><span class="badge rejected">yes</span></c:when>
                                <c:otherwise><span class="badge submitted">no</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td style="max-width:14rem; white-space:pre-wrap; font-size:0.875rem;"><c:out value="${u.banReason}"/></td>
                        <td style="max-width:16rem; white-space:pre-wrap; font-size:0.875rem;">
                            <c:if test="${not empty u.appealMessage}">
                                <span class="text-muted"><c:out value="${u.appealSubmittedAt}"/></span><br/>
                                <c:out value="${u.appealMessage}"/>
                            </c:if>
                            <c:if test="${empty u.appealMessage}">—</c:if>
                        </td>
                        <td>
                            <c:if test="${u.userId != selfUserId}">
                                <c:choose>
                                    <c:when test="${u.banned}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline;">
                                            <input type="hidden" name="action" value="unban"/>
                                            <input type="hidden" name="userId" value="${u.userId}"/>
                                            <button type="submit" class="btn btn-ghost">Unban</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:flex; flex-direction:column; gap:0.35rem; align-items:flex-start; max-width:22rem;">
                                            <input type="hidden" name="action" value="ban"/>
                                            <input type="hidden" name="userId" value="${u.userId}"/>
                                            <label class="text-muted" style="font-size:0.8rem;">Ban reason (required)</label>
                                            <textarea name="banReason" class="form-control" rows="2" required placeholder="Explain why this account is being banned…"></textarea>
                                            <button type="submit" class="btn btn-ghost">Ban</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline-flex; gap:0.25rem; align-items:center; margin-left:0.5rem;">
                                <input type="hidden" name="action" value="resetPassword"/>
                                <input type="hidden" name="userId" value="${u.userId}"/>
                                <input type="password" name="newPassword" placeholder="New password" required class="form-control" style="max-width:12rem;"/>
                                <button type="submit" class="btn btn-primary">Reset</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
