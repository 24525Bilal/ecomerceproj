package com.homeelectronics.servlet;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/adminlogin")

public class AdminLoginServlet extends HttpServlet {
    private static final String ADMIN_EMAIL = "admin@buyhive.com";
    private static final String ADMIN_PASSWORD = "helloadmin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            // ✅ valid → Create session and redirect to dashboard
            HttpSession session = request.getSession();
            session.setAttribute("adminEmail", email); // Use a distinct attribute for admins

            // 🍪 Add a cookie for the admin
            Cookie adminCookie = new Cookie("adminEmail", email);
            adminCookie.setHttpOnly(true);
            adminCookie.setMaxAge(365 * 24 * 60 * 60); // Set cookie for 1 year
            response.addCookie(adminCookie);

            //  redirection logic
            String requestedURI = (String) session.getAttribute("adminRequestedURI");
            if (requestedURI != null && !requestedURI.isEmpty()) {
                session.removeAttribute("adminRequestedURI"); // Clean up the session attribute
                response.sendRedirect(requestedURI);
            } else {
                // If no specific URL was requested, redirect to the default admin dashboard
                response.sendRedirect("account-marketplace-dashboard.jsp");
            }


//            // ✅ valid → redirect to dashboard
//            response.sendRedirect("account-marketplace-dashboard.jsp");
        } else {
            // ❌ invalid → redirect with error flag
            response.sendRedirect("admin-signin.html?error=invalid");
        }
    }
}
