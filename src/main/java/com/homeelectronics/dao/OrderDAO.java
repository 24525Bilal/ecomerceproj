package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection; // Make sure this is uncommented
import com.homeelectronics.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Import ResultSet
import java.sql.SQLException;
import java.sql.Types; // Import Types for setNull
import java.sql.Statement; // <-- Import Statement
import com.homeelectronics.model.CartItem;// <-- Import CartItem
import com.homeelectronics.model.Address;

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
     * Retrieves a paginated list of orders for a specific user.
     * @param userId The ID of the user.
     * @param pageNumber The current page number (e.g., 1, 2, 3...).
     * @param pageSize The number of orders per page (e.g., 8).
     * @return A list of Order objects for the specified page.
     * @throws SQLException
     */
    public List<Order> getOrdersByUserId(int userId, int pageNumber, int pageSize) throws SQLException {
        List<Order> orders = new ArrayList<>();
        // Calculate the OFFSET based on the page number and size
        // (pageNumber - 1) * pageSize, e.g., Page 1: (1-1)*8=0, Page 2: (2-1)*8=8
        int offset = (pageNumber - 1) * pageSize;

        // SQL query updated with LIMIT and OFFSET
        String sql = "SELECT id, order_id, user_id, address_id, total_amount, payment_method, payment_status, transaction_id, order_date " +
                "FROM orders " +
                "WHERE user_id = ? " +
                "ORDER BY order_date DESC " +
                "LIMIT ? OFFSET ?"; // LIMIT = pageSize, OFFSET = offset

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, pageSize); // Set the LIMIT
            ps.setInt(3, offset);   // Set the OFFSET

            rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderId(rs.getString("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setAddressId(rs.getInt("address_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setPaymentStatus(rs.getString("payment_status"));
                order.setTransactionId(rs.getString("transaction_id"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    public Order getOrderDetailsById(int orderIntId) throws SQLException {
        Order order = null;
        List<OrderItem> items = new ArrayList<>();
        Address address = null; // To hold the address

        // --- UPDATED SQL ---
        // Selects o.transaction_id and joins with addresses table
        // Selects only the columns that exist in your Address.java model
        String sql = "SELECT " +
                "o.id, o.order_id, o.order_date, o.payment_status, o.total_amount, o.payment_method, o.transaction_id, " + // <-- Fetches transaction_id
                "oi.order_item_id, oi.quantity, oi.price, " +
                "p.id as product_pk_id, p.name, " +
                "pi.image_path, " +
                // --- Fetches columns from YOUR Address.java model ---
                "a.id as address_id, a.address, a.state, a.zip_code, a.country " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "LEFT JOIN addresses a ON o.address_id = a.id " +
                "LEFT JOIN (SELECT product_id, image_path, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY id) as rn FROM product_images) pi " +
                "ON p.id = pi.product_id AND pi.rn = 1 " +
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
                    order.setTransactionId(rs.getString("transaction_id")); // <-- SETS TRANSACTION ID
                    order.setItems(items);

                    // --- Create Address Object using YOUR model's setters ---
                    address = new Address();
                    address.setId(rs.getInt("address_id"));
                    address.setAddress(rs.getString("address"));     // <-- Corrected (uses your field)
                    address.setState(rs.getString("state"));       // <-- Corrected (uses your field)
                    address.setZipCode(rs.getString("zip_code"));    // <-- Corrected (uses your field)
                    address.setCountry(rs.getString("country"));     // <-- Corrected (uses your field)

                    order.setAddress(address); // <-- This now works
                }

                Product product = new Product();
                product.setId(rs.getInt("product_pk_id"));
                product.setName(rs.getString("name"));
                product.setThumbnailUrl(rs.getString("image_path"));

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

    /**
     * Gets the total count of orders for a specific user.
     * @param userId The ID of the user.
     * @return The total number of orders.
     * @throws SQLException
     */
    public int getOrderCountByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalOrders = 0;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                totalOrders = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return totalOrders;



    }
    /**
     * [ADMIN] Fetches all orders for the sales page, with pagination and corrected search.
     * @param pageNumber The current page (1-based)
     * @param pageSize The number of items per page (e.g., 8)
     * @param searchQuery A search term for order_id or user_id
     * @return A list of paginated orders
     * @throws SQLException
     */
    public List<Order> getAllOrders(int pageNumber, int pageSize, String searchQuery) throws SQLException {
        List<Order> orders = new ArrayList<>();
        int offset = (pageNumber - 1) * pageSize; // Calculate offset

        // Use StringBuilder for cleaner SQL building
        StringBuilder sql = new StringBuilder("SELECT id, order_id, user_id, total_amount, payment_status, order_date FROM orders ");

        List<Object> params = new ArrayList<>(); // To hold our ? parameters safely
        boolean hasSearch = searchQuery != null && !searchQuery.trim().isEmpty();

        if (hasSearch) {
            List<String> whereClauses = new ArrayList<>();

            // 1. Always search by order_id (string)
            whereClauses.add("order_id ILIKE ?"); // ILIKE for PostgreSQL case-insensitivity
            params.add("%" + searchQuery + "%");

            // 2. Try to search by user_id (number)
            int searchUserId = -1;
            try {
                // Check if user typed "CUST123"
                if (searchQuery.toUpperCase().startsWith("CUST")) {
                    searchUserId = Integer.parseInt(searchQuery.substring(4)); // Get number after "CUST"
                } else {
                    // Try to parse the whole string as a number
                    searchUserId = Integer.parseInt(searchQuery);
                }
            } catch (NumberFormatException e) {
                // Not a valid number or "CUST" prefix, so we just ignore it
            }

            if (searchUserId != -1) {
                whereClauses.add("user_id = ?");
                params.add(searchUserId);
            }

            // Append all WHERE clauses, joined by "OR"
            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" OR ", whereClauses));
            }
        }

        // 3. Add Ordering and Pagination
        sql.append(" ORDER BY order_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);   // LIMIT
        params.add(offset);     // OFFSET

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());

            // Set all parameters dynamically
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderId(rs.getString("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setPaymentStatus(rs.getString("payment_status"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllOrders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orders;
    }

    /**
     * [ADMIN] Gets the total count of ALL orders, with corrected search, for pagination.
     * @param searchQuery A search term for order_id or user_id
     * @return The total number of matching orders
     * @throws SQLException
     */
    public int getTotalOrderCount(String searchQuery) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders ");
        List<Object> params = new ArrayList<>();
        boolean hasSearch = searchQuery != null && !searchQuery.trim().isEmpty();

        if (hasSearch) {
            List<String> whereClauses = new ArrayList<>();

            // 1. Always search by order_id (string)
            whereClauses.add("order_id ILIKE ?"); // ILIKE for PostgreSQL
            params.add("%" + searchQuery + "%");

            // 2. Try to search by user_id (number)
            int searchUserId = -1;
            try {
                if (searchQuery.toUpperCase().startsWith("CUST")) {
                    searchUserId = Integer.parseInt(searchQuery.substring(4));
                } else {
                    searchUserId = Integer.parseInt(searchQuery);
                }
            } catch (NumberFormatException e) {
                // Ignore
            }

            if (searchUserId != -1) {
                whereClauses.add("user_id = ?");
                params.add(searchUserId);
            }

            if (!whereClauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" OR ", whereClauses));
            }
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalOrders = 0;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());

            // Set all parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                totalOrders = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error in getTotalOrderCount: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return totalOrders;
    }

    /**
     * [ADMIN] Updates the payment_status of an order based on its integer ID.
     * This is the method the admin's dropdown will call.
     * @param orderIntId The integer primary key (id) of the order
     * @param newStatus The new status string (e.g., "shipped", "delivered")
     * @return true if the update was successful, false otherwise
     * @throws SQLException
     */
    public boolean updateOrderStatus(int orderIntId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, orderIntId);

            int rowsAffected = ps.executeUpdate();
            success = (rowsAffected > 0); // Will be true if 1 row was updated

        } catch (SQLException e) {
            System.err.println("Error in updateOrderStatus: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

}

