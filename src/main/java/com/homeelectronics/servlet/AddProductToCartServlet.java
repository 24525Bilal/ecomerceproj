
// This servlet will act as the controller for the "Add to Cart" action.
//  It will read the productId and quantity from the form and then use your CartDAO to save the data to the database.




package com.homeelectronics.servlet;

import com.homeelectronics.dao.CartDAO;
import com.homeelectronics.model.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/addProductToCart")
public class AddProductToCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Attempt to get the session without creating a new one if it doesn't exist.
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        if (userId == null) {
            // Redirect to login if user is not authenticated.
            // Save the current URL so we can redirect back after a successful login.
            String requestedURI = request.getRequestURI();
            if (session == null) {
                session = request.getSession(); // Create a new session to store the requestedURI
            }
            session.setAttribute("requestedURI", requestedURI);
            response.sendRedirect("account-signin.html");
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            // Use the CartDAO to add the item to the database
            CartDAO cartDAO = new CartDAO();
            cartDAO.addOrUpdateCartItem(userId, productId, quantity);

            // Fetch the updated cart from the database and store it in the session
            List<CartItem> updatedCartItems = cartDAO.getCartItemsByUserId(userId);
            session.setAttribute("cartItems", updatedCartItems);

            // Redirect to the cart page
            response.sendRedirect("cartPage");
        } catch (NumberFormatException e) {
            // Handle invalid product ID or quantity
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID or quantity.");
        }
    }}