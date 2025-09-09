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

// Maps this servlet to the /checkout URL
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the current user's session
        HttpSession session = request.getSession(false);

        // Check if a user is logged in
        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            AddressDAO addressDAO = new AddressDAO();

            try {
                // Fetch all addresses for the current user
                List<Address> addresses = addressDAO.getAddressesByUserId(userId);
                Address primaryAddress = null;

                // Find the primary address from the list
                for (Address address : addresses) {
                    if (address.isPrimary()) {
                        primaryAddress = address;
                        break;
                    }
                }

                // Set the primary address as a request attribute
                request.setAttribute("primaryAddress", primaryAddress);

                if (primaryAddress != null) {
                    session.setAttribute("primaryAddress", primaryAddress);
                }


            } catch (SQLException e) {
                e.printStackTrace();
                // Handle database errors
            }
        }

        // Forward the request to the JSP page
        request.getRequestDispatcher("checkout-v1-delivery-1.jsp").forward(request, response);
    }
}