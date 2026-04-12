<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Admin — Applications</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">Applications</h1>
        <p class="lead text-muted">Revoke pending applications (sets status to REJECTED and marks admin revoke). Not allowed for SELECTED or already REJECTED.</p>
    </header>

    <c:if test="${param.saved == '1'}"><p class="app-notice app-notice--ok" role="status">Application revoked by admin.</p></c:if>
    <c:if test="${param.error == 'revoke'}"><p class="app-notice app-notice--warn" role="alert">Could not revoke (invalid state or not found).</p></c:if>
    <c:if test="${param.error == 'invalid'}"><p class="app-notice app-notice--warn" role="alert">Invalid request.</p></c:if>

    <div class="card card--flush">
        <div class="table-scroll">
            <table class="job-table">
                <thead>
                <tr>
                    <th>Application</th>
                    <th>Job</th>
                    <th>Applicant</th>
                    <th>Status</th>
                    <th>Admin revoke</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="app" items="${applications}">
                    <tr>
                        <td class="mono">${app.applicationId}</td>
                        <td>
                            ${app.jobId}
                            <c:if test="${not empty jobTitles[app.jobId]}"> — ${jobTitles[app.jobId]}</c:if>
                        </td>
                        <td class="mono">${app.applicantUserId}</td>
                        <td><span class="badge">${app.status}</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${app.adminRevoked}"><span class="badge rejected">yes</span></c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${app.status == 'SUBMITTED' || app.status == 'UNDER_REVIEW'}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/applications" onsubmit="return confirm('Revoke this application as admin?');">
                                    <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                    <button type="submit" class="btn btn-ghost">Revoke (admin)</button>
                                </form>
                            </c:if>
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
