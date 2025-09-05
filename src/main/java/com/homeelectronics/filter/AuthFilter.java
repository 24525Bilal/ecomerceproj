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


        // Get the full path of the request from the context root.
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length()); // This gives us the path relative to your application

//        // --- DIAGNOSTIC LOGGING ---
//        // This will print every path your server receives to the console.
//        System.out.println("AuthFilter is checking path: " + path);


        // EDIT: Updated the list of public pages to include the homepage and assets.
        boolean isPublicPage = path.equals("/")
                || uri.endsWith("account-signin.html")
                || uri.endsWith("account-signup.html")
                || uri.endsWith("signin")
                || uri.endsWith("signup")
                || uri.endsWith("home-electronics.html") // Allow homepage
                || uri.startsWith(req.getContextPath() + "/assets/") // Allow CSS, JS, images
                || uri.endsWith("admin-signin.html")
                || uri.endsWith("adminlogin")
                || uri.endsWith("logout")
                || uri.endsWith("shop-catalog-electronics.jsp")
                || uri.endsWith("products")
                || path.startsWith("/product-images/")
                || uri.endsWith("/productDetails")
                || uri.contains("getProductDetails")
                || uri.endsWith("shop-product-electronics.jsp");
//                || uri.endsWith("/cartPage")
//                || path.equals("/addProductToCart")
        // Allow public pages and assets to pass through without a session.
        if (isPublicPage) {
            chain.doFilter(request, response);
            return;
        }

        // --- Admin-only pages ---

        boolean isAdminPage = path.contains("account-marketplace-dashboard.jsp")
                || path.contains("account-marketplace-products.jsp")
                || path.contains("account-marketplace-products")
                || path.contains("account-marketplace-orders.html")
                || path.contains("addProduct")
                || path.contains("account-marketplace-purchases.html")
                || path.contains("account-marketplace-payouts.html")
                || path.contains("account-marketplace-sales.html");


        if (isAdminPage) {
            if (session != null && session.getAttribute("adminEmail") != null) {
                // Admin is logged in, allow access
                chain.doFilter(request, response);
            } else {
                // Not an admin or not logged in, SAVE the requested URI
                String requestedURI = req.getRequestURI();
                // Use a separate session attribute for admin redirects
                req.getSession().setAttribute("adminRequestedURI", requestedURI);
                // Not an admin or not logged in, redirect to admin login
                res.sendRedirect(contextPath + "/admin-signin.html?error=auth");
            }
            return;
        }


        // For all other pages, check if the user is logged in.
        if (session == null || session.getAttribute("userEmail") == null) {
            String requestedURI = req.getRequestURI();
            req.getSession().setAttribute("requestedURI", requestedURI);
            res.sendRedirect("account-signin.html?error=sessionExpired");
            return;
        }

        // If the user is logged in, continue the request.
        chain.doFilter(request, response);
    }
}