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

//This servlet correctly handles all the fields from the form and saves the product and its images.


@WebServlet("/addProduct")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class AddProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get all text-based form data
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

        // 2. Handle multiple file uploads
        List<String> imagePaths = new ArrayList<>();
        String uploadPath = getServletContext().getRealPath("") + File.separator + "assets/img/shop/electronics";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Filter for parts that are file uploads and are not empty
        List<Part> fileParts = request.getParts().stream()
                .filter(part -> "productImage".equals(part.getName()) && part.getSize() > 0)
                .collect(Collectors.toList());

        for (Part filePart : fileParts) {
            String fileName = extractFileName(filePart);
            filePart.write(uploadPath + File.separator + fileName);
            imagePaths.add("assets/img/shop/electronics/" + fileName);
        }

        // 3. Create Product object
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

        // 4. Save product and its images to the DB
        ProductDAO productDAO = new ProductDAO();
        int productId = productDAO.addProduct(product);

        if (productId > 0 && !imagePaths.isEmpty()) {
            productDAO.addProductImages(productId, imagePaths);
            // Redirect to a success page or back to the products page
            response.sendRedirect("account-marketplace-products.html?status=success");
        } else {
            // Redirect to an error page or back to the form with an error message
            response.sendRedirect("add-product.html?status=error");
        }
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                // Handles file paths in different browsers, takes only the file name
                return new File(s.substring(s.indexOf("=") + 2, s.length() - 1)).getName();
            }
        }
        return "";
    }
}