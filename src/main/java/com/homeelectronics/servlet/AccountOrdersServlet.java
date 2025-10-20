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

/**
 * Servlet to handle displaying a paginated list of orders for the
 * "My Account -> Orders" page.
 */
@WebServlet("/account-orders")
public class AccountOrdersServlet extends HttpServlet {

    private OrderDAO orderDAO;

    // Define the number of orders to display per page
    private static final int ORDERS_PER_PAGE = 8;

    @Override
    public void init() {
        orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // --- User ID Retrieval ---
        Integer userIdFromSession = null;

        if (session != null && session.getAttribute("userId") != null) {
            Object userIdAttr = session.getAttribute("userId");
            if (userIdAttr instanceof Integer) {
                userIdFromSession = (Integer) userIdAttr;
            }
        }

        // Check if we successfully got the user ID
        if (userIdFromSession == null) {
            // If ID is not found, redirect to login
            response.sendRedirect("account-signin.html");
            return;
        }
        // --- End User ID Retrieval ---


        // --- Pagination Logic ---
        int currentPage = 1; // Default to the first page
        String pageParam = request.getParameter("page");

        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) {
                    currentPage = 1; // Ensure page number is positive
                }
            } catch (NumberFormatException e) {
                // If the "page" parameter is not a valid number, default to 1
                currentPage = 1;
            }
        }
        // --- End Pagination Logic ---


        try {
            // 1. Get the total count of orders for this user
            int totalOrders = orderDAO.getOrderCountByUserId(userIdFromSession);

            // 2. Calculate the total number of pages
            // (int) Math.ceil((double) 50 / 8) -> (int) Math.ceil(6.25) -> 7 pages
            int totalPages = (int) Math.ceil((double) totalOrders / ORDERS_PER_PAGE);

            // 3. (Optional but good) Ensure currentPage isn't out of bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }

            // 4. Fetch only the list of orders for the current page
            List<Order> orders = orderDAO.getOrdersByUserId(userIdFromSession, currentPage, ORDERS_PER_PAGE);

            // 5. Set attributes for the JSP
            request.setAttribute("orders", orders);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalOrders", totalOrders); // Optional: good for "Showing 1-8 of 50"

            // 6. Forward to the JSP page
            request.getRequestDispatcher("/account-orders.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace(); // Log error
            request.setAttribute("errorMessage", "Could not retrieve your orders. Please try again later.");
            request.getRequestDispatcher("/error.jsp").forward(request, response); // Forward to a generic error page
        }
    }
}