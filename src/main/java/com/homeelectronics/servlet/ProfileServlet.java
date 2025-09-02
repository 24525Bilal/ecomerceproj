package com.homeelectronics.servlet;

import com.homeelectronics.dao.ProfileDAO;
import com.homeelectronics.dao.UserDAO;
import com.homeelectronics.model.Profile;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Connection;
import java.sql.SQLException;
import com.homeelectronics.db.DBConnection;

@WebServlet({"/UpdateUserDetailsServlet", "/UpdateContactServlet", "/UpdatePasswordServlet", "/DeleteAccountServlet", "/account"})
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        System.out.println("=== ProfileServlet Debug ===");
        System.out.println("Session exists: " + (session != null));

        if (session == null || session.getAttribute("userEmail") == null) {
            System.out.println("❌ No session or userEmail - redirecting to login");
            response.sendRedirect("account-signin.html?error=sessionExpired");
            return;
        }

        String userEmail = (String) session.getAttribute("userEmail");
        System.out.println("✅ User email from session: " + userEmail);

        try (Connection conn = DBConnection.getConnection()) {
            ProfileDAO profileDAO = new ProfileDAO(conn);
            int userId = profileDAO.getUserIdByEmail(userEmail);

            System.out.println("🔍 User ID: " + userId);

            if (userId != -1) {
                Profile profile = profileDAO.getProfileByUserId(userId);

                System.out.println("🔍 Profile: " + profile);
                if (profile != null) {
                    System.out.println("🔍 Name: " + profile.getFirstName() + " " + profile.getLastName());
                }

                request.setAttribute("userDetails", profile);
                request.setAttribute("userEmail", userEmail);
                System.out.println("✅ Attributes set for JSP");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }

        request.getRequestDispatcher("/account-info.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            response.sendRedirect("account-signin.html?error=sessionExpired");
            return;
        }

        String userEmail = (String) session.getAttribute("userEmail");
        String servletPath = request.getServletPath();

        try (Connection conn = DBConnection.getConnection()) {
            ProfileDAO profileDAO = new ProfileDAO(conn);
            UserDAO userDAO = new UserDAO();

            int userId = profileDAO.getUserIdByEmail(userEmail);

            if (userId == -1) {
                response.sendRedirect("account-signin.html?error=userNotFound");
                return;
            }

            boolean success = false;
            String successMessage = "";
            String errorMessage = "";

            switch (servletPath) {
                case "/UpdateUserDetailsServlet":
                    success = handleBasicInfoUpdate(request, profileDAO, userId);
                    if (success) {
                        // After a successful DB update, fetch the fresh data...
                        Profile updatedProfile = profileDAO.getProfileByUserId(userId);
                        // ...and update the userDetails object in the session.
                        session.setAttribute("userDetails", updatedProfile);
                        //
                        successMessage = success ? "Profile updated successfully!" : "";
                    }else {
                        errorMessage = success ? "" : "Failed to update profile.";
                    }
                    break;

                case "/UpdateContactServlet":
                    success = handleContactUpdate(request, profileDAO, userId);
                    successMessage = success ? "Contact info updated successfully!" : "";
                    errorMessage = success ? "" : "Failed to update contact info.";
                    break;

                case "/UpdatePasswordServlet":
                    String result = handlePasswordUpdate(request, profileDAO, userDAO, userId, userEmail);
                    if (result.equals("success")) {
                        success = true;
                        successMessage = "Password updated successfully!";
                    } else {
                        errorMessage = result;
                    }
                    break;

                case "/DeleteAccountServlet":
                    success = profileDAO.deleteAccount(userId);
                    if (success) {
                        session.invalidate();
                        Cookie cookie = new Cookie("userEmail", "");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                        response.sendRedirect("account-signup.html?message=accountDeleted");
                        return;
                    } else {
                        errorMessage = "Failed to delete account.";
                    }
                    break;
            }

            // Redirect with appropriate message
            if (success && !successMessage.isEmpty()) {
                response.sendRedirect("/home_electronics/account?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
            } else if (!errorMessage.isEmpty()) {
                response.sendRedirect("/home_electronics/account?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
            } else {
                response.sendRedirect("/home_electronics/account");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database error in doPost: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("account-info?error=databaseError");
        }
    }

    private boolean handleBasicInfoUpdate(HttpServletRequest request, ProfileDAO profileDAO, int userId) {
        try {
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String dobString = request.getParameter("dob");

            if (firstName == null || lastName == null || dobString == null) {
                return false;
            }

            Profile profile = new Profile();
            profile.setUserId(userId);
            profile.setFirstName(firstName.trim());
            profile.setLastName(lastName.trim());
            profile.setDob(Date.valueOf(dobString));

            // Check if profile exists, create or update accordingly
            if (profileDAO.profileExists(userId)) {
                return profileDAO.updateProfile(profile);
            } else {
                return profileDAO.createProfile(profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleContactUpdate(HttpServletRequest request, ProfileDAO profileDAO, int userId) {
        String phone = request.getParameter("phone");
        if (phone != null && !phone.trim().isEmpty()) {
            return profileDAO.updatePhone(userId, phone.trim());
        }
        return false;
    }

    private String handlePasswordUpdate(HttpServletRequest request, ProfileDAO profileDAO, UserDAO userDAO, int userId, String userEmail) {
        String currentPassword = request.getParameter("current-password");
        String newPassword = request.getParameter("new-password");

        if (currentPassword == null || newPassword == null ||
                currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return "All password fields are required.";
        }

        if (newPassword.length() < 6) {
            return "New password must be at least 6 characters long.";
        }

        // Check if current password is correct
        if (!userDAO.isValidUser(userEmail, currentPassword)) {
            return "Current password is incorrect.";
        }

        boolean success = profileDAO.updatePassword(userId, newPassword);
        return success ? "success" : "Failed to update password.";
    }
}
