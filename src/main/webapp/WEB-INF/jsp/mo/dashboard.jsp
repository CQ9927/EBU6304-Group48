<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>MO Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main">
    <header class="page-header">
        <h1 class="page-title">MO dashboard</h1>
        <p class="lead">Welcome, <strong>${username}</strong>. Summary of jobs you posted and applications awaiting action.</p>
    </header>

    <c:if test="${param.saved == '1'}">
        <div class="alert alert-success" role="status">Job has been saved.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error" role="alert">${error}</div>
    </c:if>

    <div class="stats-grid stats-grid--narrow">
        <div class="card"><div class="label">My job posts</div><div class="value">${myJobsTotal}</div></div>
        <div class="card"><div class="label">My open jobs</div><div class="value">${myOpenJobs}</div></div>
        <div class="card"><div class="label">Applications to review</div><div class="value">${pendingApplications}</div></div>
    </div>

    <div class="card dashboard-actions">
        <p class="card__label">Actions</p>
        <div class="action-row">
            <button type="button" class="btn btn-primary" id="open-post-job-modal" aria-haspopup="dialog">Post a new job</button>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/mo/jobs/select">Review applications</a>
        </div>
    </div>

    <p class="hint">Your job posts (newest first):</p>
    <div class="card card--flush">
        <div class="table-scroll">
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Semester</th>
                    <th>Capacity</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="job" items="${jobs}">
                    <tr>
                        <td>${job.jobId}</td>
                        <td>${job.title}</td>
                        <td>${job.type}</td>
                        <td>${job.semester}</td>
                        <td>${job.capacity}</td>
                        <td>${job.status}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty jobs}">
                    <tr><td colspan="6" class="text-muted">No jobs posted yet.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
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

    var shouldOpen = ${not empty error or param.openPost == '1'};
    if (shouldOpen) openModal();
})();
</script>
</body>
</html>
