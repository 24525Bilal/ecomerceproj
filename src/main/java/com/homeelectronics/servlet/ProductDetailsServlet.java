package com.homeelectronics.servlet;

import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/productDetails")
public class ProductDetailsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int productId = Integer.parseInt(request.getParameter("id"));
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.getProductById(productId);
            List<String> imageUrls = productDAO.getImageUrlsByProductId(productId);

            request.setAttribute("product", product);
            request.setAttribute("imageUrls", imageUrls);

            request.getRequestDispatcher("/shop-product-electronics.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            // Handle cases where 'id' parameter is missing or not a number
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID.");
        }
    }
}