package com.homeelectronics.dao;

import com.homeelectronics.db.DBConnection;
import com.homeelectronics.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
// Handles All Database Logic
// . It correctly handles adding products and their images to the separate tables and fetching them for the catalog page
public class ProductDAO {

    /**
     * Adds a product to the database and returns the generated product ID.
     */
    public int addProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category, tags, color, size, model, manufacturer, finish, capacity, chip, diagonal, screen_type, resolution, refresh_rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int productId = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getTags());
            stmt.setString(7, product.getColor());
            stmt.setString(8, product.getSize());
            stmt.setString(9, product.getModel());
            stmt.setString(10, product.getManufacturer());
            stmt.setString(11, product.getFinish());
            stmt.setString(12, product.getCapacity());
            stmt.setString(13, product.getChip());
            stmt.setString(14, product.getDiagonal());
            stmt.setString(15, product.getScreenType());
            stmt.setString(16, product.getResolution());
            stmt.setString(17, product.getRefreshRate());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        productId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productId;
    }

    /**
     * Adds a list of image paths for a specific product ID to the product_images table.
     */
    public void addProductImages(int productId, List<String> imagePaths) {
        String sql = "INSERT INTO product_images (product_id, image_path) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (String imagePath : imagePaths) {
                stmt.setInt(1, productId);
                stmt.setString(2, imagePath);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a paginated list of products. For each product, it also fetches the *first* image
     * to be used as a thumbnail on the catalog page.
     */
    public List<Product> getProducts(int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        // This SQL query joins the products with their images and uses a window function
        // to efficiently get the first image for each product.
        String sql = "WITH NumberedImages AS (" +
                "    SELECT " +
                "        p.*, " +
                "        pi.image_path, " +
                "        ROW_NUMBER() OVER(PARTITION BY p.id ORDER BY pi.id) as rn " +
                "    FROM products p " +
                "    LEFT JOIN product_images pi ON p.id = pi.product_id" +
                ") " +
                "SELECT * FROM NumberedImages WHERE rn = 1 " +
                "ORDER BY id DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();

                    // --- Basic Info ---
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setThumbnailUrl(rs.getString("image_path")); // Set the thumbnail URL

                    // --- Details for Hover Panel ---
                    product.setModel(rs.getString("model"));
                    product.setManufacturer(rs.getString("manufacturer"));
                    product.setCapacity(rs.getString("capacity"));
                    product.setChip(rs.getString("chip"));
                    product.setDiagonal(rs.getString("diagonal"));
                    product.setScreenType(rs.getString("screen_type"));
                    product.setResolution(rs.getString("resolution"));


                    // Set other fields as needed for the catalog page
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Gets the total count of products in the database for pagination.
     */
    public int getProductCount() {
        String sql = "SELECT COUNT(*) FROM products";
        int count = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}