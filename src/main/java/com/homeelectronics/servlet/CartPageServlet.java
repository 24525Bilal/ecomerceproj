
// This  servlet will act as the controller for the cart page.
//It fetches the cart data and forwards it to the JSP.
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

@WebServlet("/cartPage")
public class CartPageServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            CartDAO cartDAO = new CartDAO();
            List<CartItem> cartItems = cartDAO.getCartItemsByUserId(userId);
            double subtotal = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();



            request.setAttribute("cartItems", cartItems);
            request.setAttribute("subtotal", subtotal);
            request.getRequestDispatcher("/checkout-v1-cart.jsp").forward(request, response);
        } else {
            // Redirect to login if not authenticated
            response.sendRedirect("account-signin.html");
        }
    }
}