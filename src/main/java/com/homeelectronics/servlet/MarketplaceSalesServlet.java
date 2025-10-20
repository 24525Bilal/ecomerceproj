package com.homeelectronics.servlet;

import com.homeelectronics.dao.OrderDAO;
import com.homeelectronics.model.Order;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// We'll map this to /admin/sales. You can change this URL.
@WebServlet("/admin-sales")
public class MarketplaceSalesServlet extends HttpServlet {

    // As requested, 8 items per page
    private static final int PAGE_SIZE = 8;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OrderDAO orderDAO = new OrderDAO();

        // --- 1. Get Pagination Parameters ---
        String pageParam = request.getParameter("page");
        int currentPage = 1;
        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) currentPage = 1;
            } catch (NumberFormatException e) {
                currentPage = 1;
            }
        }

        // --- 2. Get Search Parameter ---
        String searchQuery = request.getParameter("search");

        try {
            // --- 3. Fetch Data from DAO ---
            // Get the list of orders for the current page and search
            List<Order> salesList = orderDAO.getAllOrders(currentPage, PAGE_SIZE, searchQuery);

            // Get the total count of orders for pagination links
            int totalOrders = orderDAO.getTotalOrderCount(searchQuery);
            int totalPages = (int) Math.ceil((double) totalOrders / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1; // Ensure at least 1 page

            // --- 4. Set Attributes for JSP ---
            request.setAttribute("salesList", salesList);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalOrders", totalOrders);
            // Pass the search query back so the search bar and pagination links can use it
            request.setAttribute("searchQuery", searchQuery);

            // --- 5. Forward to the JSP page ---
            // Ensure this path matches your file structure
            RequestDispatcher dispatcher = request.getRequestDispatcher("/account-marketplace-sales.jsp");
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error retrieving sales list.");
        }
    }
}