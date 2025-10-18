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

                    product.setStockQuantity(rs.getInt("stock_quantity"));

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

// the code below is added for the function of the admin product page

    /**
     * Fetches a paginated list of all products for the marketplace page.
     */
    public List<Product> getAllProducts(int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, pi.image_path FROM products p " +
                "LEFT JOIN (SELECT product_id, image_path, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY id) as rn FROM product_images) pi " +
                "ON p.id = pi.product_id AND pi.rn = 1 " +
                "ORDER BY p.id DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setThumbnailUrl(rs.getString("image_path"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Fetches a single, complete product by its ID.
     */
    public Product getProductById(int productId) {
        Product product = null;
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setCategory(rs.getString("category"));
                product.setTags(rs.getString("tags"));
                product.setColor(rs.getString("color"));
                product.setSize(rs.getString("size"));
                product.setModel(rs.getString("model"));
                product.setManufacturer(rs.getString("manufacturer"));
                product.setFinish(rs.getString("finish"));
                product.setCapacity(rs.getString("capacity"));
                product.setChip(rs.getString("chip"));
                product.setDiagonal(rs.getString("diagonal"));
                product.setScreenType(rs.getString("screen_type"));
                product.setResolution(rs.getString("resolution"));
                product.setRefreshRate(rs.getString("refresh_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }


    /**
     * Fetches all image paths for a specific product ID., added getImageUrlsByProductId when making product page dyanamic
     */
    public List<String> getImageUrlsByProductId(int productId) {
        List<String> imageUrls = new ArrayList<>();
        String sql = "SELECT image_path FROM product_images WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    imageUrls.add(rs.getString("image_path"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }





    /**
     * Updates an existing product in the database.
     */
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, description=?, price=?, stock_quantity=?, category=?, tags=?, color=?, size=?, model=?, manufacturer=?, finish=?, capacity=?, chip=?, diagonal=?, screen_type=?, resolution=?, refresh_rate=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            stmt.setInt(18, product.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a product from the database using its ID.
     */
    public void deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * Fetches a list of the most recently added products (12 products). in home-electroincs page
     */
    public List<Product> getLatestProducts(int count) {
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
                "ORDER BY id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, count);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setThumbnailUrl(rs.getString("image_path"));

                    product.setStockQuantity(rs.getInt("stock_quantity"));

                    product.setModel(rs.getString("model"));
                    product.setManufacturer(rs.getString("manufacturer"));
                    product.setCapacity(rs.getString("capacity"));
                    product.setChip(rs.getString("chip"));
                    product.setDiagonal(rs.getString("diagonal"));
                    product.setScreenType(rs.getString("screen_type"));
                    product.setResolution(rs.getString("resolution"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    /**
     * Updates the stock quantity for a specific product.
     * Decreases the stock by the quantity sold.
     *
     * @param productId    The ID of the product to update.
     * @param quantitySold The number of items sold (positive integer).
     * @return true if the update was successful, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateStockQuantity(int productId, int quantitySold) throws SQLException {
        // Ensure quantitySold is positive to prevent accidental stock increase
        if (quantitySold <= 0) {
            System.err.println("Warning: Attempted to update stock with non-positive quantity: " + quantitySold + " for product ID: " + productId);
            return false; // Or throw an IllegalArgumentException
        }

        // SQL to decrease stock. Ensure stock doesn't go below zero.
        // The GREATEST function ensures the stock doesn't become negative.
        String sql = "UPDATE products SET stock_quantity = GREATEST(0, stock_quantity - ?) WHERE id = ? AND stock_quantity >= ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            // Important: Start transaction if you need atomicity (all updates succeed or none)
            // conn.setAutoCommit(false); // Uncomment for transactions

            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantitySold); // Amount to decrease by
            ps.setInt(2, productId);
            ps.setInt(3, quantitySold); // Ensure current stock is sufficient

            int rowsAffected = ps.executeUpdate();

            // Optional: Check if the row was actually updated (rowsAffected == 1)
            // If rowsAffected == 0, it might mean the product ID didn't exist or stock was insufficient.
            if (rowsAffected == 0) {
                System.err.println("Warning: Stock update failed for product ID: " + productId + ". Product not found or insufficient stock.");
                // Rollback if using transactions
                // if (conn != null) conn.rollback();
                return false;
            }

            // Commit transaction if used
            // if (conn != null) conn.commit();
            return true; // Successfully updated

        } catch (SQLException e) {
            // Rollback transaction on error
            // if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            throw e; // Re-throw exception
        } finally {
            // Restore auto-commit if changed
            // if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

}