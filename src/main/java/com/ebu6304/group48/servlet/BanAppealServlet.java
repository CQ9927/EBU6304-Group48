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

/**
 * Public page for banned users: verify identity, view ban reason, submit appeal text (stored on {@link User}).
 */
@WebServlet(name = "BanAppealServlet", urlPatterns = "/ban-appeal")
public class BanAppealServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object pendingId = session != null ? session.getAttribute(SessionKeys.BANNED_APPEAL_USER_ID) : null;
        if (pendingId != null) {
            UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
            repo.ensureStorage();
            Optional<User> u = repo.findByUserId(String.valueOf(pendingId));
            if (u.isEmpty() || !Boolean.TRUE.equals(u.get().getBanned())) {
                if (session != null) {
                    session.removeAttribute(SessionKeys.BANNED_APPEAL_USER_ID);
                }
                req.setAttribute("step", "identify");
                req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("step", "appeal");
            req.setAttribute("appealUser", u.get());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
            return;
        }
        req.setAttribute("step", "identify");
        req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = trim(req.getParameter("action"));
        UserRepository repo = new UserRepository(AppPaths.resolveDataDirectory(getServletContext()));
        String ctx = req.getContextPath();

        if ("identify".equals(action)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            try {
                repo.ensureStorage();
                Optional<User> user = repo.authenticate(
                        username != null ? username : "",
                        password != null ? password : "");
                if (user.isEmpty()) {
                    req.setAttribute("step", "identify");
                    req.setAttribute("identifyError", "Invalid username or password.");
                    req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
                    return;
                }
                if (!Boolean.TRUE.equals(user.get().getBanned())) {
                    req.setAttribute("step", "identify");
                    req.setAttribute("identifyError", "This account is not banned; no appeal is needed.");
                    req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
                    return;
                }
                HttpSession session = req.getSession(true);
                session.setAttribute(SessionKeys.BANNED_APPEAL_USER_ID, user.get().getUserId());
                resp.sendRedirect(ctx + "/ban-appeal");
            } catch (IOException e) {
                req.setAttribute("step", "identify");
                req.setAttribute("identifyError", "Something went wrong. Please try again later.");
                req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
            }
            return;
        }

        if ("submitAppeal".equals(action)) {
            HttpSession session = req.getSession(false);
            Object pendingId = session != null ? session.getAttribute(SessionKeys.BANNED_APPEAL_USER_ID) : null;
            if (pendingId == null) {
                resp.sendRedirect(ctx + "/ban-appeal");
                return;
            }
            String text = req.getParameter("appealText");
            try {
                repo.ensureStorage();
                if (repo.submitAppeal(String.valueOf(pendingId), text != null ? text : "")) {
                    session.removeAttribute(SessionKeys.BANNED_APPEAL_USER_ID);
                    req.setAttribute("step", "done");
                    req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
                } else {
                    req.setAttribute("step", "appeal");
                    Optional<User> u = repo.findByUserId(String.valueOf(pendingId));
                    if (u.isPresent() && Boolean.TRUE.equals(u.get().getBanned())) {
                        req.setAttribute("appealUser", u.get());
                        req.setAttribute("appealError", "Please enter your appeal message.");
                    } else {
                        session.removeAttribute(SessionKeys.BANNED_APPEAL_USER_ID);
                        req.setAttribute("step", "identify");
                        req.setAttribute("identifyError", "Your session expired or the account status changed. Please verify again.");
                    }
                    req.getRequestDispatcher("/WEB-INF/jsp/auth/ban-appeal.jsp").forward(req, resp);
                }
            } catch (IOException e) {
                resp.sendRedirect(ctx + "/ban-appeal");
            }
            return;
        }

        resp.sendRedirect(ctx + "/ban-appeal");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
