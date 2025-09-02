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
                        }
                    }
                } catch (SQLException e) {
                    // Log the error and handle it appropriately
                    e.printStackTrace();
                }




                // adding cookie
                Cookie loginCookie = new Cookie("userEmail", email);
                loginCookie.setHttpOnly(true);   //
                loginCookie.setMaxAge(30 * 60);  // 30 mins(true);     // only over HTTPS
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



