package com.homeelectronics.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/*") // apply to all requests
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Define a list of pages that require a logged-in user.
        boolean isUserProtectedPage = path.equals("/cartPage")
                || path.equals("/addProductToCart")
                || path.equals("/removeFromCart")
                || path.equals("/clearCart")
                || uri.endsWith("account-info.jsp")
                || uri.endsWith("account-addresses.jsp")
                || uri.endsWith("account-orders.jsp")
                || uri.endsWith("account-notifications.jsp")
                || uri.endsWith("account-payment.jsp")
                || uri.endsWith("checkout-v1-cart.jsp")
                || uri.endsWith("account-reviews.jsp")
                || path.equals("/cartPage")
                || uri.endsWith("checkout-v1-cart.jsp")
                || path.equals("/checkout")
                || uri.endsWith("checkout-v1-delivery-1.jsp");



        // Define a list of pages that require a logged-in admin.
        boolean isAdminPage = path.contains("account-marketplace-dashboard.html")
                || path.contains("account-marketplace-products.jsp")
                || path.contains("account-marketplace-products")
                || path.contains("account-marketplace-orders.html")
                || path.contains("addProduct")
                || path.contains("account-marketplace-purchases.html")
                || path.contains("account-marketplace-users.html")
                || path.contains("account-marketplace-sales.html");


        if (isUserProtectedPage) {
            // Check if the user is logged in for user-protected pages.
            if (session != null && session.getAttribute("userEmail") != null) {
                chain.doFilter(request, response);
            } else {
                // Not logged in, redirect to user sign-in.
                String requestedURI = req.getRequestURI();
                req.getSession().setAttribute("requestedURI", requestedURI);
                res.sendRedirect(contextPath + "/account-signin.html");
            }
            return;
        }

        if (isAdminPage) {
            // Check if the admin is logged in for admin pages.
            if (session != null && session.getAttribute("adminEmail") != null) {
                chain.doFilter(request, response);
            } else {
                // Not logged in as admin, redirect to admin sign-in.
                String requestedURI = req.getRequestURI();
                req.getSession().setAttribute("adminRequestedURI", requestedURI);
                res.sendRedirect(contextPath + "/admin-signin.html");
            }
            return;
        }

        // For all other pages (not explicitly user- or admin-protected), allow access.
        chain.doFilter(request, response);
    }
}