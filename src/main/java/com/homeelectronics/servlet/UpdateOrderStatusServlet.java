package com.homeelectronics.servlet;

import com.homeelectronics.dao.OrderDAO;
import com.google.gson.Gson; // You must have the Gson library in your WEB-INF/lib

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

// We'll map this to /admin/update-status.
@WebServlet("/admin/update-status")
public class UpdateOrderStatusServlet extends HttpServlet {

    private Gson gson = new Gson();

    /**
     * Handles the POST request to update an order's status.
     * Expects a JSON payload like: {"orderId": 123, "status": "shipped"}
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read the JSON payload from the request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Parse the JSON string into a Map
            // We expect the JavaScript to send the INTEGER ID ('id'), not the string 'order_id'
            Map<String, Object> payload = gson.fromJson(sb.toString(), Map.class);

            // Extract data. GSON parses numbers as Double, so we must cast carefully.
            int orderIntId = ((Double) payload.get("orderId")).intValue();
            String newStatus = (String) payload.get("status");

            if (newStatus == null || newStatus.trim().isEmpty()) {
                throw new IllegalArgumentException("Status cannot be empty.");
            }

            // --- Update the Database ---
            OrderDAO orderDAO = new OrderDAO();
            boolean success = orderDAO.updateOrderStatus(orderIntId, newStatus);

            // Send a JSON response back to the JavaScript
            if (success) {
                // HTTP 200 OK
                response.getWriter().print("{\"success\": true, \"message\": \"Status updated successfully.\"}");
            } else {
                // HTTP 404 Not Found (or 400 Bad Request)
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"success\": false, \"message\": \"Order not found or status not changed.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // HTTP 400 Bad Request for parsing errors or other exceptions
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"success\": false, \"message\": \"Error updating status: " + e.getMessage() + "\"}");
        }
    }
}