<%@ page import="com.ebu6304.group48.util.SemesterFormat" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>My Positions</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">My Positions</h1>
        <p class="lead">Hello, <strong>${username}</strong></p>
    </header>

    <c:if test="${param.saved == '1'}">
        <div class="alert alert-success" role="status">Job has been saved.</div>
    </c:if>
    <c:if test="${param.closed == '1'}">
        <div class="alert alert-success" role="status">Job has been closed.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error" role="alert">${error}</div>
    </c:if>

    <%-- Compact stats --%>
    <div class="compact-stats">
        <div class="compact-stat">
            <span class="compact-stat__value">${myJobsTotal}</span>
            <span class="compact-stat__label">Posts</span>
        </div>
        <div class="compact-stat">
            <span class="compact-stat__value">${myOpenJobs}</span>
            <span class="compact-stat__label">Open</span>
        </div>
        <div class="compact-stat">
            <span class="compact-stat__value">${pendingApplications}</span>
            <span class="compact-stat__label">Pending</span>
        </div>
    </div>

    <%-- Primary CTAs --%>
    <div class="mo-ctas">
        <button type="button" class="btn btn-primary" id="open-post-job-modal" aria-haspopup="dialog">+ Post New Job</button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/mo/jobs/select">Review Applications &rarr;</a>
    </div>

    <%-- Job cards --%>
    <c:choose>
        <c:when test="${not empty jobs}">
            <h2 class="section__title" style="margin-top: 1.5rem; margin-bottom: 0.75rem;">Your Positions</h2>
            <div class="job-cards">
                <c:forEach var="job" items="${jobs}">
                    <c:set var="jid" value="${job.jobId}"/>
                    <c:set var="appTotal" value="${totalByJob[jid] != null ? totalByJob[jid] : 0}"/>
                    <c:set var="appSub" value="${submittedByJob[jid] != null ? submittedByJob[jid] : 0}"/>
                    <c:set var="appRev" value="${underReviewByJob[jid] != null ? underReviewByJob[jid] : 0}"/>
                    <c:set var="appSel" value="${selectedByJob[jid] != null ? selectedByJob[jid] : 0}"/>
                    <c:set var="appRej" value="${rejectedByJob[jid] != null ? rejectedByJob[jid] : 0}"/>
                    <div class="job-card">
                        <div class="job-card__header">
                            <div>
                                <span class="job-card__id mono">${job.jobId}</span>
                                <h3 class="job-card__title">${job.title}</h3>
                            </div>
                            <span class="badge badge-${fn:toLowerCase(job.status)}">${job.status}</span>
                        </div>
                        <div class="job-card__meta">
                            <span class="badge badge-${fn:toLowerCase(job.type)}">${job.type}</span>
                            <span><%= SemesterFormat.label(((com.ebu6304.group48.model.Job)pageContext.getAttribute("job")).getSemester()) %></span>
                            <span>Cap: ${job.capacity}</span>
                            <c:if test="${not empty job.deadline}">
                                <span class="deadline-badge">Deadline: ${job.deadline}</span>
                            </c:if>
                        </div>
                        <div class="job-card__applicants">
                            <c:choose>
                                <c:when test="${appTotal > 0}">
                                    <div>&#x1f4ca; ${appTotal} applicant<c:if test="${appTotal != 1}">s</c:if>
                                    <c:if test="${appSub > 0}"><span class="applicant-detail">(${appSub} submitted<c:if test="${appRev > 0}">, ${appRev} under review</c:if>)</span></c:if>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div>&#x1f4ca; No applicants yet</div>
                                </c:otherwise>
                            </c:choose>
                            <%-- Show selected TAs --%>
                            <c:set var="selList" value="${selectedProfilesByJob[jid]}"/>
                            <c:if test="${not empty selList}">
                                <div class="selected-tas">
                                    <span class="selected-tas__label">&#x2705; Hired (${selList.size()}/${job.capacity}):</span>
                                    <span class="selected-tas__names"><c:forEach var="p" items="${selList}" varStatus="st">${p.name}<c:if test="${not st.last}">, </c:if></c:forEach></span>
                                </div>
                            </c:if>
                        </div>
                        <div class="job-card__actions">
                            <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/mo/jobs/select">Review Applicants &rarr;</a>
                            <c:if test="${job.status == 'OPEN'}">
                                <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/mo/jobs/invite?jobId=${job.jobId}">Invite TAs</a>
                                <form method="post" action="${pageContext.request.contextPath}/mo/dashboard" style="display:inline" onsubmit="return confirm('Close this job? No more applications will be accepted.');">
                                    <input type="hidden" name="action" value="close"/>
                                    <input type="hidden" name="jobId" value="${job.jobId}"/>
                                    <button type="submit" class="btn btn-ghost btn-sm" style="color:#d32f2f;">Close Job</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <%-- Empty state --%>
            <div class="empty-state-card">
                <h3>No jobs yet?</h3>
                <p>Click <strong>"Post New Job"</strong> above to create your first position. Then applicants can apply and you can review them here.</p>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<div class="modal" id="post-job-modal" role="dialog" aria-modal="true" aria-labelledby="post-job-modal-title" hidden>
    <div class="modal__backdrop" data-modal-close tabindex="-1"></div>
    <div class="modal__panel">
        <header class="modal__header">
            <h2 class="modal__title" id="post-job-modal-title">Post new job</h2>
            <button type="button" class="modal__close" data-modal-close aria-label="Close">&times;</button>
        </header>
        <div class="modal__body">
            <jsp:include page="/WEB-INF/jsp/mo/_post-job-form.jsp"/>
        </div>
    </div>
</div>

<script>
(function () {
    var modal = document.getElementById('post-job-modal');
    var openBtn = document.getElementById('open-post-job-modal');
    if (!modal || !openBtn) return;

    function openModal() {
        modal.hidden = false;
        document.body.classList.add('modal-open');
        var first = modal.querySelector('input, select, textarea, button');
        if (first) first.focus();
    }

    function closeModal() {
        modal.hidden = true;
        document.body.classList.remove('modal-open');
        openBtn.focus();
    }

    openBtn.addEventListener('click', openModal);

    modal.querySelectorAll('[data-modal-close]').forEach(function (el) {
        el.addEventListener('click', closeModal);
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && !modal.hidden) closeModal();
    });

    <c:set var="shouldOpenJs" value="${not empty error or param.openPost == '1'}"/>
    var shouldOpen = ${shouldOpenJs};
    if (!shouldOpen && window.location.hash === '#post-job-modal') {
        shouldOpen = true;
    }
    if (shouldOpen) openModal();
})();
</script>
</body>
</html>
