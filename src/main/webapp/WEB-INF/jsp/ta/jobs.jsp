<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>Available Jobs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=ai2"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main container-grid">
    <h1>Available TA Jobs</h1>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-success">
            ${sessionScope.message}
            <c:remove var="message" scope="session"/>
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-error">
            ${sessionScope.error}
            <c:remove var="error" scope="session"/>
        </div>
    </c:if>

    <c:if test="${not empty userProfile}">
        <div class="stats">
            <div class="stat-item">
                <div class="stat-value">${not empty userProfile.skills ? userProfile.skills.size() : 0}</div>
                <div class="stat-label">Skills Listed</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${not empty userProfile.availability ? userProfile.availability.size() : 0}</div>
                <div class="stat-label">Time Slots</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">${appliedJobIds.size()}</div>
                <div class="stat-label">Applications</div>
            </div>
        </div>
    </c:if>

    <c:if test="${empty userProfile}">
        <div class="profile-warning">
            <strong>Profile incomplete.</strong> Please complete your <a href="${pageContext.request.contextPath}/ta/profile">profile</a> before applying for jobs.
            Your skills and availability will be used to match you with suitable positions.
        </div>
    </c:if>

    <c:if test="${empty userProfile.cvFileName}">
        <div class="profile-warning">
            <strong>CV missing.</strong> Please upload your <a href="${pageContext.request.contextPath}/ta/cv">CV</a> before applying for jobs.
            Most applications require a current CV.
        </div>
    </c:if>

    <div class="card">
        <h2>Job Search Filters</h2>
        <form method="get" action="${pageContext.request.contextPath}/ta/jobs" class="filter-form">
            <div class="filter-group">
                <label for="type">Job Type</label>
                <select id="type" name="type" class="form-control">
                    <option value="">All Types</option>
                    <option value="MODULE" ${typeFilter eq 'MODULE' ? 'selected' : ''}>Module Assistant</option>
                    <option value="INVIGILATION" ${typeFilter eq 'INVIGILATION' ? 'selected' : ''}>Invigilation</option>
                </select>
            </div>
            
            <div class="filter-group">
                <label for="semester">Semester</label>
                <select id="semester" name="semester" class="form-control">
                    <option value="">All Semesters</option>
                    <option value="2026_SPRING" ${semesterFilter eq '2026_SPRING' ? 'selected' : ''}>2026 Spring</option>
                    <option value="2025_FALL" ${semesterFilter eq '2025_FALL' ? 'selected' : ''}>2025 Fall</option>
                    <option value="2025_SPRING" ${semesterFilter eq '2025_SPRING' ? 'selected' : ''}>2025 Spring</option>
                </select>
            </div>
            
            <div class="filter-group">
                <label for="skill">Required Skill</label>
                <input type="text" id="skill" name="skill" class="form-control" 
                       placeholder="e.g., Java, Teaching" value="${skillFilter}">
            </div>
            
            <div class="filter-actions">
                <button type="submit" class="btn btn-primary">Apply Filters</button>
                <a href="${pageContext.request.contextPath}/ta/jobs" class="btn btn-secondary">Clear Filters</a>
            </div>
        </form>
        
        <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
            <div class="alert alert-info">
                <strong>Active filters:</strong>
                <c:if test="${not empty typeFilter}">Type: <c:out value="${typeFilter}"/></c:if>
                <c:if test="${not empty semesterFilter}"> | Semester: <c:out value="${semesterFilter}"/></c:if>
                <c:if test="${not empty skillFilter}"> | Skill: <c:out value="${skillFilter}"/></c:if>
            </div>
        </c:if>
    </div>

    <div class="card">
        <h2>Available Positions (${jobs.size()})</h2>
        
        <c:choose>
            <c:when test="${not empty jobs}">
                <div class="table-scroll">
                <table class="job-table">
                    <thead>
                        <tr>
                            <th>Job Title</th>
                            <th>Type</th>
                            <th>Schedule</th>
                            <th>Skills Required</th>
                            <th>Match</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="job" items="${jobs}">
                            <c:set var="isApplied" value="${appliedJobIds.contains(job.jobId)}" />
                            <c:set var="mr" value="${matchResultMap[job.jobId]}" />
                            
                            <tr>
                                <td>
                                    <strong>${job.title}</strong><br>
                                    <small class="text-muted">${job.semester} • Capacity: ${job.capacity}</small>
                                </td>
                                <td>
                                    <span class="badge badge-${job.type.toLowerCase()}">
                                        ${job.type}
                                    </span>
                                </td>
                                <td>${job.schedule}</td>
                                <td>
                                    <div class="skills-list">
                                        <c:forEach var="skill" items="${job.requiredSkills}">
                                            <c:choose>
                                                <c:when test="${not empty mr and mr.matchedSkills.contains(skill)}">
                                                    <span class="skill-badge skill-match" title="You have this skill">${skill} ✓</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="skill-badge skill-missing" title="Missing skill">${skill} ✗</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty mr}">
                                            <span class="match-score
                                                <c:choose>
                                                    <c:when test="${mr.totalScore >= 80}">match-high</c:when>
                                                    <c:when test="${mr.totalScore >= 50}">match-medium</c:when>
                                                    <c:otherwise>match-low</c:otherwise>
                                                </c:choose>">
                                                ${mr.totalScore}%
                                            </span>
                                            <br><small class="text-muted">
                                                S:${mr.skillScore}/50 T:${mr.scheduleScore}/25
                                                M:${mr.majorScore}/15 P:${mr.completenessScore}/10
                                            </small>
                                            <c:if test="${not empty userProfile}">
                                                <br><button class="btn-ai-analyze" data-job-id="${job.jobId}"
                                                        onclick="loadAiAnalysis('${job.jobId}', this)">AI Analysis</button>
                                                <button class="btn-ai-analyze" data-job-id="${job.jobId}"
                                                        onclick="loadSkillGap('${job.jobId}', this)" style="margin-left:4px;">Skill Gap</button>
                                                <div class="ai-analysis-result" id="ai-${job.jobId}" style="display:none;"></div>
                                                <div class="ai-analysis-result" id="sg-${job.jobId}" style="display:none;"></div>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${isApplied}">
                                            <span class="badge badge-secondary">Applied</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-open">OPEN</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${isApplied}">
                                            <button class="btn btn-secondary btn-apply" disabled>Applied</button>
                                        </c:when>
                                        <c:when test="${empty userProfile or empty userProfile.cvFileName}">
                                            <button type="button" class="btn btn-secondary btn-apply btn-disabled" disabled
                                                    title="Complete profile and upload CV first">Apply</button>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="<c:url value='/ta/apply'><c:param name='jobId' value='${job.jobId}'/></c:url>"
                                               class="btn btn-success btn-apply">Apply</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                </div>

                <div class="pagination">
                    <div class="pagination-info">
                        Showing ${jobs.size()} job<c:if test="${jobs.size() != 1}">s</c:if>
                        <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
                            (filtered)
                        </c:if>
                    </div>
                    <div>
                        <c:if test="${jobs.size() > 0}">
                            <small>Last updated: ${fn:substring(jobs[0].createdAt, 0, 10)} ${fn:substring(jobs[0].createdAt, 11, 16)}</small>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-jobs">
                    <h3>No jobs found</h3>
                    <p>There are currently no open positions matching your criteria.</p>
                    <c:if test="${not empty typeFilter or not empty semesterFilter or not empty skillFilter}">
                        <p>Try clearing your filters or check back later.</p>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <h2>Application Tips</h2>
        <ul>
            <li><strong>Match your skills:</strong> Highlight skills that match the job requirements in your profile</li>
            <li><strong>Check availability:</strong> Ensure your availability matches the job schedule</li>
            <li><strong>Update CV:</strong> Make sure your CV is current and highlights relevant experience</li>
            <li><strong>Apply early:</strong> Popular positions fill up quickly</li>
            <li><strong>Track applications:</strong> Check your application status regularly</li>
        </ul>
        <p style="margin-top: 1rem; margin-bottom: 0;">
            <a href="${pageContext.request.contextPath}/ta/status" class="btn btn-secondary">
                View Application Status
            </a>
        </p>
    </div>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Add tooltips for match scores
    const matchElements = document.querySelectorAll('.match-score');
    matchElements.forEach(el => {
        const score = parseInt(el.textContent);
        let tooltip = '';
        if (score >= 80) {
            tooltip = 'Excellent match! Strongly recommended to apply.';
        } else if (score >= 50) {
            tooltip = 'Good match. Consider applying if interested.';
        } else if (score > 0) {
            tooltip = 'Partial match. You might want to improve your skills first.';
        } else {
            tooltip = 'No skills match. Consider other positions or update your skills.';
        }
        el.title = tooltip;
    });
});
</script>

<script>
function renderAiText(text) {
    if (!text) return '';
    // Bold: **text** → <strong class="ai-hl">text</strong>
    var html = text.replace(/\*\*(.+?)\*\*/g, '<strong class="ai-hl">$1</strong>');

    var lines = html.split('\n');
    var result = '';
    var bulletGroup = null;
    var i = 0;

    while (i < lines.length) {
        var line = lines[i].trim();
        if (!line) { i++; continue; }

        var bulletMatch = line.match(/^[•\-]\s+(.*)/);
        var numMatch = line.match(/^(\d+)[\.\)]\s+(.*)/);
        var emojiMatch = line.match(/^([\u{1F534}\u{1F7E1}\u{1F7E2}\u{1F4A1}\u2705\u26A0\uFE0F\u274C])\s*(.*)/u);

        function classifyLine(text) {
            var lower = text.toLowerCase();
            if (/verdict|overall|recommend/i.test(lower)) return 'ai-insight-tip';
            if (/missing|gap|lack|no\s|doesn|poor|weak/i.test(lower)) return 'ai-insight-critical';
            if (/partial|some|moderate|could|might/i.test(lower)) return 'ai-insight-warning';
            if (/good|strong|excellent|well|match/i.test(lower)) return 'ai-insight-good';
            return '';
        }

        if (emojiMatch) {
            var emoji = emojiMatch[1];
            var rest = emojiMatch[2];
            var cls = 'ai-insight';
            if (/[\u{1F534}\u274C]/.test(emoji)) cls += ' ai-insight-critical';
            else if (/[\u{1F7E1}\u26A0]/.test(emoji)) cls += ' ai-insight-warning';
            else if (/[\u{1F7E2}\u2705]/.test(emoji)) cls += ' ai-insight-good';
            else if (/\u{1F4A1}/.test(emoji)) cls += ' ai-insight-tip';
            if (bulletGroup) { result += '</' + bulletGroup + '>'; bulletGroup = null; }
            result += '<div class="' + cls + '">' + emoji + ' ' + rest + '</div>';
        } else if (bulletMatch) {
            if (bulletGroup !== 'ul') {
                if (bulletGroup) result += '</' + bulletGroup + '>';
                result += '<ul>';
                bulletGroup = 'ul';
            }
            var extraCls = classifyLine(bulletMatch[1]);
            result += '<li' + (extraCls ? ' class="' + extraCls + '"' : '') + '>' + bulletMatch[1] + '</li>';
        } else if (numMatch) {
            if (bulletGroup !== 'ol') {
                if (bulletGroup) result += '</' + bulletGroup + '>';
                result += '<ol>';
                bulletGroup = 'ol';
            }
            result += '<li>' + numMatch[2] + '</li>';
        } else {
            if (bulletGroup) { result += '</' + bulletGroup + '>'; bulletGroup = null; }
            var cls2 = classifyLine(line);
            result += '<div class="' + (cls2 || 'ai-insight') + '">' + line + '</div>';
        }
        i++;
    }
    if (bulletGroup) result += '</' + bulletGroup + '>';
    return result;
}

function loadAiAnalysis(jobId, btn) {
    var resultDiv = document.getElementById('ai-' + jobId);
    if (!resultDiv) return;

    btn.disabled = true;
    btn.textContent = 'Analyzing...';
    resultDiv.style.display = 'block';
    resultDiv.innerHTML = '<p>Analyzing...</p>';

    var ctxPath = '${pageContext.request.contextPath}';
    fetch(ctxPath + '/ta/jobs/ai-analysis?jobId=' + encodeURIComponent(jobId))
        .then(function(resp) {
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            return resp.json();
        })
        .then(function(data) {
            if (data.explanation) {
                resultDiv.innerHTML = renderAiText(data.explanation);
                btn.style.display = 'none';
            } else {
                resultDiv.innerHTML = '<p>' + (data.error || 'AI analysis unavailable.') + '</p>';
                btn.textContent = 'AI Analysis';
                btn.disabled = false;
            }
        })
        .catch(function(err) {
            resultDiv.innerHTML = '<p>AI analysis is currently unavailable. Please try again later.</p>';
            btn.textContent = 'AI Analysis';
            btn.disabled = false;
        });
}

function loadSkillGap(jobId, btn) {
    var resultDiv = document.getElementById('sg-' + jobId);
    if (!resultDiv) return;

    btn.disabled = true;
    btn.textContent = 'Analyzing...';
    resultDiv.style.display = 'block';
    resultDiv.innerHTML = '<p>Analyzing skill gaps...</p>';

    var ctxPath = '${pageContext.request.contextPath}';
    fetch(ctxPath + '/ta/jobs/ai-analysis?type=skills&jobId=' + encodeURIComponent(jobId))
        .then(function(resp) {
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            return resp.json();
        })
        .then(function(data) {
            if (data.explanation) {
                resultDiv.innerHTML = renderAiText(data.explanation);
                btn.style.display = 'none';
            } else {
                resultDiv.innerHTML = '<p>' + (data.error || 'Skill gap analysis unavailable.') + '</p>';
                btn.textContent = 'Skill Gap';
                btn.disabled = false;
            }
        })
        .catch(function(err) {
            resultDiv.innerHTML = '<p>Skill gap analysis is currently unavailable. Please try again later.</p>';
            btn.textContent = 'Skill Gap';
            btn.disabled = false;
        });
}
</script>
</body>
</html>