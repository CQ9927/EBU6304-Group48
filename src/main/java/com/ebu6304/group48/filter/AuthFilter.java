package com.ebu6304.group48.filter;

import com.ebu6304.group48.util.RoleLanding;
import com.ebu6304.group48.util.SessionKeys;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Requires login and matching role for {@code /ta/*}, {@code /mo/*}, {@code /admin/*}.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        String requiredRole = null;
        if (path.startsWith("/ta/")) {
            requiredRole = "TA";
        } else if (path.startsWith("/mo/")) {
            requiredRole = "MO";
        } else if (path.startsWith("/admin/")) {
            requiredRole = "ADMIN";
        }

        if (requiredRole == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object userId = session != null ? session.getAttribute(SessionKeys.USER_ID) : null;
        Object role = session != null ? session.getAttribute(SessionKeys.ROLE) : null;

        if (userId == null || role == null) {
            resp.sendRedirect(req.getContextPath() + "/login?next=" + urlEncode(path));
            return;
        }
        if (!requiredRole.equals(String.valueOf(role))) {
            String landingPath = RoleLanding.defaultPath(String.valueOf(role));
            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + landingPath + "?notice=forbidden"));
            return;
        }

        chain.doFilter(request, response);
    }

    private static String urlEncode(String path) {
        return java.net.URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
