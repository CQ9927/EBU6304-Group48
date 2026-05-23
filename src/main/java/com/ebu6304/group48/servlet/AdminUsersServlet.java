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
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "AdminUsersServlet", urlPatterns = "/admin/users")
public class AdminUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
        repo.ensureStorage();
        List<User> users = repo.findAll().stream()
                .sorted(Comparator.comparing(u -> u.getUsername() != null ? u.getUsername().toLowerCase() : ""))
                .collect(Collectors.toList());
        req.setAttribute("users", users);
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.setAttribute("selfUserId", req.getSession().getAttribute(SessionKeys.USER_ID));
        req.setAttribute("navCurrent", "users");
        req.getRequestDispatcher("/WEB-INF/jsp/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = trim(req.getParameter("action"));
        String userId = trim(req.getParameter("userId"));
        String selfId = String.valueOf(req.getSession().getAttribute(SessionKeys.USER_ID));

        UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
        String redirect = req.getContextPath() + "/admin/users";

        try {
            repo.ensureStorage();
            if ("ban".equals(action)) {
                if (userId.isEmpty() || userId.equals(selfId)) {
                    resp.sendRedirect(redirect + "?error=self");
                    return;
                }
                String banReason = req.getParameter("banReason");
                if (repo.setBanned(userId, true, banReason)) {
                    resp.sendRedirect(redirect + "?saved=ban");
                } else {
                    resp.sendRedirect(redirect + "?error=notfound");
                }
                return;
            }
            if ("unban".equals(action)) {
                if (userId.isEmpty()) {
                    resp.sendRedirect(redirect + "?error=invalid");
                    return;
                }
                if (repo.setBanned(userId, false, null)) {
                    resp.sendRedirect(redirect + "?saved=unban");
                } else {
                    resp.sendRedirect(redirect + "?error=notfound");
                }
                return;
            }
            if ("resetPassword".equals(action)) {
                String newPassword = req.getParameter("newPassword");
                if (userId.isEmpty() || newPassword == null || newPassword.isBlank()) {
                    resp.sendRedirect(redirect + "?error=invalid");
                    return;
                }
                if (repo.resetPassword(userId, newPassword)) {
                    resp.sendRedirect(redirect + "?saved=password");
                } else {
                    resp.sendRedirect(redirect + "?error=notfound");
                }
                return;
            }
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(redirect + "?error=invalid");
            return;
        }

        resp.sendRedirect(redirect + "?error=invalid");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
