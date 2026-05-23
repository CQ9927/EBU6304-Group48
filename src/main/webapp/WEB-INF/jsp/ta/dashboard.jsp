<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>TA Hub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">Welcome back, ${username}</h1>
        <div class="inline-stats">
            <span class="inline-stat"><strong>${openJobsCount}</strong> Open</span>
            <span class="inline-stat-sep">&middot;</span>
            <span class="inline-stat"><strong>${myApplicationsTotal}</strong> Applied</span>
            <c:if test="${myApplicationsUnderReview > 0}">
                <span class="inline-stat-sep">&middot;</span>
                <span class="inline-stat"><strong>${myApplicationsUnderReview}</strong> In Review</span>
            </c:if>
        </div>
    </header>

    <%-- Profile completeness card (only shown if < 100%) --%>
    <c:if test="${profileCompleteness < 100}">
    <div class="profile-completeness-card">
        <div class="profile-completeness-card__left">
            <span class="profile-completeness-card__label">Your Profile</span>
            <span class="profile-completeness-card__pct">${profileCompleteness}% complete</span>
            <c:if test="${empty profile}">
                <span class="profile-completeness-card__hint">Complete your profile to get personalised match scores</span>
            </c:if>
        </div>
        <div class="profile-completeness-card__right">
            <c:choose>
                <c:when test="${empty profile}">
                    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/ta/profile">Complete Profile &rarr;</a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/ta/profile">Edit Profile</a>
                    <c:choose>
                        <c:when test="${not empty profile.cvFileName}">
                            <span class="cv-status cv-status--ok">CV &#x2713;</span>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/ta/cv">Upload CV</a>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    </c:if>

    <%-- Recent applications (hidden if none) --%>
    <c:if test="${not empty recentApplications}">
    <section class="section">
        <div class="section__header">
            <h2 class="section__title">Latest Applications</h2>
            <a class="section__action" href="${pageContext.request.contextPath}/ta/status">View All &rarr;</a>
        </div>
        <div class="app-list">
            <c:forEach var="app" items="${recentApplications}">
                <c:set var="appJob" value="${jobMap[app.jobId]}"/>
                <div class="app-list-item">
                    <div class="app-list-item__info">
                        <span class="app-list-item__title">${not empty appJob ? appJob.title : app.jobId}</span>
                        <span class="badge ${fn:toLowerCase(app.status)}">${app.status}</span>
                        <c:if test="${not empty matchMap[app.jobId]}">
                            <span class="match-pill ${matchMap[app.jobId].totalScore >= 80 ? 'match-high' : matchMap[app.jobId].totalScore >= 50 ? 'match-medium' : 'match-low'}">${matchMap[app.jobId].totalScore}% match</span>
                        </c:if>
                    </div>
                    <span class="app-list-item__date">${fn:substring(app.createdAt, 0, 10)} ${fn:substring(app.createdAt, 11, 16)}</span>
                </div>
            </c:forEach>
        </div>
    </section>
    </c:if>

    <%-- Featured positions --%>
    <section class="section">
        <div class="section__header">
            <h2 class="section__title">Featured Positions</h2>
            <a class="section__action" href="${pageContext.request.contextPath}/ta/jobs">Browse All &nearr;</a>
        </div>

        <c:choose>
            <c:when test="${not empty featuredJobs}">
                <div class="job-cards">
                    <c:forEach var="job" items="${featuredJobs}">
                        <c:set var="match" value="${matchMap[job.jobId]}"/>
                        <div class="job-card">
                            <div class="job-card__header">
                                <h3 class="job-card__title">${job.title}</h3>
                                <c:if test="${not empty match}">
                                    <span class="match-pill ${match.totalScore >= 80 ? 'match-high' : match.totalScore >= 50 ? 'match-medium' : 'match-low'}">
                                        <c:choose>
                                            <c:when test="${match.totalScore >= 80}">&#x1f7e2;</c:when>
                                            <c:when test="${match.totalScore >= 50}">&#x1f7e1;</c:when>
                                            <c:otherwise>&#x1f534;</c:otherwise>
                                        </c:choose>
                                        ${match.totalScore}% match
                                    </span>
                                </c:if>
                            </div>
                            <div class="job-card__meta">
                                <span class="badge badge-${fn:toLowerCase(job.type)}">${job.type}</span>
                                <span class="job-card__schedule">${job.schedule}</span>
                                <span class="job-card__cap">Cap: ${job.capacity}</span>
                            </div>
                            <c:if test="${not empty match and not empty match.matchedSkills or not empty match.missingSkills}">
                            <div class="job-card__skills">
                                <c:forEach var="sk" items="${match.matchedSkills}">
                                    <span class="skill-pill skill-match">&#x2713; ${sk}</span>
                                </c:forEach>
                                <c:forEach var="sk" items="${match.missingSkills}">
                                    <span class="skill-pill skill-missing">&#x2717; ${sk}</span>
                                </c:forEach>
                            </div>
                            </c:if>
                            <a class="btn btn-primary btn-sm job-card__cta" href="${pageContext.request.contextPath}/ta/jobs">Apply &rarr;</a>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>No open positions right now. Check back later.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- Quick links --%>
    <div class="quick-links">
        <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/ta/status">My Status</a>
        <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/ta/profile">Edit Profile</a>
        <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/ta/cv">Upload CV</a>
    </div>
</main>
</body>
</html>
