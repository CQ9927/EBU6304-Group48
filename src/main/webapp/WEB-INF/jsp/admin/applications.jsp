<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Admin — Applications</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=ai3"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">Applications</h1>
        <p class="lead text-muted">Review and revoke applications. Finalised applications (SELECTED / REJECTED) cannot be revoked here.</p>
    </header>

    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Application revoked by admin.</div></c:if>
    <c:if test="${param.error == 'revoke'}"><div class="alert alert-warning" role="alert">Could not revoke (invalid state or not found).</div></c:if>
    <c:if test="${param.error == 'invalid'}"><div class="alert alert-warning" role="alert">Invalid request.</div></c:if>

    <c:if test="${empty applications}">
        <div class="admin-panel"><p class="text-muted" style="text-align:center; padding:2rem 1rem;">No applications found.</p></div>
    </c:if>

    <c:if test="${not empty applications}">
    <div class="admin-panel">

        <%-- Inline filter bar --%>
        <form method="get" action="${pageContext.request.contextPath}/admin/applications" class="filter-bar--inline">
            <div class="form-group">
                <label for="status">Filter by Status</label>
                <select id="status" name="status">
                    <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>All</option>
                    <option value="SUBMITTED" ${statusFilter == 'SUBMITTED' ? 'selected' : ''}>Submitted</option>
                    <option value="UNDER_REVIEW" ${statusFilter == 'UNDER_REVIEW' ? 'selected' : ''}>Under Review</option>
                    <option value="SELECTED" ${statusFilter == 'SELECTED' ? 'selected' : ''}>Selected</option>
                    <option value="REJECTED" ${statusFilter == 'REJECTED' ? 'selected' : ''}>Rejected</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary btn-sm">Filter</button>
            <c:if test="${statusFilter != 'ALL'}">
                <a href="${pageContext.request.contextPath}/admin/applications" class="btn btn-ghost btn-sm">Clear</a>
            </c:if>
        </form>

        <%-- Result count --%>
        <p class="result-count">
            Showing <strong>${applications.size()}</strong> application<c:if test="${applications.size() != 1}">s</c:if>
            <c:if test="${statusFilter != 'ALL'}">with status <strong>${statusFilter}</strong></c:if>
        </p>

        <div class="table-scroll">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Application</th>
                    <th>Job</th>
                    <th>Applicant</th>
                    <th>Match</th>
                    <th>Status</th>
                    <th>Admin Revoke</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="app" items="${applications}">
                    <tr>
                        <td class="mono">${app.applicationId}</td>
                        <td>
                            ${app.jobId}
                            <c:if test="${not empty jobTitles[app.jobId]}"><br/><small class="text-muted">${jobTitles[app.jobId]}</small></c:if>
                        </td>
                        <td class="mono">${app.applicantUserId}</td>
                        <td>
                            <c:set var="score" value="${matchScores[app.applicationId]}"/>
                            <c:choose>
                                <c:when test="${not empty score}">
                                    <span class="match-pill
                                        <c:choose>
                                            <c:when test="${score >= 70}">match-high</c:when>
                                            <c:when test="${score >= 40}">match-medium</c:when>
                                            <c:otherwise>match-low</c:otherwise>
                                        </c:choose>">
                                        ${score}%
                                    </span>
                                </c:when>
                                <c:otherwise><span class="text-muted">—</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${app.status == 'SUBMITTED'}"><span class="badge submitted">SUBMITTED</span></c:when>
                                <c:when test="${app.status == 'UNDER_REVIEW'}"><span class="badge under-review">UNDER REVIEW</span></c:when>
                                <c:when test="${app.status == 'SELECTED'}"><span class="badge selected">SELECTED</span></c:when>
                                <c:otherwise><span class="badge rejected">REJECTED</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${app.adminRevoked}"><span class="badge rejected">Yes</span></c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${app.status == 'SUBMITTED' || app.status == 'UNDER_REVIEW'}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/applications" onsubmit="return confirm('Revoke this application as admin?');">
                                    <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                    <button type="submit" class="btn btn-ghost btn-sm">Revoke</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    </c:if>
</main>
</body>
</html>
