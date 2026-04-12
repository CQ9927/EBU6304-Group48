<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ebu6304.group48.util.RoleLanding" %>
<%@ page import="com.ebu6304.group48.util.SessionKeys" %>
<%
    boolean guest = "true".equals(request.getParameter("guest"));
    String ctx = request.getContextPath();
    Object userId = session.getAttribute(SessionKeys.USER_ID);
    String role = session.getAttribute(SessionKeys.ROLE) != null
            ? String.valueOf(session.getAttribute(SessionKeys.ROLE))
            : null;
    String brandHref = ctx + (userId != null && !guest ? RoleLanding.defaultPath(role) : "/home");
    String navCurrent = request.getAttribute("navCurrent") != null
            ? String.valueOf(request.getAttribute("navCurrent"))
            : "";
%>
<header class="site-header">
    <div class="site-header__inner">
        <a class="site-brand" href="<%= brandHref %>">TA Recruitment</a>
        <% if (userId != null && !guest) { %>
        <nav class="site-header__actions" aria-label="Account">
            <a class="site-header__logout" href="<%= ctx %>/logout">Logout</a>
        </nav>
        <% } %>
    </div>
</header>
<% if (userId != null && !guest) { %>
<nav class="nav app-role-nav" aria-label="Primary">
    <% if ("TA".equals(role)) { %>
    <a href="<%= ctx %>/ta/dashboard" class="<%= "dashboard".equals(navCurrent) ? "nav-link--current" : "" %>">Dashboard</a>
    <a href="<%= ctx %>/ta/profile" class="<%= "profile".equals(navCurrent) ? "nav-link--current" : "" %>">Profile</a>
    <a href="<%= ctx %>/ta/cv" class="<%= "cv".equals(navCurrent) ? "nav-link--current" : "" %>">CV</a>
    <a href="<%= ctx %>/ta/jobs" class="<%= "jobs".equals(navCurrent) ? "nav-link--current" : "" %>">Jobs</a>
    <a href="<%= ctx %>/ta/status" class="<%= "status".equals(navCurrent) ? "nav-link--current" : "" %>">Status</a>
    <% } else if ("MO".equals(role)) { %>
    <a href="<%= ctx %>/mo/dashboard" class="<%= "dashboard".equals(navCurrent) ? "nav-link--current" : "" %>">Dashboard</a>
    <a href="<%= ctx %>/mo/jobs/new" class="<%= "post".equals(navCurrent) ? "nav-link--current" : "" %>">Post job</a>
    <a href="<%= ctx %>/mo/jobs/select" class="<%= "select".equals(navCurrent) ? "nav-link--current" : "" %>">Review applications</a>
    <% } else if ("ADMIN".equals(role)) { %>
    <a href="<%= ctx %>/admin/workload" class="<%= "workload".equals(navCurrent) ? "nav-link--current" : "" %>">Workload</a>
    <% } %>
</nav>
<% } %>
<% if ("forbidden".equals(request.getParameter("notice"))) { %>
<p class="app-notice app-notice--warn" role="alert">You do not have access to that page for your role.</p>
<% } %>
