package com.homeelectronics.servlet;

import com.google.gson.Gson;
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
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        if (userId == null) {
            String requestedURI = request.getRequestURI();
            if (session == null) {
                session = request.getSession();
            }
            session.setAttribute("requestedURI", requestedURI);
            response.sendRedirect("account-signin.html");
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            boolean shouldRedirect = "true".equals(request.getParameter("redirect"));

            CartDAO cartDAO = new CartDAO();
            cartDAO.addOrUpdateCartItem(userId, productId, quantity);

            List<CartItem> updatedCartItems = cartDAO.getCartItemsByUserId(userId);
            session.setAttribute("cartItems", updatedCartItems);

            if (shouldRedirect) {
                response.sendRedirect("checkout-v1-cart.jsp");
            } else {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String jsonResponse = new Gson().toJson(updatedCartItems);
                response.getWriter().write(jsonResponse);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID or quantity.");
        }
    }
}