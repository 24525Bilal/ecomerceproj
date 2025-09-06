package com.homeelectronics.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.homeelectronics.dao.ProductDAO;
import com.homeelectronics.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/trending-products")
public class TrendingProductsServlet extends HttpServlet {
    private static final int TRENDING_PRODUCT_COUNT = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ProductDAO productDAO = new ProductDAO();
        List<Product> products = productDAO.getLatestProducts(TRENDING_PRODUCT_COUNT);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String jsonProducts = gson.toJson(products);

        PrintWriter out = response.getWriter();
        out.print(jsonProducts);
        out.flush();
    }
}