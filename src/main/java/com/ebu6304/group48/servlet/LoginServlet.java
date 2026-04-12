package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.User;
import com.ebu6304.group48.repository.UserRepository;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession existing = req.getSession(false);
        if (existing != null && existing.getAttribute(SessionKeys.USER_ID) != null) {
            resp.sendRedirect(req.getContextPath() + landingPath(String.valueOf(existing.getAttribute(SessionKeys.ROLE))));
            return;
        }
        if ("forbidden".equals(req.getParameter("error"))) {
            req.setAttribute("message", "You do not have access to that page for your role.");
        }
        if ("banned".equals(req.getParameter("error"))) {
            req.setAttribute("message", "This account has been banned.");
            req.setAttribute("appealPageHref", req.getContextPath() + "/ban-appeal");
        }
        if ("1".equals(req.getParameter("registered"))) {
            req.setAttribute("message", "Registration successful. Please sign in.");
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
        try {
            repo.ensureStorage();
            Optional<User> user = repo.authenticate(username != null ? username : "", password != null ? password : "");
            if (user.isEmpty()) {
                req.setAttribute("message", "Invalid username or password.");
                req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
                return;
            }
            if (Boolean.TRUE.equals(user.get().getBanned())) {
                HttpSession s = req.getSession(true);
                s.setAttribute(SessionKeys.BANNED_APPEAL_USER_ID, user.get().getUserId());
                req.setAttribute("message", "This account has been banned.");
                req.setAttribute("appealPageHref", req.getContextPath() + "/ban-appeal");
                req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute(SessionKeys.USER_ID, user.get().getUserId());
            session.setAttribute(SessionKeys.USERNAME, user.get().getUsername());
            session.setAttribute(SessionKeys.ROLE, user.get().getRole());

            String next = safeNext(req.getParameter("next"), req.getContextPath());
            if (next != null) {
                resp.sendRedirect(req.getContextPath() + next);
            } else {
                resp.sendRedirect(req.getContextPath() + landingPath(user.get().getRole()));
            }
        } catch (IOException e) {
            req.setAttribute("message", "Login failed: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
        }
    }

    private static String landingPath(String role) {
        if ("MO".equals(role)) {
            return "/mo/dashboard";
        }
        if ("ADMIN".equals(role)) {
            return "/admin/workload";
        }
        return "/ta/dashboard";
    }

    /** Prevent open redirects: only same-app absolute paths. */
    private static String safeNext(String next, String contextPath) {
        if (next == null || next.isBlank()) {
            return null;
        }
        next = next.trim();
        if (next.contains("://") || !next.startsWith("/") || next.startsWith("//")) {
            return null;
        }
        return next;
    }
}
