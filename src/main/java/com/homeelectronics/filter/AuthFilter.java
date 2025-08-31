package com.homeelectronics.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/*") // apply to all requests
public class AuthFilter implements Filter {
    @Override

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;



        String uri = req.getRequestURI();



        HttpSession session = req.getSession(false);

        // Allow login & signup pages without session
        if (uri.endsWith("account-signin.html") || uri.endsWith("account-signup.html")
                || uri.endsWith("signin") || uri.endsWith("signup")) {
            chain.doFilter(request, response);
            return;
        }

        // Block other pages if user not logged in
        if (session == null || session.getAttribute("userEmail") == null) {
            res.sendRedirect("account-signin.html?error=sessionExpired");
            return;
        }




        // Continue request if session is valid
        chain.doFilter(request, response);
    }
}