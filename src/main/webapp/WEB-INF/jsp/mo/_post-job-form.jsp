<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<form method="post" action="${pageContext.request.contextPath}/mo/jobs/new" class="form-stack" id="post-job-form">
    <p>
        <label for="title">Title *</label><br/>
        <input type="text" id="title" name="title" required/>
    </p>
    <p>
        <label for="type">Type *</label><br/>
        <select id="type" name="type" required>
            <option value="">— choose —</option>
            <option value="MODULE">MODULE</option>
            <option value="INVIGILATION">INVIGILATION</option>
        </select>
    </p>
    <p>
        <label for="semester">Semester *</label><br/>
        <input type="text" id="semester" name="semester" placeholder="e.g. 2026_SPRING" required/>
    </p>
    <p>
        <label for="schedule">Schedule *</label><br/>
        <select id="schedule" name="schedule" required>
            <option value="">— choose —</option>
            <option value="MON_09_12">MON_09_12</option>
            <option value="MON_14_16">MON_14_16</option>
            <option value="MON_18_20">MON_18_20</option>
            <option value="TUE_09_12">TUE_09_12</option>
            <option value="TUE_14_16">TUE_14_16</option>
            <option value="TUE_18_20">TUE_18_20</option>
            <option value="WED_09_12">WED_09_12</option>
            <option value="WED_14_16">WED_14_16</option>
            <option value="WED_18_20">WED_18_20</option>
            <option value="THU_09_12">THU_09_12</option>
            <option value="THU_14_16">THU_14_16</option>
            <option value="THU_18_20">THU_18_20</option>
            <option value="FRI_09_12">FRI_09_12</option>
            <option value="FRI_10_12">FRI_10_12</option>
            <option value="FRI_14_16">FRI_14_16</option>
            <option value="FRI_18_20">FRI_18_20</option>
            <option value="SAT_09_12">SAT_09_12</option>
            <option value="SAT_14_16">SAT_14_16</option>
            <option value="SUN_09_12">SUN_09_12</option>
            <option value="SUN_14_16">SUN_14_16</option>
        </select>
    </p>
    <p>
        <label for="capacity">Capacity *</label><br/>
        <input type="number" id="capacity" name="capacity" min="1" required/>
    </p>
    <p>
        <label for="requiredSkills">Required skills (comma-separated)</label><br/>
        <input type="text" id="requiredSkills" name="requiredSkills" placeholder="Java, Teaching, Algorithms"/>
    </p>
    <div class="modal__actions">
        <button type="button" class="btn btn-ghost" data-modal-close>Cancel</button>
        <button type="submit" class="btn btn-primary">Create job</button>
    </div>
</form>
