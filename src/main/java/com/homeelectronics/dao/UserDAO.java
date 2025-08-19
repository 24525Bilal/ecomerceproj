package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection;
import com.homeelectronics.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean saveUser(User user) {
        // Step 1: Check if the user already exists
        if (isUserExists(user.getEmail())) {
            System.out.println("❌ Signup failed. Email already exists.");
            return false; // Return false if user exists
        }

        // Step 2: Proceed with insertion if the user is new
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0; // true if insert worked

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // New method to check if a user with the given email already exists
    public boolean isUserExists(String email) {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // Returns true if a row is found
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume exists to prevent signup on database error
        }
    }
}