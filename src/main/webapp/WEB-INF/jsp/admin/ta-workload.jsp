<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Admin — TA Workload</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=ai3"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">TA Workload Overview</h1>
        <p class="lead text-muted">View each TA's profile summary, application pipeline, and estimated weekly hours from selected positions.</p>
    </header>

    <c:if test="${param.saved == 'threshold'}">
        <div class="alert alert-success" role="status">High-load threshold updated.</div>
    </c:if>
    <c:if test="${param.error == 'invalid-threshold'}">
        <div class="alert alert-warning" role="alert">Enter a whole number between 1 and 168 hours per week.</div>
    </c:if>
    <c:if test="${param.error == 'save-failed'}">
        <div class="alert alert-warning" role="alert">Could not save threshold. Please try again.</div>
    </c:if>

    <div class="admin-panel" style="margin-bottom: 1.25rem;">
        <h2>High-load threshold</h2>
        <p class="text-muted">TAs whose estimated weekly hours from selected jobs exceed this value are marked as high load.</p>
        <form method="post" action="${pageContext.request.contextPath}/admin/ta-workload" class="admin-toolbar" style="margin-top: 0.75rem;">
            <label for="weeklyHoursThreshold" class="text-muted">Hours / week</label>
            <input type="number" id="weeklyHoursThreshold" name="weeklyHoursThreshold" min="1" max="168" step="1"
                   value="${weeklyHoursThreshold}" class="form-control" style="max-width: 8rem;" required/>
            <button type="submit" class="btn btn-primary">Save threshold</button>
        </form>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-warning" role="alert"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${not empty snapshot}">
        <div class="admin-stat-cards">
            <div class="admin-stat-card">
                <span class="admin-stat-card__value">${snapshot.totalTas}</span>
                <span class="admin-stat-card__label">TAs</span>
            </div>
            <div class="admin-stat-card">
                <span class="admin-stat-card__value">${snapshot.totalSelectedAssignments}</span>
                <span class="admin-stat-card__label">Selected Assignments</span>
            </div>
            <div class="admin-stat-card" style="border-color: #fde68a;">
                <span class="admin-stat-card__value" style="color: var(--color-warning);">${snapshot.tasOverWeeklyThreshold}</span>
                <span class="admin-stat-card__label">&gt; ${snapshot.weeklyHoursThreshold} h / week</span>
            </div>
            <div class="admin-stat-card" style="border-color: #fecaca;">
                <span class="admin-stat-card__value" style="color: var(--color-danger);">${snapshot.tasWithScheduleConflicts}</span>
                <span class="admin-stat-card__label">Schedule Conflicts</span>
            </div>
        </div>

        <c:if test="${not empty focusRow}">
            <div class="admin-panel" style="margin-bottom: 1.25rem;">
                <h2><c:out value="${focusRow.displayName}"/> — detail</h2>
                <p class="text-muted">Username: <c:out value="${focusRow.username}"/> · User ID: <c:out value="${focusRow.userId}"/></p>
                <p>
                    <strong>Major:</strong> <c:out value="${empty focusRow.major ? '—' : focusRow.major}"/>
                    · <strong>Email:</strong> <c:out value="${empty focusRow.email ? '—' : focusRow.email}"/>
                    · <strong>Skills:</strong> ${focusRow.skillCount}
                    · <strong>CV:</strong> ${focusRow.hasCv ? 'Yes' : 'No'}
                </p>
                <c:if test="${not empty focusRow.selectedJobs}">
                    <h3 style="font-size: 1rem; margin-top: 1rem;">Selected jobs</h3>
                    <ul>
                        <c:forEach var="job" items="${focusRow.selectedJobs}">
                            <li>
                                <c:out value="${job.title}"/> (<c:out value="${job.jobId}"/>)
                                — <c:out value="${job.schedule}"/> · ${job.weeklyHours} h/wk · <c:out value="${job.semester}"/>
                            </li>
                        </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${empty focusRow.selectedJobs}">
                    <p class="text-muted">No selected jobs yet.</p>
                </c:if>
                <p style="margin-top: 0.75rem;">
                    <a href="${pageContext.request.contextPath}/admin/ta-workload">← Back to full list</a>
                </p>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty snapshot.rows}">
                <div class="card card--flush">
                    <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>TA</th>
                                <th>Major</th>
                                <th>Selected</th>
                                <th>Pending</th>
                                <th>Rejected</th>
                                <th>Est. hours / wk</th>
                                <th>Selected positions</th>
                                <th>Flags</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${snapshot.rows}">
                                <tr>
                                    <td>
                                        <strong><c:out value="${row.displayName}"/></strong><br/>
                                        <small class="text-muted"><c:out value="${row.username}"/></small>
                                    </td>
                                    <td><c:out value="${empty row.major ? '—' : row.major}"/></td>
                                    <td>${row.selectedCount}</td>
                                    <td>${row.pendingCount}</td>
                                    <td>${row.rejectedCount}</td>
                                    <td>
                                        <strong>${row.weeklyHours}</strong>
                                        <c:if test="${row.highLoad}">
                                            <br/><small class="warn">High load</small>
                                        </c:if>
                                    </td>
                                    <td style="max-width: 18rem;">
                                        <c:choose>
                                            <c:when test="${not empty row.selectedJobs}">
                                                <c:forEach var="job" items="${row.selectedJobs}">
                                                    <span class="skill-badge skill-match" title="${job.schedule}"><c:out value="${job.title}"/></span>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:set var="hasFlag" value="false"/>
                                        <c:if test="${row.highLoad}">
                                            <span class="badge under-review">High load</span>
                                            <c:set var="hasFlag" value="true"/>
                                        </c:if>
                                        <c:if test="${row.banned}">
                                            <span class="badge rejected">Banned</span>
                                            <c:set var="hasFlag" value="true"/>
                                        </c:if>
                                        <c:if test="${row.scheduleConflict}">
                                            <span class="badge rejected">Schedule clash</span>
                                            <c:set var="hasFlag" value="true"/>
                                        </c:if>
                                        <c:if test="${!row.hasCv && row.totalApplications > 0}">
                                            <span class="badge under-review">No CV</span>
                                            <c:set var="hasFlag" value="true"/>
                                        </c:if>
                                        <c:if test="${!hasFlag}">
                                            <span class="text-muted">—</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/admin/ta-workload?userId=${row.userId}">Detail</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <p class="text-muted">No TA accounts found.</p>
            </c:otherwise>
        </c:choose>
    </c:if>
</main>
</body>
</html>
