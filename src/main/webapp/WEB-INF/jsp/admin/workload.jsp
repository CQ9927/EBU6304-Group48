<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Admin Console</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=ai3"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main" id="main-content">
    <header class="page-header">
        <h1 class="page-title">Admin Console</h1>
        <p class="lead">Hello, <strong>${username}</strong></p>
    </header>

    <%-- Toolbar --%>
    <div class="admin-toolbar">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users">Manage Users</a>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/admin/applications">Applications</a>
        <button class="btn btn-ghost" id="toggle-workload-table" type="button">Hide Details &#8593;</button>
    </div>

    <%-- Two-column grid --%>
    <div class="admin-grid">

        <%-- LEFT COLUMN --%>
        <div>

            <%-- 4 key metrics --%>
            <div class="admin-stat-cards">
                <div class="admin-stat-card">
                    <span class="admin-stat-card__value">${totalUsers}</span>
                    <span class="admin-stat-card__label">Users</span>
                </div>
                <div class="admin-stat-card">
                    <span class="admin-stat-card__value">${snapshot.totalJobs}</span>
                    <span class="admin-stat-card__label">Jobs</span>
                </div>
                <div class="admin-stat-card">
                    <span class="admin-stat-card__value">${snapshot.totalApplications}</span>
                    <span class="admin-stat-card__label">Applications</span>
                </div>
                <div class="admin-stat-card" style="border-color: #fecaca;">
                    <span class="admin-stat-card__value" style="color: var(--color-danger);">${alertCount}</span>
                    <span class="admin-stat-card__label">Alerts</span>
                </div>
            </div>

            <%-- Alerts & Warnings --%>
            <c:set var="hasAlerts" value="false"/>
            <c:if test="${not empty snapshot.rows}">
                <c:forEach var="row" items="${snapshot.rows}">
                    <c:if test="${row.overCapacity}">
                        <c:set var="hasAlerts" value="true"/>
                    </c:if>
                </c:forEach>
            </c:if>
            <c:if test="${snapshot.openJobsWithoutSelection > 0}">
                <c:set var="hasAlerts" value="true"/>
            </c:if>
            <c:if test="${not empty snapshot.hints}">
                <c:set var="hasAlerts" value="true"/>
            </c:if>

            <c:if test="${hasAlerts}">
            <div class="admin-panel" style="margin-bottom: 1.25rem;">
                <h2>&#x26a0; Alerts &amp; Warnings</h2>
                <div class="alert-cards">
                    <c:forEach var="row" items="${snapshot.rows}">
                        <c:if test="${row.overCapacity}">
                            <div class="alert-card alert-card--danger">
                                <span class="alert-card__icon">&#x1f534;</span>
                                <div class="alert-card__body">
                                    <strong>Over Capacity</strong>
                                    <p>${row.jobId} (${row.title}): ${row.selectedCount} selected / cap ${row.capacity}</p>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                    <c:if test="${snapshot.openJobsWithoutSelection > 0}">
                        <div class="alert-card alert-card--warning">
                            <span class="alert-card__icon">&#x1f7e1;</span>
                            <div class="alert-card__body">
                                <strong>Open Jobs Without Selection</strong>
                                <p>${snapshot.openJobsWithoutSelection} job<c:if test="${snapshot.openJobsWithoutSelection != 1}">s</c:if> have no selected TA yet</p>
                            </div>
                        </div>
                    </c:if>
                    <c:forEach var="hint" items="${snapshot.hints}">
                        <div class="alert-card alert-card--warning">
                            <span class="alert-card__icon">&#x26a0;</span>
                            <div class="alert-card__body">
                                <p>${hint}</p>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
            </c:if>

            <%-- Per-job workload table --%>
            <div class="admin-panel" id="workload-table-panel">
                <h2>Per-job Workload</h2>
                <c:choose>
                    <c:when test="${not empty snapshot.rows}">
                        <div class="table-scroll">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Job</th>
                                <th>Status</th>
                                <th>Capacity</th>
                                <th>Apps</th>
                                <th>Sub</th>
                                <th>Review</th>
                                <th>Sel</th>
                                <th>Rej</th>
                                <th>Rem</th>
                                <th>Risk</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${snapshot.rows}">
                                <tr>
                                    <td><span class="mono">${row.jobId}</span><br/><small>${row.title}</small></td>
                                    <td><span class="badge badge-${row.status == 'OPEN' ? 'open' : 'secondary'}">${row.status}</span></td>
                                    <td>${row.capacity}</td>
                                    <td>${row.totalApplications}</td>
                                    <td>${row.submittedCount}</td>
                                    <td>${row.underReviewCount}</td>
                                    <td>${row.selectedCount}</td>
                                    <td>${row.rejectedCount}</td>
                                    <td>${row.remainingSlots}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${row.overCapacity}">
                                                <span class="badge rejected">OVER</span>
                                            </c:when>
                                            <c:otherwise>&mdash;</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted">No job data yet. Ask MO to create at least one job.</p>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>

        <%-- RIGHT COLUMN --%>
        <div>

            <%-- AI Insights --%>
            <div class="admin-panel">
                <h2>&#x1f50d; AI Workload Insights</h2>
                <button id="btn-ai-insights" class="btn-ai-analyze" style="margin-bottom:0.8rem;"
                        onclick="loadWorkloadInsights()">✨ Analyze with AI</button>
                <div class="ai-analysis-result" id="ai-insights-result" style="display:none;"></div>
            </div>

        </div>

    </div>

</main>

<script>
var toggleBtn = document.getElementById('toggle-workload-table');
var tablePanel = document.getElementById('workload-table-panel');
if (toggleBtn && tablePanel) {
    toggleBtn.addEventListener('click', function () {
        var hidden = tablePanel.style.display === 'none';
        tablePanel.style.display = hidden ? 'block' : 'none';
        toggleBtn.innerHTML = hidden ? 'Hide Details &#8593;' : 'Workload Details &#8595;';
    });
}

function renderWorkloadText(text) {
    if (!text) return '';
    var lines = text.split('\n');
    var html = '';
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i].trim();
        if (!line) continue;
        var emojiMatch = line.match(/^([\u{1F534}\u{1F7E1}\u{1F7E2}\u{1F4A1}])\s*(.*)/u);
        if (emojiMatch) {
            var emoji = emojiMatch[1];
            var rest = emojiMatch[2];
            var cls = 'ai-insight';
            if (emoji === '\u{1F534}') cls += ' ai-insight-critical';
            else if (emoji === '\u{1F7E1}') cls += ' ai-insight-warning';
            else if (emoji === '\u{1F7E2}') cls += ' ai-insight-good';
            else if (emoji === '\u{1F4A1}') cls += ' ai-insight-tip';
            html += '<div class="' + cls + '">' + emoji + ' ' + rest + '</div>';
        } else {
            html += '<p>' + line + '</p>';
        }
    }
    return html;
}

function loadWorkloadInsights() {
    var btn = document.getElementById('btn-ai-insights');
    var resultDiv = document.getElementById('ai-insights-result');
    if (!btn || !resultDiv) return;

    btn.disabled = true;
    btn.classList.add('is-loading');
    btn.textContent = ' Analyzing...';
    resultDiv.style.display = 'block';
    resultDiv.innerHTML = '<p><span class="spin-emoji">&#9881;</span> Analyzing workload data...</p>';

    fetch('${pageContext.request.contextPath}/admin/workload/ai-insights')
        .then(function(resp) {
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            return resp.json();
        })
        .then(function(data) {
            if (data.insights) {
                resultDiv.innerHTML = renderWorkloadText(data.insights);
                btn.style.display = 'none';
            } else {
                resultDiv.innerHTML = '<p>' + (data.error || 'AI insights unavailable.') + '</p>';
                btn.textContent = '✨ Analyze with AI';
                btn.classList.remove('is-loading');
                btn.disabled = false;
            }
        })
        .catch(function(err) {
            resultDiv.innerHTML = '<p>AI analysis is currently unavailable. Please try again later.</p>';
            btn.textContent = '✨ Analyze with AI';
            btn.classList.remove('is-loading');
            btn.disabled = false;
        });
}
</script>
</body>
</html>
