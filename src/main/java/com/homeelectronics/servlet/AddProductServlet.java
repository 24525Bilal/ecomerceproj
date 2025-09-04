package com.homeelectronics.servlet;

import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the "Add Product" form submission and file uploads.
 */
@WebServlet("/addProduct")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class AddProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Use a try-catch for robust error handling (e.g., bad number formats from user input).
        try {
            // 1. Get all text data from the form.
            String name = request.getParameter("productName");
            String description = request.getParameter("productDescription");
            double price = Double.parseDouble(request.getParameter("price"));
            int stockQuantity = Integer.parseInt(request.getParameter("stockQuantity"));
            String category = request.getParameter("category");
            String tags = request.getParameter("tags");
            String color = request.getParameter("color");
            String size = request.getParameter("size");
            String model = request.getParameter("model");
            String manufacturer = request.getParameter("manufacturer");
            String finish = request.getParameter("finish");
            String capacity = request.getParameter("capacity");
            String chip = request.getParameter("chip");
            String diagonal = request.getParameter("diagonal");
            String screenType = request.getParameter("screenType");
            String resolution = request.getParameter("resolution");
            String refreshRate = request.getParameter("refreshRate");

            // 2. Save product details first to get the unique productId for the folder name.
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            product.setTags(tags);
            product.setColor(color);
            product.setSize(size);
            product.setModel(model);
            product.setManufacturer(manufacturer);
            product.setFinish(finish);
            product.setCapacity(capacity);
            product.setChip(chip);
            product.setDiagonal(diagonal);
            product.setScreenType(screenType);
            product.setResolution(resolution);
            product.setRefreshRate(refreshRate);

            ProductDAO productDAO = new ProductDAO();
            int productId = productDAO.addProduct(product);

            // 3. If product was saved, handle file uploads.
            if (productId > 0) {
                List<Part> fileParts = request.getParts().stream()
                        .filter(part -> "productImage".equals(part.getName()) && part.getSize() > 0)
                        .collect(Collectors.toList());

                if (!fileParts.isEmpty()) {
                    // Define the physical save location (e.g., D:\product_image).
                    String baseUploadPath = "D:" + File.separator + "product_image";

                    System.out.println("--- File Upload Location: " + baseUploadPath + " ---");
                    // Create a unique folder for this product (e.g., D:\product_image\101).
                    String productSpecificPath = baseUploadPath + File.separator + productId;
                    File productDir = new File(productSpecificPath);
                    if (!productDir.exists()) {
                        productDir.mkdirs();
                    }



                    List<String> imagePathsForDb = new ArrayList<>();
                    for (Part filePart : fileParts) {
                        String fileName = extractFileName(filePart);
                        // Save the physical file.
                        filePart.write(productSpecificPath + File.separator + fileName);
                        // Prepare the relative web path for the database.
                        imagePathsForDb.add("product-images/" + productId + "/" + fileName);
                    }

                    // Save image paths to the database.
                    productDAO.addProductImages(productId, imagePathsForDb);
                }

                // On success, redirect back to the products page.
                response.sendRedirect("account-marketplace-products.jsp");

            } else {
                // On failure, redirect back to the products page with an error.
                response.sendRedirect("account-marketplace-products.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // On any unexpected error, redirect back to the products page.
            response.sendRedirect("account-marketplace-products.jsp");
        }
    }

    /**
     * Extracts the clean file name from the request part header.
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return new File(s.substring(s.indexOf("=") + 2, s.length() - 1)).getName();
            }
        }
        return "";
    }
}