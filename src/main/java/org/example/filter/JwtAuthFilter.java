package org.example.filter;

import org.example.util.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebFilter(urlPatterns = "/api/*")
public class JwtAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isPublic(request)) {
            chain.doFilter(req, res);
            return;
        }

        String token = extractBearerToken(request.getHeader("Authorization"));
        if (!JwtUtil.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"success\":false,\"message\":\"未登录或令牌已过期\"}");
            out.flush();
            return;
        }

        chain.doFilter(req, res);
    }

    private static boolean isPublic(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (uri != null && uri.contains("/api/submissions/download")) {
            return true;
        }
        if (uri != null && uri.endsWith("/api/login") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (uri != null && uri.endsWith("/api/register") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        return false;
    }

    private static String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    @Override
    public void destroy() {
    }
}
