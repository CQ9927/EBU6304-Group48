<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<main class="site-main container-grid">
    <h1>TA Profile Management</h1>

    <c:if test="${not empty sessionScope.message}">
        <div class="alert alert-success">
            ${sessionScope.message}
            <c:remove var="message" scope="session"/>
        </div>
    </c:if>
    
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger">
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
                <label>Skills (select all that apply)</label>
                <div class="checkbox-group">
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-java" name="skills" value="Java" 
                               ${not empty profile and profile.skills.contains('Java') ? 'checked' : ''}>
                        <label for="skill-java">Java</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-python" name="skills" value="Python" 
                               ${not empty profile and profile.skills.contains('Python') ? 'checked' : ''}>
                        <label for="skill-python">Python</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-web" name="skills" value="Web Development" 
                               ${not empty profile and profile.skills.contains('Web Development') ? 'checked' : ''}>
                        <label for="skill-web">Web Development</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-db" name="skills" value="Database" 
                               ${not empty profile and profile.skills.contains('Database') ? 'checked' : ''}>
                        <label for="skill-db">Database</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-teaching" name="skills" value="Teaching" 
                               ${not empty profile and profile.skills.contains('Teaching') ? 'checked' : ''}>
                        <label for="skill-teaching">Teaching</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-algorithms" name="skills" value="Algorithms" 
                               ${not empty profile and profile.skills.contains('Algorithms') ? 'checked' : ''}>
                        <label for="skill-algorithms">Algorithms</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="skill-proctoring" name="skills" value="Proctoring" 
                               ${not empty profile and profile.skills.contains('Proctoring') ? 'checked' : ''}>
                        <label for="skill-proctoring">Proctoring</label>
                    </div>
                </div>
            </div>
            
            <div class="form-group">
                <label>Availability (select available time slots)</label>
                <div class="checkbox-group">
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-mon-14-16" name="availability" value="MON_14_16" 
                               ${not empty profile and profile.availability.contains('MON_14_16') ? 'checked' : ''}>
                        <label for="time-mon-14-16">Monday 14:00-16:00</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-wed-18-20" name="availability" value="WED_18_20" 
                               ${not empty profile and profile.availability.contains('WED_18_20') ? 'checked' : ''}>
                        <label for="time-wed-18-20">Wednesday 18:00-20:00</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-fri-10-12" name="availability" value="FRI_10_12" 
                               ${not empty profile and profile.availability.contains('FRI_10_12') ? 'checked' : ''}>
                        <label for="time-fri-10-12">Friday 10:00-12:00</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-tue-18-20" name="availability" value="TUE_18_20" 
                               ${not empty profile and profile.availability.contains('TUE_18_20') ? 'checked' : ''}>
                        <label for="time-tue-18-20">Tuesday 18:00-20:00</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-thu-18-20" name="availability" value="THU_18_20" 
                               ${not empty profile and profile.availability.contains('THU_18_20') ? 'checked' : ''}>
                        <label for="time-thu-18-20">Thursday 18:00-20:00</label>
                    </div>
                    <div class="checkbox-item">
                        <input type="checkbox" id="time-fri-09-12" name="availability" value="FRI_09_12" 
                               ${not empty profile and profile.availability.contains('FRI_09_12') ? 'checked' : ''}>
                        <label for="time-fri-09-12">Friday 09:00-12:00</label>
                    </div>
                </div>
            </div>
            
            <div class="form-group">
                <label for="notes">Additional Notes</label>
                <textarea id="notes" name="notes" class="form-control" rows="4">${not empty profile ? profile.notes : ''}</textarea>
            </div>
            
            <button type="submit" class="btn btn-primary">Save profile</button>
        </form>
        
        <c:if test="${not empty profile and not empty profile.updatedAt}">
            <p class="last-updated">Last updated: ${profile.updatedAt}</p>
        </c:if>
    </div>
</main>
</body>
</html>