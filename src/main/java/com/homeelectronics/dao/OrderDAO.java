package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection; // Make sure this is uncommented
import com.homeelectronics.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Import ResultSet
import java.sql.SQLException;
import java.sql.Types; // Import Types for setNull

public class OrderDAO {

    /**
     * Creates a new order in the database.
     * @param order The Order object to be saved.
     * @return true if the order was created successfully, false otherwise.
     * @throws SQLException
     */
    public boolean createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_id, user_id, address_id, total_amount, payment_method, payment_status, transaction_id, order_date) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setString(1, order.getOrderId());
            ps.setInt(2, order.getUserId());
            ps.setInt(3, order.getAddressId());
            ps.setDouble(4, order.getTotalAmount());
            ps.setString(5, order.getPaymentMethod());
            ps.setString(6, order.getPaymentStatus());

            if (order.getTransactionId() != null) {
                ps.setString(7, order.getTransactionId());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Gets the numeric part of the last Order ID from the database.
     * Assumes Order IDs are like 'ORDNOXXXXX'.
     * @return The last order number, or 0 if no orders exist.
     * @throws SQLException
     */
    public int getLastOrderNumber() throws SQLException {
        // Query to find the highest order_id, extract the number part, and order descending
        // substring function might vary slightly depending on exact DB (this is for PostgreSQL)
        String sql = "SELECT order_id FROM orders WHERE order_id LIKE 'ORDNO%' ORDER BY CAST(substring(order_id from 6) AS INTEGER) DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int lastNumber = 0; // Default if no orders found

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                String lastOrderId = rs.getString("order_id");
                // Extract number part (assuming format ORDNOXXXXX)
                try {
                    lastNumber = Integer.parseInt(lastOrderId.substring(5)); // Get substring after "ORDNO"
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    System.err.println("Could not parse order number from ID: " + lastOrderId);
                    // Handle error appropriately - maybe default to a safe value or re-query differently
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return lastNumber;
    }

    /**
     * Gets the numeric part of the last Transaction ID from the database.
     * Assumes Transaction IDs are like 'TXNNOXXXXX'.
     * @return The last transaction number, or 0 if no transactions exist.
     * @throws SQLException
     */
    public int getLastTransactionNumber() throws SQLException {
        // Query to find the highest transaction_id, extract number part, order descending
        String sql = "SELECT transaction_id FROM orders WHERE transaction_id LIKE 'TXNNO%' ORDER BY CAST(substring(transaction_id from 6) AS INTEGER) DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int lastNumber = 0; // Default if no transactions found

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                String lastTxnId = rs.getString("transaction_id");
                // Extract number part (assuming format TXNNOXXXXX)
                if (lastTxnId != null) { // Check if transaction_id is not null
                    try {
                        lastNumber = Integer.parseInt(lastTxnId.substring(5)); // Get substring after "TXNNO"
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        System.err.println("Could not parse transaction number from ID: " + lastTxnId);
                        // Handle error appropriately
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return lastNumber;
    }
}