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

    <div class="stats-grid stats-grid--narrow">
        <div class="card"><div class="label">My job posts</div><div class="value">${myJobsTotal}</div></div>
        <div class="card"><div class="label">My open jobs</div><div class="value">${myOpenJobs}</div></div>
        <div class="card"><div class="label">Applications to review</div><div class="value">${pendingApplications}</div></div>
    </div>

    <div class="card dashboard-actions">
        <p class="card__label">Actions</p>
        <div class="action-row">
            <button type="button" class="btn btn-primary" id="openPostJobModal">Post a new job</button>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/mo/jobs/select">Review applications</a>
        </div>
    </div>
</main>

<dialog id="postJobModal" class="modal" aria-labelledby="postJobModalTitle">
    <form method="post" action="${pageContext.request.contextPath}/mo/jobs/new" class="modal__panel form-stack">
        <header class="modal__header">
            <h2 id="postJobModalTitle" class="modal__title">Post new job</h2>
            <button type="button" class="modal__close" data-modal-close aria-label="Close">&times;</button>
        </header>
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert">${error}</div>
        </c:if>
        <div class="modal__body">
            <p>
                <label for="title">Title *</label><br/>
                <input type="text" id="title" name="title" value="${title}" required/>
            </p>
            <p>
                <label for="type">Type *</label><br/>
                <select id="type" name="type" required>
                    <option value="">— choose —</option>
                    <option value="MODULE" ${type == 'MODULE' ? 'selected' : ''}>MODULE</option>
                    <option value="INVIGILATION" ${type == 'INVIGILATION' ? 'selected' : ''}>INVIGILATION</option>
                </select>
            </p>
            <p>
                <label for="semester">Semester *</label><br/>
                <input type="text" id="semester" name="semester" value="${semester}" placeholder="e.g. 2026_SPRING" required/>
            </p>
            <p>
                <label for="schedule">Schedule *</label><br/>
                <input type="text" id="schedule" name="schedule" value="${schedule}" placeholder="e.g. WED_18_20" required/>
            </p>
            <p>
                <label for="capacity">Capacity *</label><br/>
                <input type="number" id="capacity" name="capacity" min="1" value="${capacity}" required/>
            </p>
            <p>
                <label for="requiredSkills">Required skills (comma-separated)</label><br/>
                <input type="text" id="requiredSkills" name="requiredSkills" value="${requiredSkills}"
                       placeholder="Java, Teaching, Algorithms"/>
            </p>
        </div>
        <footer class="modal__footer">
            <button type="button" class="btn btn-ghost" data-modal-close>Cancel</button>
            <button type="submit" class="btn btn-primary">Create job</button>
        </footer>
    </form>
</dialog>

<script>
(function () {
    var dialog = document.getElementById('postJobModal');
    var openBtn = document.getElementById('openPostJobModal');
    if (!dialog || !openBtn) return;

    function openModal() {
        if (typeof dialog.showModal === 'function') {
            dialog.showModal();
        }
    }

    openBtn.addEventListener('click', openModal);

    dialog.querySelectorAll('[data-modal-close]').forEach(function (btn) {
        btn.addEventListener('click', function () { dialog.close(); });
    });

    dialog.addEventListener('click', function (e) {
        if (e.target === dialog) dialog.close();
    });

    <c:if test="${openPostJobModal}">
    openModal();
    </c:if>
})();
</script>
</body>
</html>
