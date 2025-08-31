package com.homeelectronics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Delete cookie
        Cookie cookie = new Cookie("userEmail", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // Redirect to login page
        response.sendRedirect("account-signin.html");
    }
}
