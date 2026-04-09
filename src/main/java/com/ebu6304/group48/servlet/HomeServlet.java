package com.ebu6304.group48.servlet;

import com.ebu6304.group48.config.AppPaths;
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
        req.setAttribute("dataDirectory", AppPaths.resolveDataDirectory(getServletContext()));
        HttpSession session = req.getSession(false);
        if (session != null) {
            req.setAttribute("loggedIn", session.getAttribute(SessionKeys.USER_ID) != null);
            req.setAttribute("username", session.getAttribute(SessionKeys.USERNAME));
            req.setAttribute("role", session.getAttribute(SessionKeys.ROLE));
        } else {
            req.setAttribute("loggedIn", false);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
