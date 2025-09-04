package com.homeelectronics.servlet;

import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Import the annotation
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/updateProduct") // This line replaces the web.xml mapping
public class UpdateProductServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Product product = new Product();
        product.setId(Integer.parseInt(request.getParameter("productId")));
        product.setName(request.getParameter("productName"));
        product.setDescription(request.getParameter("productDescription"));
        product.setPrice(Double.parseDouble(request.getParameter("price")));
        product.setStockQuantity(Integer.parseInt(request.getParameter("stockQuantity")));
        product.setCategory(request.getParameter("category"));
        product.setTags(request.getParameter("tags"));
        product.setColor(request.getParameter("color"));
        product.setSize(request.getParameter("size"));
        product.setModel(request.getParameter("model"));
        product.setManufacturer(request.getParameter("manufacturer"));
        product.setFinish(request.getParameter("finish"));
        product.setCapacity(request.getParameter("capacity"));
        product.setChip(request.getParameter("chip"));
        product.setDiagonal(request.getParameter("diagonal"));
        product.setScreenType(request.getParameter("screenType"));
        product.setResolution(request.getParameter("resolution"));
        product.setRefreshRate(request.getParameter("refreshRate"));

        ProductDAO productDAO = new ProductDAO();
        productDAO.updateProduct(product);

        response.sendRedirect("account-marketplace-products");
    }
}