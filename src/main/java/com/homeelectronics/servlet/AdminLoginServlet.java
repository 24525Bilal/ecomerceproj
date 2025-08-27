package com.homeelectronics.servlet;

import java.io.IOException;
import jakarta.servlet.*;
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
            // ✅ valid → redirect to dashboard
            response.sendRedirect("account-marketplace-dashboard.html");
        } else {
            // ❌ invalid → redirect with error flag
            response.sendRedirect("admin-signin.html?error=invalid");
        }
    }
}
