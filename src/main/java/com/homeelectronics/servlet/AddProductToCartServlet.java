package com.homeelectronics.servlet;

import com.google.gson.Gson;
import com.homeelectronics.dao.CartDAO;
import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.CartItem;
import com.homeelectronics.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/addProductToCart")
public class AddProductToCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // --- LOGGING ---
        System.out.println("AddProductToCartServlet: doPost started.");

        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        if (userId == null) {
            // --- LOGGING ---
            System.out.println("AddProductToCartServlet: User not logged in.");
            String requestedURI = request.getRequestURI();
            if (session == null) {
                session = request.getSession();
            }
            session.setAttribute("requestedURI", requestedURI);

            if (!"true".equals(request.getParameter("redirect"))) {
                sendJsonError(response, "You must be logged in to add items.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect("account-signin.html");
            }
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantityToAdd = Integer.parseInt(request.getParameter("quantity"));
            boolean shouldRedirect = "true".equals(request.getParameter("redirect"));

            // --- LOGGING ---
            System.out.println("AddProductToCartServlet: User ID: " + userId + ", Product ID: " + productId + ", Quantity to Add: " + quantityToAdd);

            CartDAO cartDAO = new CartDAO();
            ProductDAO productDAO = new ProductDAO();

            Product product = productDAO.getProductById(productId);

            if (product == null) {
                // --- LOGGING ---
                System.out.println("AddProductToCartServlet: Product not found (ID: " + productId + "). Sending JSON error.");
                sendJsonError(response, "Product not found.");
                return;
            }

            int availableStock = product.getStockQuantity();
            int currentInCart = cartDAO.getCartItemQuantity(userId, productId);

            // --- LOGGING ---
            System.out.println("AddProductToCartServlet: Stock Check - Available: " + availableStock + ", Current in Cart: " + currentInCart + ", Quantity to Add: " + quantityToAdd);

            // The Core Check
            if (availableStock < (currentInCart + quantityToAdd)) {
                String errorMsg;
                if (availableStock <= currentInCart) {
                    errorMsg = "Not enough stock. You already have " + currentInCart + " in your cart.";
                } else {
                    int remaining = availableStock - currentInCart;
                    errorMsg = "Cannot add " + quantityToAdd + " items. Only " + remaining + " more available.";
                }
                // --- LOGGING ---
                System.out.println("AddProductToCartServlet: Stock insufficient. Sending JSON error: " + errorMsg);
                sendJsonError(response, errorMsg);
                return;
            }

            // --- LOGGING ---
            System.out.println("AddProductToCartServlet: Stock sufficient. Adding/Updating cart item.");
            cartDAO.addOrUpdateCartItem(userId, productId, quantityToAdd);

            List<CartItem> updatedCartItems = cartDAO.getCartItemsByUserId(userId);
            session.setAttribute("cartItems", updatedCartItems);

            if (shouldRedirect) {
                // --- LOGGING ---
                System.out.println("AddProductToCartServlet: Redirecting after successful add.");
                response.sendRedirect("checkout-v1-cart.jsp");
            } else {
                // --- LOGGING ---
                System.out.println("AddProductToCartServlet: Sending success JSON response.");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String jsonResponse = new Gson().toJson(updatedCartItems);
                response.getWriter().write(jsonResponse);
                // --- LOGGING ---
                System.out.println("AddProductToCartServlet: Success JSON response sent.");
            }

        } catch (NumberFormatException e) {
            // --- LOGGING ---
            System.err.println("AddProductToCartServlet: NumberFormatException caught. Sending JSON error.");
            e.printStackTrace(); // Print stack trace to logs
            sendJsonError(response, "Invalid product ID or quantity.");
        } catch (Throwable t) {
            // --- LOGGING ---
            System.err.println("AddProductToCartServlet: Throwable caught. Sending JSON error.");
            t.printStackTrace(); // Print stack trace to logs
            sendJsonError(response, "An unexpected server error occurred.");
        }
        // --- LOGGING ---
        System.out.println("AddProductToCartServlet: doPost finished.");
    }

    // --- HELPER METHOD (Keep as is, but add logging inside) ---
    private void sendJsonError(HttpServletResponse response, String message, int statusCode) throws IOException {
        // --- LOGGING ---
        System.out.println("AddProductToCartServlet: Inside sendJsonError. Status: " + statusCode + ", Message: " + message);

        // Prevent writing if response is already committed (less likely but possible)
        if (response.isCommitted()) {
            System.err.println("AddProductToCartServlet: Response already committed in sendJsonError. Cannot send: " + message);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);

        response.getWriter().write(new Gson().toJson(errorResponse));
        // --- LOGGING ---
        System.out.println("AddProductToCartServlet: JSON error response written.");
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        sendJsonError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }
}