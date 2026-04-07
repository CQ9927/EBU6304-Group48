<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>TA Profile</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 42rem; margin: 2rem auto; padding: 0 1rem; }
        .container { display: grid; gap: 1.5rem; }
        .card { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0.375rem; padding: 1.5rem; }
        .card h2 { margin-top: 0; color: #2c3e50; }
        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; margin-bottom: 0.375rem; font-weight: 500; color: #495057; }
        .form-control { width: 100%; padding: 0.5rem 0.75rem; font-size: 1rem; line-height: 1.5; border: 1px solid #ced4da; border-radius: 0.25rem; box-sizing: border-box; }
        .form-control:focus { border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }
        .checkbox-group { display: flex; flex-wrap: wrap; gap: 0.75rem; margin-top: 0.5rem; }
        .checkbox-item { display: flex; align-items: center; gap: 0.375rem; }
        .btn { display: inline-block; font-weight: 400; text-align: center; vertical-align: middle; user-select: none; padding: 0.5rem 1rem; font-size: 1rem; line-height: 1.5; border-radius: 0.25rem; border: 1px solid transparent; cursor: pointer; }
        .btn-primary { color: #fff; background-color: #007bff; border-color: #007bff; }
        .btn-primary:hover { background-color: #0069d9; border-color: #0062cc; }
        .alert { padding: 0.75rem 1.25rem; margin-bottom: 1rem; border: 1px solid transparent; border-radius: 0.25rem; }
        .alert-success { color: #155724; background-color: #d4edda; border-color: #c3e6cb; }
        .alert-danger { color: #721c24; background-color: #f8d7da; border-color: #f5c6cb; }
        .nav { display: flex; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid #dee2e6; }
        .nav a { color: #007bff; text-decoration: none; }
        .nav a:hover { text-decoration: underline; }
        .last-updated { font-size: 0.875rem; color: #6c757d; margin-top: 0.5rem; }
    </style>
</head>
<body>
<div class="container">
    <h1>TA Profile Management</h1>
    
    <div class="nav">
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/ta/profile" style="font-weight: bold;">Profile</a>
        <a href="${pageContext.request.contextPath}/ta/cv">CV</a>
        <a href="${pageContext.request.contextPath}/ta/jobs">Jobs</a>
        <a href="${pageContext.request.contextPath}/logout" style="margin-left: auto;">Logout</a>
    </div>

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
            
            <button type="submit" class="btn btn-primary">Save Profile</button>
        </form>
        
        <c:if test="${not empty profile and not empty profile.updatedAt}">
            <p class="last-updated">Last updated: ${profile.updatedAt}</p>
        </c:if>
    </div>
</div>
</body>
</html>