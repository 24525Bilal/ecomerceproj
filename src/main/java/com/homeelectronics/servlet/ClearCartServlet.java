package com.homeelectronics.servlet;

import com.homeelectronics.dao.CartDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException; // Import SQLException

@WebServlet("/clearCart")
public class ClearCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        String cartPageUrl = "cartPage"; // Define your cart page URL here (e.g., "checkout-v1-cart.jsp")

        if (userId != null) {
            CartDAO cartDAO = new CartDAO();
            try {
                // Try to clear the cart
                cartDAO.clearCartByUserId(userId);
                System.out.println("Cart cleared successfully via ClearCartServlet for user: " + userId);
                // Redirect back to the cart page after successful clearing
                response.sendRedirect(cartPageUrl);

            } catch (SQLException e) {
                // Handle the database error if clearing fails
                System.err.println("Error clearing cart via ClearCartServlet for user ID: " + userId);
                e.printStackTrace(); // Log the full error
                // Set an error message to display on the cart page
                request.setAttribute("cartError", "Could not clear the cart due to a server error. Please try again.");
                // Forward back to the cart page to show the error
                request.getRequestDispatcher(cartPageUrl).forward(request, response);
            }
        } else {
            // Redirect to login if not authenticated
            response.sendRedirect("account-signin.html");
        }
    }

    // Optional: Implement doGet if you want to allow clearing via GET request too
    // @Override
    // protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //     doPost(req, resp); // Simply call doPost
    // }
}