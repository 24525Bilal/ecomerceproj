//This new servlet handles the logic for removing an item

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

@WebServlet("/removeFromCart")
public class RemoveFromCartServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            int productId = Integer.parseInt(request.getParameter("productId"));

            CartDAO cartDAO = new CartDAO();
            cartDAO.removeCartItem(userId, productId);

            // Update the session with the new cart data
            List<CartItem> updatedCart = cartDAO.getCartItemsByUserId(userId);
            session.setAttribute("cartItems", updatedCart);

            response.sendRedirect("cartPage");
        } else {
            response.sendRedirect("account-signin.html");
        }
    }
}