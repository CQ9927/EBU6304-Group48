<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Admin — Users</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=ai3"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">User Management</h1>
        <p class="lead text-muted">Ban / unban accounts and reset passwords. Password hashes are never shown.</p>
    </header>

    <c:if test="${param.saved == 'ban'}"><div class="alert alert-success" role="status">User banned.</div></c:if>
    <c:if test="${param.saved == 'unban'}"><div class="alert alert-success" role="status">User unbanned.</div></c:if>
    <c:if test="${param.saved == 'password'}"><div class="alert alert-success" role="status">Password reset.</div></c:if>
    <c:if test="${param.error == 'self'}"><div class="alert alert-warning" role="alert">You cannot ban your own account.</div></c:if>
    <c:if test="${param.error == 'notfound'}"><div class="alert alert-warning" role="alert">User not found.</div></c:if>
    <c:if test="${param.error == 'invalid'}"><div class="alert alert-warning" role="alert">Invalid request.</div></c:if>

    <c:if test="${not empty users}">
    <%-- Quick stats --%>
    <c:set var="taCount" value="0"/>
    <c:set var="moCount" value="0"/>
    <c:set var="adminCount" value="0"/>
    <c:set var="bannedCount" value="0"/>
    <c:forEach var="u" items="${users}">
        <c:if test="${u.role == 'TA'}"><c:set var="taCount" value="${taCount + 1}"/></c:if>
        <c:if test="${u.role == 'MO'}"><c:set var="moCount" value="${moCount + 1}"/></c:if>
        <c:if test="${u.role == 'ADMIN'}"><c:set var="adminCount" value="${adminCount + 1}"/></c:if>
        <c:if test="${u.banned}"><c:set var="bannedCount" value="${bannedCount + 1}"/></c:if>
    </c:forEach>

    <div class="admin-stat-cards">
        <div class="admin-stat-card">
            <span class="admin-stat-card__value">${users.size()}</span>
            <span class="admin-stat-card__label">Total Users</span>
        </div>
        <div class="admin-stat-card">
            <span class="admin-stat-card__value">${taCount}</span>
            <span class="admin-stat-card__label" style="color: var(--color-success);">TAs</span>
        </div>
        <div class="admin-stat-card">
            <span class="admin-stat-card__value">${moCount}</span>
            <span class="admin-stat-card__label" style="color: var(--color-info);">MOs</span>
        </div>
        <div class="admin-stat-card" ${bannedCount > 0 ? 'style="border-color: #fecaca;"' : ''}>
            <span class="admin-stat-card__value" ${bannedCount > 0 ? 'style="color: var(--color-danger);"' : ''}>${bannedCount}</span>
            <span class="admin-stat-card__label">Banned</span>
        </div>
    </div>

    <div class="admin-panel">
    <div class="table-scroll">
        <table class="data-table">
            <thead>
            <tr>
                <th>User ID</th>
                <th>Username</th>
                <th>Role</th>
                <th>Banned</th>
                <th>Ban Reason</th>
                <th>Appeal</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td class="mono">${u.userId}</td>
                    <td><strong>${u.username}</strong></td>
                    <td>
                        <c:choose>
                            <c:when test="${u.role == 'ADMIN'}"><span class="badge-role badge-role--admin">ADMIN</span></c:when>
                            <c:when test="${u.role == 'MO'}"><span class="badge-role badge-role--mo">MO</span></c:when>
                            <c:otherwise><span class="badge-role badge-role--ta">TA</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${u.banned}"><span class="badge rejected">Yes</span></c:when>
                            <c:otherwise><span class="badge submitted">No</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${u.banReason}" default="—"/></td>
                    <td>
                        <c:if test="${not empty u.appealMessage}">
                            <small class="text-muted"><c:out value="${u.appealSubmittedAt}"/></small><br/>
                            <c:out value="${u.appealMessage}"/>
                        </c:if>
                        <c:if test="${empty u.appealMessage}">—</c:if>
                    </td>
                    <td class="td-actions">
                        <c:if test="${u.userId != selfUserId}">
                            <c:choose>
                                <c:when test="${u.banned}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/users" onsubmit="return confirm('Unban this user?');">
                                        <input type="hidden" name="action" value="unban"/>
                                        <input type="hidden" name="userId" value="${u.userId}"/>
                                        <button type="submit" class="btn btn-ghost btn-sm">Unban</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/users" class="ban-form" onsubmit="return confirm('Ban this user?');">
                                        <input type="hidden" name="action" value="ban"/>
                                        <input type="hidden" name="userId" value="${u.userId}"/>
                                        <textarea name="banReason" class="form-control" rows="2" required placeholder="Ban reason…"></textarea>
                                        <button type="submit" class="btn btn-ghost btn-sm">Ban</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <form method="post" action="${pageContext.request.contextPath}/admin/users" onsubmit="return confirm('Reset password for this user?');">
                            <input type="hidden" name="action" value="resetPassword"/>
                            <input type="hidden" name="userId" value="${u.userId}"/>
                            <input type="password" name="newPassword" placeholder="New password" required class="form-control reset-pw-input"/>
                            <button type="submit" class="btn btn-primary btn-sm">Reset PW</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    </div>

    </c:if>

    <c:if test="${empty users}">
        <div class="admin-panel"><p class="text-muted" style="text-align:center; padding:2rem 1rem;">No users found.</p></div>
    </c:if>
</main>
</body>
</html>
