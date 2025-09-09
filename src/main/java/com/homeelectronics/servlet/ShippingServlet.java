package com.homeelectronics.servlet;

import com.homeelectronics.dao.AddressDAO;
import com.homeelectronics.model.Address;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/shipping")
public class ShippingServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            AddressDAO addressDAO = new AddressDAO();
            try {
                List<Address> addresses = addressDAO.getAddressesByUserId(userId);
                Address primaryAddress = null;
                for (Address address : addresses) {
                    if (address.isPrimary()) {
                        primaryAddress = address;
                        break;
                    }
                }

                // If no primary address is found, create an empty one to avoid null pointer errors
                if (primaryAddress == null) {
                    primaryAddress = new Address();
                }

                request.setAttribute("primaryAddress", primaryAddress);

            } catch (SQLException e) {
                e.printStackTrace();
                // Even if there's an error, send an empty address object
                request.setAttribute("primaryAddress", new Address());
            }
        } else {
            // If no user is logged in, send an empty address object
            request.setAttribute("primaryAddress", new Address());
        }

        request.getRequestDispatcher("checkout-v1-shipping.jsp").forward(request, response);
    }

    // The doPost method remains the same and is correct.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Process the submitted form data
        String firstName = request.getParameter("shipping-fn");
        String lastName = request.getParameter("shipping-ln");
        String email = request.getParameter("shipping-email");
        String mobile = request.getParameter("shipping-mobile");
        String city = request.getParameter("shipping-city");
        String postcode = request.getParameter("shipping-postcode");
        String address = request.getParameter("shipping-address");

        // Here you can save the shipping address to the session or database
        // For now, we will just redirect to the payment page
        response.sendRedirect("checkout-v1-payment.html");
    }
}