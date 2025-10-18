package com.homeelectronics.servlet;

import com.homeelectronics.dao.AddressDAO;
import com.homeelectronics.dao.ProfileDAO; // Assuming you have this DAO
import com.homeelectronics.model.Address;
import com.homeelectronics.model.Profile; // Assuming you have this model

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/payment") // Map this servlet to a URL like /payment
public class PaymentPageServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            String userEmail = (String) session.getAttribute("userEmail"); // Get email if stored

            AddressDAO addressDAO = new AddressDAO();
            ProfileDAO profileDAO = new ProfileDAO(); // Instantiate ProfileDAO
            Address primaryAddress = null;
            Profile profile = null;

            try {
                // Option 1: Retrieve from session if set previously (e.g., by ShippingServlet)
                primaryAddress = (Address) session.getAttribute("primaryAddress");

                // Option 2: Fetch fresh if not in session or if needed
                if (primaryAddress == null) {
                    List<Address> addresses = addressDAO.getAddressesByUserId(userId);
                    for (Address address : addresses) {
                        if (address.isPrimary()) {
                            primaryAddress = address;
                            break;
                        }
                    }
                    if (primaryAddress != null) {
                        session.setAttribute("primaryAddress", primaryAddress); // Store in session
                    }
                }

                // Fetch profile data
                profile = profileDAO.getProfileByUserId(userId); // Assuming this method exists

                // Set attributes for the JSP
                request.setAttribute("primaryAddress", primaryAddress);
                request.setAttribute("profile", profile);
                request.setAttribute("userEmail", userEmail); // Pass email too


                // Retrieving from session using the SAME names
                Double subtotal = (Double) session.getAttribute("sessionCartSubtotal");
                Double shippingCost = (Double) session.getAttribute("sessionShippingCost");
                Double totalCost = (Double) session.getAttribute("sessionTotalCost");

                // Passing to JSP using request attributes
                request.setAttribute("cartSubtotal", subtotal);
                request.setAttribute("shippingCost", shippingCost);
                request.setAttribute("totalCost", totalCost);





            } catch (SQLException e) {
                // Log the error properly
                e.printStackTrace();
                // Optionally redirect to an error page
                response.sendRedirect("errorPage.jsp"); // Example error page
                return;
            }

            // Forward to the JSP page
            request.getRequestDispatcher("checkout-v1-payment.jsp").forward(request, response);

        } else {
            // Redirect to login if user is not logged in
            response.sendRedirect("account-signin.html");
        }
    }

    // You might handle POST requests differently if the shipping form submitted here
    // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //    doGet(request, response); // Simple example: just call doGet
    // }
}