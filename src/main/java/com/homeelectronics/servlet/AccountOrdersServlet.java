package com.homeelectronics.servlet;

import com.homeelectronics.dao.OrderDAO;
import com.homeelectronics.model.Order;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/account-orders")
public class AccountOrdersServlet extends HttpServlet {

    private OrderDAO orderDAO;

    @Override
    public void init() {
        orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // --- Corrected User ID Retrieval ---
        Integer userIdFromSession = null; // Use Integer wrapper class

        if (session != null && session.getAttribute("userId") != null) {
            // Retrieve the userId stored by SigninServlet
            Object userIdAttr = session.getAttribute("userId");
            if (userIdAttr instanceof Integer) {
                userIdFromSession = (Integer) userIdAttr;
            }
        }

        // Check if we successfully got the user ID
        if (userIdFromSession == null) {
            // If ID is not found, redirect to login (AuthFilter might also do this)
            response.sendRedirect("account-signin.html");
            return;
        }
        // --- End Corrected Retrieval ---


        try {
            // Use the retrieved ID (now an int) with your DAO method
            List<Order> orders = orderDAO.getOrdersByUserId(userIdFromSession); // Pass the int ID
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/account-orders.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace(); // Log error
            request.setAttribute("errorMessage", "Could not retrieve your orders. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}