package com.homeelectronics.servlet;

import com.homeelectronics.dao.AddressDAO;
import com.homeelectronics.dao.ProfileDAO; // 1. Import ProfileDAO
import com.homeelectronics.model.Address;
import com.homeelectronics.model.Profile;// 2. Import Profile
import com.homeelectronics.model.CartItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// I've updated the mapping to be more specific to this page
@WebServlet("/checkout-shipping")
public class ShippingServlet extends HttpServlet {

    // In ShippingServlet.java

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");


            AddressDAO addressDAO = new AddressDAO();
            ProfileDAO profileDAO = new ProfileDAO();

            try {
                // Fetch the Profile object
                Profile profile = profileDAO.getProfileByUserId(userId);



                // Fetch Address
                List<Address> addresses = addressDAO.getAddressesByUserId(userId);
                Address primaryAddress = null;
                for (Address address : addresses) {
                    if (address.isPrimary()) {
                        primaryAddress = address;
                        break;
                    }
                }

                // --- Order Summary Calculation ---
                // Get the cart from the session
                List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

                 // Calculate the subtotal
                double subtotal = 0;
                if (cartItems != null) {
                    for (CartItem item : cartItems) {
                        subtotal += item.getTotalPrice();
                    }
                }

                // Set the fixed shipping cost and calculate the total
                double shippingCost = 100.0;
                double totalCost = subtotal + shippingCost;



                // Set attributes for the JSP page
                request.setAttribute("primaryAddress", primaryAddress);
                request.setAttribute("profile", profile);
                request.setAttribute("userEmail", session.getAttribute("userEmail"));


                session.setAttribute("sessionCartSubtotal", subtotal); // Storing in session
                session.setAttribute("sessionShippingCost", shippingCost); // Storing in session
                session.setAttribute("sessionTotalCost", totalCost);       // Storing in session

                request.setAttribute("cartSubtotal", subtotal);
                request.setAttribute("shippingCost", shippingCost);
                request.setAttribute("totalCost", totalCost);

            } catch (SQLException e) {
                e.printStackTrace();
                throw new ServletException("Database error fetching checkout data", e);
            }

            request.getRequestDispatcher("checkout-v1-shipping.jsp").forward(request, response);

        } else {
            response.sendRedirect("account-signin.html");
        }
    }
}