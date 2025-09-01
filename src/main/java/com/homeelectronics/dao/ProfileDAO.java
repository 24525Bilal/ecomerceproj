package com.homeelectronics.dao;

import com.homeelectronics.model.Profile;
import com.homeelectronics.db.DBConnection;
import java.sql.*;

public class ProfileDAO {
    private Connection conn;
    public ProfileDAO(Connection conn) {
        this.conn = conn;
    }
    // Get user ID by email - like UserDAO style
    public int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Get profile by user ID
    public Profile getProfileByUserId(int userId) {
        String sql = "SELECT * FROM user_details WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Profile profile = new Profile();
                    profile.setId(rs.getInt("id"));
                    profile.setUserId(rs.getInt("user_id"));
                    profile.setFirstName(rs.getString("first_name"));
                    profile.setLastName(rs.getString("last_name"));
                    profile.setDob(rs.getDate("dob"));
                    profile.setPhoneNumber(rs.getString("phone_number"));
                    return profile;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Check if profile exists
    public boolean profileExists(int userId) {
        String sql = "SELECT COUNT(*) FROM user_details WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Create new profile
    public boolean createProfile(Profile profile) {
        String sql = "INSERT INTO user_details (user_id, first_name, last_name, dob, phone_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setDate(4, profile.getDob());
            ps.setString(5, profile.getPhoneNumber());

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update profile details
    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE user_details SET first_name=?, last_name=?, dob=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setDate(3, profile.getDob());
            ps.setInt(4, profile.getUserId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update phone number
    public boolean updatePhone(int userId, String phone) {
        String sql = "UPDATE user_details SET phone_number=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, userId);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update password
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete account
    public boolean deleteAccount(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
