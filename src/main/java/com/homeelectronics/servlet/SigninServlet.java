package com.homeelectronics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// i  added when new show the user name dyanamically
import com.homeelectronics.model.Profile;
import com.homeelectronics.dao.ProfileDAO;
import com.homeelectronics.db.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

//for session creating

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;


import com.homeelectronics.dao.UserDAO;

// added while for cart
import com.homeelectronics.model.CartItem;
import com.homeelectronics.dao.CartDAO;
import java.util.List;

@WebServlet("/signin")
public class SigninServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();

        // Check if the email exists in the database
        boolean emailExists = userDAO.isEmailExists(email);

        if (emailExists) {
            // If email exists, check if the password matches
            boolean isValidUser = userDAO.isValidUser(email, password);

            if (isValidUser) {

                // ✅ Create session (main login tracking)
                HttpSession session = request.getSession();
                session.setAttribute("userEmail", email);

                // implimented mainly to show the username everywhere
                // Fetch user details and add them to the session
                try (Connection conn = DBConnection.getConnection()) {
                    ProfileDAO profileDAO = new ProfileDAO(conn);
                    int userId = profileDAO.getUserIdByEmail(email);
                    if (userId != -1) {
                        Profile userDetails = profileDAO.getProfileByUserId(userId);
                        if (userDetails != null) {

                            // Store the entire userDetails object in the session
                            session.setAttribute("userDetails", userDetails);

                            // to show the greeting message
                            // Prepare the greeting message and store it in the session
                            String greetingMessage = "Hello, user"; // Default
                            if (userDetails.getFirstName() != null && !userDetails.getFirstName().isEmpty()) {
                                greetingMessage = "Hello, " + userDetails.getFirstName();
                            }
                            session.setAttribute("greetingMessage", greetingMessage);


                            // for the cart
                            // 1. Fetch the user's persistent cart from the database
                            CartDAO cartDAO = new CartDAO();
                            List<CartItem> cartItems = cartDAO.getCartItemsByUserId(userId);

                            // 2. Store the list of cart items in the session
                            session.setAttribute("cartItems", cartItems);


                        }
                    }
                } catch (SQLException e) {
                    // Log the error and handle it appropriately
                    e.printStackTrace();
                }


                // adding cookie
                Cookie loginCookie = new Cookie("userEmail", email);
                loginCookie.setHttpOnly(true);   //
                loginCookie.setMaxAge(365 * 24 * 60 * 60);  // 1 year;     // only over HTTPS
                response.addCookie(loginCookie);


//                // ✅ Valid user → Redirect to dashboard
//                response.sendRedirect("home-electronics.html");


                // EDIT: Check for a saved URL and redirect accordingly.
                String requestedURI = (String) session.getAttribute("requestedURI");
                if (requestedURI != null && !requestedURI.isEmpty()) {
                    session.removeAttribute("requestedURI"); // Clean up the session
                    response.sendRedirect(requestedURI);
                } else {
                    // If no URL was saved, redirect to the default dashboard.
                    response.sendRedirect("home-electronics.html");
                }



            } else {
                // ❌ Incorrect password → Redirect with a specific error flag
                response.sendRedirect("account-signin.html?error=password");
            }
        } else {
            // ❌ Email doesn't exist → Redirect with a different error flag
            response.sendRedirect("account-signin.html?error=email");

        }
    }
}



