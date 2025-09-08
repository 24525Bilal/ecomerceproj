package com.homeelectronics.servlet;

import com.homeelectronics.dao.AddressDAO;
import com.homeelectronics.model.Address;
import com.homeelectronics.model.Profile; // Import the Profile class
import com.homeelectronics.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/account-addresses")
public class AddressServlet extends HttpServlet {

    private final AddressDAO addressDAO = new AddressDAO();

    /**
     * Safely gets the user ID from the session, whether a User or Profile object is stored.
     * @param session The current HttpSession.
     * @return The user's ID as an int, or -1 if not found.
     * @throws SQLException if a database error occurs during email-to-ID lookup.
     */
    private int getUserIdFromSession(HttpSession session) throws SQLException {
        Object userObject = session.getAttribute("userDetails");

        if (userObject instanceof Profile) {
            // If it's a Profile object, we can get the ID directly.
            return ((Profile) userObject).getUserId();
        } else if (userObject instanceof User) {
            // If it's a User object, we need to look up the ID from the email.
            String email = ((User) userObject).getEmail();
            return addressDAO.getUserIdByEmail(email);
        }

        // Return -1 if the object is missing or of an unknown type.
        return -1;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userDetails") == null) {
            response.sendRedirect("account-signin.html");
            return;
        }

        try {
            int userId = getUserIdFromSession(session);
            if (userId == -1) {
                // User not found in DB or invalid session object
                response.sendRedirect("logout");
                return;
            }

            List<Address> addresses = addressDAO.getAddressesByUserId(userId);
            request.setAttribute("addresses", addresses);
            request.getRequestDispatcher("/account-addresses.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Database error occurred while fetching addresses.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userDetails") == null) {
            response.sendRedirect("account-signin.html");
            return;
        }

        String action = request.getParameter("action");

        try {
            int userId = getUserIdFromSession(session);
            if (userId == -1) {
                response.sendRedirect("logout");
                return;
            }

            switch (action) {
                case "add":
                    addAddress(request, userId);
                    break;
                case "update":
                    updateAddress(request, userId);
                    break;
                case "delete":
                    deleteAddress(request, userId);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error occurred while managing addresses.", e);
        }

        response.sendRedirect(request.getContextPath() + "/account-addresses");
    }

    private void addAddress(HttpServletRequest request, int userId) throws SQLException {
        Address address = new Address();
        address.setUserId(userId);
        address.setCountry(request.getParameter("country"));
        address.setState(request.getParameter("state"));
        address.setZipCode(request.getParameter("zipCode"));
        address.setAddress(request.getParameter("address"));
        address.setPrimary(request.getParameter("setPrimary") != null);
        addressDAO.addAddress(address);
    }

    private void updateAddress(HttpServletRequest request, int userId) throws SQLException {
        Address address = new Address();
        address.setId(Integer.parseInt(request.getParameter("addressId")));
        address.setUserId(userId);
        address.setCountry(request.getParameter("country"));
        address.setState(request.getParameter("state"));
        address.setZipCode(request.getParameter("zipCode"));
        address.setAddress(request.getParameter("address"));
        address.setPrimary(request.getParameter("setPrimary") != null);
        addressDAO.updateAddress(address);
    }

    private void deleteAddress(HttpServletRequest request, int userId) throws SQLException {
        int addressId = Integer.parseInt(request.getParameter("addressId"));
        addressDAO.deleteAddress(addressId, userId);
    }
}