package com.ebu6304.group48.servlet;

import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "MoDashboardServlet", urlPatterns = "/mo/dashboard")
public class MoDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("username", req.getSession().getAttribute(SessionKeys.USERNAME));
        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }
}
