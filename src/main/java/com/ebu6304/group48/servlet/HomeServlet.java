package com.ebu6304.group48.servlet;

import com.ebu6304.group48.util.RoleLanding;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(SessionKeys.USER_ID) != null) {
            String role = String.valueOf(session.getAttribute(SessionKeys.ROLE));
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + RoleLanding.defaultPath(role)));
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
