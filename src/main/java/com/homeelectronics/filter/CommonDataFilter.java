package com.homeelectronics.filter;

import com.homeelectronics.dao.CartDAO;
import com.homeelectronics.model.CartItem;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


// mapping is added to xml file
//@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class CommonDataFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);



        // Check if the userId is present in the session
        if (session != null && session.getAttribute("userId") != null) {
            // Get the userId directly from the session
            int userId = (int) session.getAttribute("userId");



            // Use the provided CartDAO to fetch the cart items for the logged-in user
            CartDAO cartDAO = new CartDAO();
            List<CartItem> cartItems = cartDAO.getCartItemsByUserId(userId);

            // Calculate the subtotal
            double subtotal = 0.0;
            for (CartItem item : cartItems) {
                // Ensure the totalPrice is calculated correctly for each item
                subtotal += item.getProduct().getPrice() * item.getQuantity();
            }

            // Set the cart items and subtotal as attributes in the request scope.
            // This makes them accessible in all JSPs using JSTL.
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("subtotal", subtotal);

        }

        // Continue the request chain, allowing the request to reach the intended JSP
        chain.doFilter(request, response);
    }
}