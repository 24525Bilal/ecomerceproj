
// handles all communication with the database for the cart.
//It includes methods to fetch, add, and remove items.

package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection;
import com.homeelectronics.model.CartItem;
import com.homeelectronics.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    /**
     * Fetches all cart items for a given user ID from the database.
     */
    public List<CartItem> getCartItemsByUserId(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        // Fetches product details and quantity for a user's cart

         // SQL query to join with product_images and order by cart_items.id in descending order.
        String sql = "SELECT ci.quantity, p.id, p.name, p.price, p.model, pi.image_path " +
                "FROM carts c " +
                "JOIN cart_items ci ON c.id = ci.cart_id " +
                "JOIN products p ON ci.product_id = p.id " +
                "LEFT JOIN (SELECT product_id, image_path, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY id) as rn FROM product_images) pi " +
                "ON p.id = pi.product_id AND pi.rn = 1 " +
                "WHERE c.user_id = ? ORDER BY ci.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setModel(rs.getString("model"));
                    // Set the thumbnail URL from the joined product_images table
                    product.setThumbnailUrl(rs.getString("image_path"));
                    int quantity = rs.getInt("quantity");
                    cartItems.add(new CartItem(product, quantity));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartItems;
    }

    /**
     * Adds a product to the user's cart or updates the quantity if it already exists.
     */
    public void addOrUpdateCartItem(int userId, int productId, int quantity) {
        Integer cartId = null;

        // SQL to find the user's existing cart ID
        String findCartSql = "SELECT id FROM carts WHERE user_id = ?";
        // SQL to insert a new cart and get the generated ID
        String createCartSql = "INSERT INTO carts (user_id) VALUES (?)";
        // SQL to add or update the item in the cart
        String cartItemsSql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) ON CONFLICT (cart_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Check if the user already has a cart
            try (PreparedStatement findStmt = conn.prepareStatement(findCartSql)) {
                findStmt.setInt(1, userId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        cartId = rs.getInt("id"); // Cart exists, get its ID
                    }
                }
            }

            // Step 2: If no cart was found, create one
            if (cartId == null) {
                try (PreparedStatement createStmt = conn.prepareStatement(createCartSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    createStmt.setInt(1, userId);
                    createStmt.executeUpdate();
                    try (ResultSet generatedKeys = createStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            cartId = generatedKeys.getInt(1); // Get the new cart ID
                        } else {
                            throw new SQLException("Creating cart failed, no ID obtained.");
                        }
                    }
                }
            }

            // Step 3: Add or update the item in the cart_items table using the cartId
            try (PreparedStatement itemStmt = conn.prepareStatement(cartItemsSql)) {
                itemStmt.setInt(1, cartId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.executeUpdate();
            }

            conn.commit(); // Commit the transaction

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Removes a product from the user's cart.
     */
    public void removeCartItem(int userId, int productId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = ?) AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all products from the user's cart.
     */
    public void clearCartByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // REPLACE your old getCartItemQuantity with this one
    public int getCartItemQuantity(int userId, int productId) {
        // --- THIS SQL IS NOW CORRECT ---
        // It first finds the cart_id from the user_id, then gets the quantity
        String sql = "SELECT quantity FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE user_id = ?) AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity"); // Return current quantity
                } else {
                    return 0; // Not in cart yet
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return 0 on error
        }
    }
}