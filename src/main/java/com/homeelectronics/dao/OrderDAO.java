package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection; // Make sure this is uncommented
import com.homeelectronics.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Import ResultSet
import java.sql.SQLException;
import java.sql.Types; // Import Types for setNull
import java.sql.Statement; // <-- Import Statement
import com.homeelectronics.model.CartItem; // <-- Import CartItem

import java.util.ArrayList;
import java.util.List;
import com.homeelectronics.model.Product;
import com.homeelectronics.model.OrderItem;

public class OrderDAO {

    /**
     * Creates a new order in the database.
     * MODIFIED: Now accepts a Connection (for transactions) and returns the new order's INT ID.
     * @param order The Order object to be saved.
     * @param conn An active database connection.
     * @return The new integer primary key (id) of the created order.
     * @throws SQLException
     */
    public int createOrder(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO orders (order_id, user_id, address_id, total_amount, payment_method, payment_status, transaction_id, order_date) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int orderId = -1;

        try {
            // We pass Statement.RETURN_GENERATED_KEYS to get the new 'id' back
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

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

            if (rowsAffected > 0) {
                // Get the generated primary key (the 'id' column)
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    orderId = rs.getInt(1); // <-- This is the new integer ID
                }
            }
            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback in the servlet
        } finally {
            // DO NOT close the connection here, the servlet will
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
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


    // added when needed in the order page
    /**
     * Retrieves all orders for a specific user.
     * @param userId The ID of the user whose orders are to be fetched.
     * @return A list of Order objects.
     * @throws SQLException
     */
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        // Added payment_status to the SELECT query
        String sql = "SELECT id, order_id, user_id, address_id, total_amount, payment_method, payment_status, transaction_id, order_date FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderId(rs.getString("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setAddressId(rs.getInt("address_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setPaymentStatus(rs.getString("payment_status")); // Fetch the status
                order.setTransactionId(rs.getString("transaction_id"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead
            throw e;
        } finally {
            // Ensure resources are closed properly
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    /**
     * Retrieves a single order by its integer ID, including all its items.
     * (This is the method for the offcanvas - should already be here from my previous answer)
     */
    public Order getOrderDetailsById(int orderIntId) throws SQLException {
        Order order = null;
        List<OrderItem> items = new ArrayList<>();

        // --- UPDATED SQL ---
        // Joins with product_images like CartDAO does
        String sql = "SELECT " +
                "o.id, o.order_id, o.order_date, o.payment_status, o.total_amount, o.payment_method, " +
                "oi.order_item_id, oi.quantity, oi.price, " +
                "p.id as product_pk_id, p.name, " +
                "pi.image_path " + // <-- Select image_path from product_images
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                // --- ADDED JOIN ---
                "LEFT JOIN (SELECT product_id, image_path, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY id) as rn FROM product_images) pi " +
                "ON p.id = pi.product_id AND pi.rn = 1 " + // Join with product_images to get the first image
                "WHERE o.id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderIntId);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (order == null) {
                    order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setOrderId(rs.getString("order_id"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setPaymentStatus(rs.getString("payment_status"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    order.setItems(items);
                }

                Product product = new Product();
                product.setId(rs.getInt("product_pk_id"));
                product.setName(rs.getString("name"));
                // --- Use image_path, but store it in thumbnailUrl field ---
                product.setThumbnailUrl(rs.getString("image_path")); // <-- Get image_path from result

                OrderItem item = new OrderItem();
                item.setId(rs.getInt("order_item_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                item.setProduct(product);

                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error fetching order details for ID " + orderIntId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("General Error fetching order details for ID " + orderIntId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return order;
    }

    /**
     * NEW METHOD: Adds a list of cart items to the order_items table.
     * @param orderId The new integer ID from createOrder.
     * @param cartItems The list of items from the user's cart.
     * @param conn An active database connection.
     * @throws SQLException
     */
    public void addOrderItems(int orderId, List<CartItem> cartItems, Connection conn) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);

            for (CartItem item : cartItems) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProduct().getId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getProduct().getPrice()); // Or item.getPrice() if cart has a snapshot price
                ps.addBatch(); // Add this query to the batch
            }

            ps.executeBatch(); // Execute all queries at once

        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        } finally {
            // DO NOT close the connection
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }



    }




}