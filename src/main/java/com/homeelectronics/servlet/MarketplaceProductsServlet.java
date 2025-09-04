package com.homeelectronics.servlet;

import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Import the annotation
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/account-marketplace-products") // This line replaces the web.xml mapping
public class MarketplaceProductsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();
        int page = 1;
        int pageSize = 10;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        List<Product> productList = productDAO.getAllProducts(page, pageSize);
        int totalProducts = productDAO.getProductCount();
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        request.setAttribute("productList", productList);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalProducts", totalProducts);

        request.getRequestDispatcher("account-marketplace-products.jsp").forward(request, response);
    }
}