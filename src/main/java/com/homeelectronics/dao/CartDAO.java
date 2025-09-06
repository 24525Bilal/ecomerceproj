
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
        String sql = "SELECT ci.quantity, p.id, p.name, p.price, p.model FROM carts c JOIN cart_items ci ON c.id = ci.cart_id JOIN products p ON ci.product_id = p.id WHERE c.user_id = ?";
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
                    // Use a static placeholder image for now, as product_images table structure wasn't provided
//                    product.setThumbnailUrl("assets/img/shop/electronics/thumbs/18.png");
                    product.setThumbnailUrl(rs.getString("thumbnail_url"));
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
        String cartSql = "INSERT INTO carts (user_id) VALUES (?) ON CONFLICT (user_id) DO NOTHING";
        String cartItemsSql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES ((SELECT id FROM carts WHERE user_id = ?), ?, ?) ON CONFLICT (cart_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";

        Connection conn = null;  // Declare outside so it’s visible in catch
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Ensure cart exists
            try (PreparedStatement cartStmt = conn.prepareStatement(cartSql)) {
                cartStmt.setInt(1, userId);
                cartStmt.executeUpdate();
            }

            // Add or update item
            try (PreparedStatement itemStmt = conn.prepareStatement(cartItemsSql)) {
                itemStmt.setInt(1, userId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {   // Check if connection was established
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Always close connection
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
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
}