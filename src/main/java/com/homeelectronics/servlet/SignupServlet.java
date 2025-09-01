
package com.homeelectronics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.homeelectronics.dao.UserDAO;
import com.homeelectronics.model.User;

/**
 * Servlet that handles user signup requests.
 * Uses query parameter approach to show error alerts.
 */
@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1️⃣ Get form data
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 2️⃣ Create User object
        User user = new User(email, password);

        // 3️⃣ Save user in DB
        UserDAO userDAO = new UserDAO();
        boolean success = userDAO.saveUser(user);

        // 4️⃣ Redirect based on result
        if (success) {
            // ✅ Signup successful → go to sign-in page
            response.sendRedirect("account-signin.html");
        } else {
            // ❌ Signup failed → redirect back to signup with error flag
            response.sendRedirect("account-signup.html?error=1");
        }
    }
}
