package com.homeelectronics.servlet;

import com.homeelectronics.dao.OrderDAO;
import com.homeelectronics.model.Order;
import com.google.gson.Gson; // <-- Import Gson

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/get-order-details")
public class OrderDetailsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // --- ADD LOGGING ---
        System.out.println("--- OrderDetailsServlet: doGet() CALLED ---");
        String orderIdParam = request.getParameter("id");
        System.out.println("OrderDetailsServlet: Received id parameter: " + orderIdParam);
        // --- END LOGGING --

        try {
            // 1. Get the integer 'id' from the request
            int orderId = Integer.parseInt(request.getParameter("id"));

            // 2. Fetch the complete order data from the DAO
            OrderDAO orderDAO = new OrderDAO();
            Order order = orderDAO.getOrderDetailsById(orderId);

            // 3. Convert the Java 'Order' object (with its list of items) to a JSON string
            Gson gson = new Gson();
            String orderJson = gson.toJson(order);

            // 4. Send the JSON string as the response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(orderJson);
            out.flush();

        } catch (Exception e) {
            // Send a clear error response in JSON format
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"error\": \"Failed to retrieve order details. " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}