package com.homeelectronics.servlet;

import com.homeelectronics.dao.CartDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/clearCart")
public class ClearCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;

        if (userId != null) {
            CartDAO cartDAO = new CartDAO();
            cartDAO.clearCartByUserId(userId);

            // Redirect back to the cart page after clearing
            response.sendRedirect("cartPage");
        } else {
            // Redirect to login if not authenticated
            response.sendRedirect("account-signin.html");
        }
    }
}