package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.repository.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    private static final Set<String> ROLES = Set.of("TA", "MO", "ADMIN");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");
        String role = req.getParameter("role");

        if (username == null || username.isBlank()) {
            req.setAttribute("error", "Username is required.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }
        if (password == null || password.length() < 4) {
            req.setAttribute("error", "Password must be at least 4 characters.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }
        if (role == null || !ROLES.contains(role)) {
            req.setAttribute("error", "Invalid role.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }

        UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
        try {
            repo.register(username.trim(), password, role);
        } catch (IllegalStateException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        } catch (IOException e) {
            req.setAttribute("error", "Could not save user: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?registered=1");
    }
}
