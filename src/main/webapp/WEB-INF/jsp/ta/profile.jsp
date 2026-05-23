<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="view-transition" content="same-origin"/>
    <title>TA Profile</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/_include/app-header.jsp"/>
<main class="site-main container-grid" id="main-content">
    <div class="back-link"><a href="${pageContext.request.contextPath}/ta/dashboard">&larr; Back to Dashboard</a></div>
    <h1>TA Profile Management</h1>

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

    <div class="card">
        <h2>Personal Information</h2>
        <form method="post" action="${pageContext.request.contextPath}/ta/profile">
            <div class="form-group">
                <label for="name">Full Name *</label>
                <input type="text" id="name" name="name" class="form-control" 
                       value="${not empty profile ? profile.name : ''}" required>
            </div>
            
            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" name="email" class="form-control" 
                       value="${not empty profile ? profile.email : ''}" required>
            </div>
            
            <div class="form-group">
                <label for="major">Major/Program *</label>
                <input type="text" id="major" name="major" class="form-control" 
                       value="${not empty profile ? profile.major : ''}" required>
            </div>
            
            <div class="form-group">
                <label for="skillsInput">Skills (comma-separated)</label>
                <input type="text" id="skillsInput" name="skillsInput" class="form-control"
                       value="${not empty profile ? String.join(', ', profile.skills) : ''}"
                       placeholder="Java, Python, Machine Learning, Teaching"/>
                <c:if test="${not empty suggestedSkills}">
                <div class="suggested-skills" style="margin-top: 0.5rem;">
                    <small class="text-muted">Suggested skills from open jobs:</small>
                    <div class="chip-row" style="margin-top: 0.25rem; display: flex; flex-wrap: wrap; gap: 0.25rem;">
                        <c:forEach var="sk" items="${suggestedSkills}">
                            <button type="button" class="chip" onclick="addSkill('${sk}')" style="cursor:pointer; background:var(--color-bg-secondary); border:1px solid var(--color-border); border-radius:1rem; padding:0.2rem 0.75rem; font-size:0.85rem;">${sk}</button>
                        </c:forEach>
                    </div>
                </div>
                </c:if>
            </div>
            
            <div class="form-group">
                <label>Availability (select available time slots)</label>
                <div class="checkbox-group" style="display:grid; grid-template-columns:repeat(auto-fill, minmax(190px, 1fr)); gap:0.35rem;">
                    <div class="checkbox-item"><input type="checkbox" id="time-mon-09-12" name="availability" value="MON_09_12" ${not empty profile and profile.availability.contains('MON_09_12') ? 'checked' : ''}><label for="time-mon-09-12">Mon 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-mon-14-16" name="availability" value="MON_14_16" ${not empty profile and profile.availability.contains('MON_14_16') ? 'checked' : ''}><label for="time-mon-14-16">Mon 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-mon-18-20" name="availability" value="MON_18_20" ${not empty profile and profile.availability.contains('MON_18_20') ? 'checked' : ''}><label for="time-mon-18-20">Mon 18:00-20:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-tue-09-12" name="availability" value="TUE_09_12" ${not empty profile and profile.availability.contains('TUE_09_12') ? 'checked' : ''}><label for="time-tue-09-12">Tue 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-tue-14-16" name="availability" value="TUE_14_16" ${not empty profile and profile.availability.contains('TUE_14_16') ? 'checked' : ''}><label for="time-tue-14-16">Tue 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-tue-18-20" name="availability" value="TUE_18_20" ${not empty profile and profile.availability.contains('TUE_18_20') ? 'checked' : ''}><label for="time-tue-18-20">Tue 18:00-20:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-wed-09-12" name="availability" value="WED_09_12" ${not empty profile and profile.availability.contains('WED_09_12') ? 'checked' : ''}><label for="time-wed-09-12">Wed 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-wed-14-16" name="availability" value="WED_14_16" ${not empty profile and profile.availability.contains('WED_14_16') ? 'checked' : ''}><label for="time-wed-14-16">Wed 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-wed-18-20" name="availability" value="WED_18_20" ${not empty profile and profile.availability.contains('WED_18_20') ? 'checked' : ''}><label for="time-wed-18-20">Wed 18:00-20:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-thu-09-12" name="availability" value="THU_09_12" ${not empty profile and profile.availability.contains('THU_09_12') ? 'checked' : ''}><label for="time-thu-09-12">Thu 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-thu-14-16" name="availability" value="THU_14_16" ${not empty profile and profile.availability.contains('THU_14_16') ? 'checked' : ''}><label for="time-thu-14-16">Thu 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-thu-18-20" name="availability" value="THU_18_20" ${not empty profile and profile.availability.contains('THU_18_20') ? 'checked' : ''}><label for="time-thu-18-20">Thu 18:00-20:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-fri-09-12" name="availability" value="FRI_09_12" ${not empty profile and profile.availability.contains('FRI_09_12') ? 'checked' : ''}><label for="time-fri-09-12">Fri 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-fri-10-12" name="availability" value="FRI_10_12" ${not empty profile and profile.availability.contains('FRI_10_12') ? 'checked' : ''}><label for="time-fri-10-12">Fri 10:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-fri-14-16" name="availability" value="FRI_14_16" ${not empty profile and profile.availability.contains('FRI_14_16') ? 'checked' : ''}><label for="time-fri-14-16">Fri 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-fri-18-20" name="availability" value="FRI_18_20" ${not empty profile and profile.availability.contains('FRI_18_20') ? 'checked' : ''}><label for="time-fri-18-20">Fri 18:00-20:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-sat-09-12" name="availability" value="SAT_09_12" ${not empty profile and profile.availability.contains('SAT_09_12') ? 'checked' : ''}><label for="time-sat-09-12">Sat 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-sat-14-16" name="availability" value="SAT_14_16" ${not empty profile and profile.availability.contains('SAT_14_16') ? 'checked' : ''}><label for="time-sat-14-16">Sat 14:00-16:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-sun-09-12" name="availability" value="SUN_09_12" ${not empty profile and profile.availability.contains('SUN_09_12') ? 'checked' : ''}><label for="time-sun-09-12">Sun 09:00-12:00</label></div>
                    <div class="checkbox-item"><input type="checkbox" id="time-sun-14-16" name="availability" value="SUN_14_16" ${not empty profile and profile.availability.contains('SUN_14_16') ? 'checked' : ''}><label for="time-sun-14-16">Sun 14:00-16:00</label></div>
                </div>
            </div>
            
            <div class="form-group">
                <label for="notes">Additional Notes</label>
                <textarea id="notes" name="notes" class="form-control" rows="4">${not empty profile ? profile.notes : ''}</textarea>
            </div>
            
            <button type="submit" class="btn btn-primary">Save profile</button>
        </form>
        
        <c:if test="${not empty profile and not empty profile.updatedAt}">
            <p class="last-updated">Last updated: ${fn:substring(profile.updatedAt, 0, 10)} ${fn:substring(profile.updatedAt, 11, 16)}</p>
        </c:if>
    </div>
</main>

<script>
function addSkill(skill) {
    var input = document.getElementById('skillsInput');
    var current = input.value.trim();
    // Avoid duplicates
    var existing = current ? current.split(/\\s*,\\s*/) : [];
    for (var i = 0; i < existing.length; i++) {
        if (existing[i].toLowerCase() === skill.toLowerCase()) return;
    }
    if (current) current += ', ';
    input.value = current + skill;
    input.focus();
}
</script>
</body>
</html>