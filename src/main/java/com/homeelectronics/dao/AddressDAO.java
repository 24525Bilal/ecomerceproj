package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection;
import com.homeelectronics.model.Address;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    /**
     * Retrieves a user's ID from the database using their email address.
     * This is the first step before fetching address-specific data.
     * @param email The user's email.
     * @return The user's ID, or -1 if not found.
     */
    public int getUserIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1; // Indicates user not found
    }

    /**
     * Gets all addresses for a specific user ID, with the primary address listed first.
     * @param userId The ID of the user.
     * @return A list of addresses.
     */
    public List<Address> getAddressesByUserId(int userId) throws SQLException {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_primary DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Address address = new Address();
                    address.setId(rs.getInt("id"));
                    address.setUserId(rs.getInt("user_id"));
                    address.setCountry(rs.getString("country"));
                    address.setState(rs.getString("state"));
                    address.setZipCode(rs.getString("zip_code"));
                    address.setAddress(rs.getString("address"));
                    address.setPrimary(rs.getBoolean("is_primary"));
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }

    /**
     * Adds a new address to the database for a given user ID.
     * @param address The address to add.
     */
    public void addAddress(Address address) throws SQLException {
        if (address.isPrimary()) {
            unsetAllPrimary(address.getUserId());
        }
        String sql = "INSERT INTO addresses (user_id, country, state, zip_code, address, is_primary) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, address.getUserId());
            stmt.setString(2, address.getCountry());
            stmt.setString(3, address.getState());
            stmt.setString(4, address.getZipCode());
            stmt.setString(5, address.getAddress());
            stmt.setBoolean(6, address.isPrimary());
            stmt.executeUpdate();
        }
    }

    /**
     * Updates an existing address.
     * @param address The address with updated information.
     */
    public void updateAddress(Address address) throws SQLException {
        if (address.isPrimary()) {
            unsetAllPrimary(address.getUserId());
            setAddressAsPrimary(address.getId(), address.getUserId());
        }
        String sql = "UPDATE addresses SET country = ?, state = ?, zip_code = ?, address = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, address.getCountry());
            stmt.setString(2, address.getState());
            stmt.setString(3, address.getZipCode());
            stmt.setString(4, address.getAddress());
            stmt.setInt(5, address.getId());
            stmt.setInt(6, address.getUserId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an address by its ID, ensuring it belongs to the correct user.
     * @param addressId The ID of the address to delete.
     * @param userId The ID of the user for security.
     */
    public void deleteAddress(int addressId, int userId) throws SQLException {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Helper method to set all of a user's addresses to not be primary.
     */
    private void unsetAllPrimary(int userId) throws SQLException {
        String sql = "UPDATE addresses SET is_primary = false WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Helper method to set a specific address as primary.
     */
    private void setAddressAsPrimary(int addressId, int userId) throws SQLException {
        String sql = "UPDATE addresses SET is_primary = true WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}