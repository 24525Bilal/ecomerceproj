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

import jakarta.servlet.http.HttpSession; // Import HttpSession


// Fetches Products for Display
//  It gets the products for the current page and sends them to the JSP.
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private static final int PRODUCTS_PER_PAGE = 18;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                // Keep page = 1 if the parameter is not a valid number
            }
        }

        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getProducts(page, PRODUCTS_PER_PAGE);
        int totalProducts = productDAO.getProductCount();
        int totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);

        request.setAttribute("products", products);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);



        request.getRequestDispatcher("shop-catalog-electronics.jsp").forward(request, response);
    }
}