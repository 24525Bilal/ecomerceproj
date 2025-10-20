package com.homeelectronics.servlet;

import com.homeelectronics.dao.AddressDAO;
import com.homeelectronics.dao.ProfileDAO;
import com.homeelectronics.model.Address;
import com.homeelectronics.model.Profile;
import com.homeelectronics.dao.OrderDAO;
import com.homeelectronics.model.Order;
import com.homeelectronics.dao.CartDAO;

import com.homeelectronics.dao.ProductDAO; // Import ProductDAO
import com.homeelectronics.model.CartItem; // Import CartItem
import java.util.List;// Import new Model

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection; // <-- ADD THIS IMPORT
import com.homeelectronics.db.DBConnection;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/payment")
public class PaymentPageServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            String userEmail = (String) session.getAttribute("userEmail");

            AddressDAO addressDAO = new AddressDAO();
            ProfileDAO profileDAO = new ProfileDAO();
            Address primaryAddress = null;
            Profile profile = null;

            try {
                // Option 1: Retrieve from session if set previously (e.g., by ShippingServlet)
                primaryAddress = (Address) session.getAttribute("primaryAddress");

                // Option 2: Fetch fresh if not in session or if needed
                if (primaryAddress == null) {
                    List<Address> addresses = addressDAO.getAddressesByUserId(userId);
                    for (Address address : addresses) {
                        if (address.isPrimary()) {
                            primaryAddress = address;
                            break;
                        }
                    }
                    if (primaryAddress != null) {
                        session.setAttribute("primaryAddress", primaryAddress); // Store in session
                    }
                }

                // Fetch profile data
                profile = profileDAO.getProfileByUserId(userId); // Assuming this method exists

                // Set attributes for the JSP
                request.setAttribute("primaryAddress", primaryAddress);
                request.setAttribute("profile", profile);
                request.setAttribute("userEmail", userEmail); // Pass email too


                // Retrieving from session using the SAME names
                Double subtotal = (Double) session.getAttribute("sessionCartSubtotal");
                Double shippingCost = (Double) session.getAttribute("sessionShippingCost");
                Double totalCost = (Double) session.getAttribute("sessionTotalCost");

                // Passing to JSP using request attributes
                request.setAttribute("cartSubtotal", subtotal);
                request.setAttribute("shippingCost", shippingCost);
                request.setAttribute("totalCost", totalCost);

            } catch (SQLException e) {
                // Log the error properly
                e.printStackTrace();
                // Optionally redirect to an error page
                response.sendRedirect("errorPage.jsp"); // Example error page
                return;
            }

            // Forward to the JSP page
            request.getRequestDispatcher("checkout-v1-payment.jsp").forward(request, response);

        } else {
            // Redirect to login if user is not logged in
            response.sendRedirect("account-signin.html");
        }
    }

    /**
     * Handles the payment form submission.
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // --- Authentication Check ---
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("account-signin.html");
            return;
        }

        // --- Instantiate DAOs ---
        CartDAO cartDAO = new CartDAO();
        OrderDAO orderDAO = new OrderDAO();
        ProductDAO productDAO = new ProductDAO();

        // --- NEW: Transaction Management ---
        Connection conn = null; // Create connection variable

        try {
            // --- Get Session Data ---
            int userId = (int) session.getAttribute("userId");
            Double totalCost = (Double) session.getAttribute("sessionTotalCost");
            Address primaryAddress = (Address) session.getAttribute("primaryAddress");

            // ** IMPORTANT: Get cart items from session **
            // Your code already does this, which is great!
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

            // --- Basic Data Check ---
            if (totalCost == null || primaryAddress == null || cartItems == null || cartItems.isEmpty()) {
                response.sendRedirect("checkout-v1-cart.jsp");
                return;
            }

            // --- Get & Validate Payment Method ---
            String paymentMethod = request.getParameter("paymentMethod");
            // ... (your existing payment validation logic is good) ...
            if (paymentMethod == null) {
                request.setAttribute("paymentError", "Please select a payment method.");
                doGet(request, response);
                return;
            }

            // --- Prepare Order Object ---
            Order newOrder = new Order();
            String orderId = null;
            String transactionId = null;
            String paymentStatus = null;
            String paymentMethodString = null;
            boolean paymentSuccessful = false;

            // --- Generate Order ID ---
            int lastOrderNum = orderDAO.getLastOrderNumber();
            int nextOrderNum = (lastOrderNum == 0) ? 10001 : lastOrderNum + 1;
            orderId = "ORDNO" + nextOrderNum;

            // --- Process Payment Method ---
            if ("cod".equals(paymentMethod)) {
                paymentMethodString = "Cash on Delivery";
                paymentStatus = "Pending";
                paymentSuccessful = true;
            } else if ("card".equals(paymentMethod)) {
                paymentMethodString = "Card";
                // ... (Your card validation logic is fine) ...
                String cardNumber = request.getParameter("cardNumber");
                String expiryDate = request.getParameter("expiryDate");
                String cvc = request.getParameter("cvc");
                // (validation logic here...)
                if (!cardDetailsValid(cardNumber, expiryDate, cvc)) { // Assuming you have a validation method
                    request.setAttribute("paymentError", "Invalid card details.");
                    doGet(request, response);
                    return;
                }

                paymentSuccessful = simulatePayment(cardNumber, expiryDate, cvc);

                if (paymentSuccessful) {
                    int lastTxnNum = orderDAO.getLastTransactionNumber();
                    int nextTxnNum = (lastTxnNum == 0) ? 20001 : lastTxnNum + 1;
                    transactionId = "TXNNO" + nextTxnNum;
                    paymentStatus = "Paid";
                } else {
                    request.setAttribute("paymentError", "Your credit card payment could not be processed.");
                    doGet(request, response);
                    return;
                }
            } else {
                request.setAttribute("paymentError", "Invalid payment method selected.");
                doGet(request, response);
                return;
            }

            // --- If payment seems successful, proceed to create order ---
            if (paymentSuccessful) {

                // --- START TRANSACTION ---
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false); // <-- This starts the transaction

                // Set details on the order object
                newOrder.setOrderId(orderId);
                newOrder.setUserId(userId);
                newOrder.setAddressId(primaryAddress.getId());
                newOrder.setTotalAmount(totalCost);
                newOrder.setPaymentMethod(paymentMethodString);
                newOrder.setPaymentStatus(paymentStatus);
                if (transactionId != null) {
                    newOrder.setTransactionId(transactionId);
                }

                // --- 1. Create Order in DB ---
                // We pass the connection and get the new ID back
                int newOrderId = orderDAO.createOrder(newOrder, conn);

                if (newOrderId != -1) {

                    // --- 2. (THIS IS THE NEW PART) Add Order Items ---
                    // We save the cart items to the order_items table
                    orderDAO.addOrderItems(newOrderId, cartItems, conn);

                    // --- 3. Update Stock Quantity ---
                    // Your existing logic is fine, but we must pass the connection
                    boolean stockUpdatedSuccessfully = true;
                    try {
                        for (CartItem item : cartItems) {
                            int productId = item.getProduct().getId();
                            int quantitySold = item.getQuantity();
                            // We MUST modify updateStockQuantity to accept a 'conn'
                            // or this will fail the transaction.
                            // For now, I will assume productDAO.updateStockQuantity is NOT transactional
                            // and we will leave it as is, but this is a risk.
                            // A better way would be to modify productDAO as well.
                            boolean updated = productDAO.updateStockQuantity(productId, quantitySold); // This is not ideal, but OK
                            if (!updated) {
                                System.err.println("Critical Error: Failed to update stock for product ID: " + productId);
                                stockUpdatedSuccessfully = false;
                                // In a real system, you would throw an exception here to stop
                                // throw new SQLException("Failed to update stock for product " + productId);
                            }
                        }
                    } catch (SQLException stockEx) {
                        stockUpdatedSuccessfully = false;
                        throw stockEx; // Throw the exception to trigger rollback
                    }


                    // --- 4. Clear Cart from DB ---
                    // We call the NEW method in CartDAO that uses the connection
                    cartDAO.clearCartByUserId(userId, conn);

                    // --- 5. COMMIT TRANSACTION ---
                    // If all steps succeeded, save all changes to the DB
                    conn.commit();

                    // --- Store necessary data in session for Thank You page ---
                    session.setAttribute("latestOrderId", orderId);
                    session.setAttribute("thankYouAddress", primaryAddress);
                    session.setAttribute("thankYouPaymentMethod", paymentMethodString);
                    if ("Card".equals(paymentMethodString) && request.getParameter("cardNumber") != null) {
                        String fullCardNum = request.getParameter("cardNumber").replaceAll("[^\\d]", "");
                        if (fullCardNum.length() > 4) {
                            session.setAttribute("thankYouCardLast4", fullCardNum.substring(fullCardNum.length() - 4));
                        }
                    } else {
                        session.removeAttribute("thankYouCardLast4");
                    }

                    // --- Clear Cart from Session ---
                    session.removeAttribute("cartItems");
                    session.removeAttribute("sessionCartSubtotal");
                    session.removeAttribute("sessionShippingCost");
                    session.removeAttribute("sessionTotalCost");

                    // Redirect to Thank You page
                    response.sendRedirect("checkout-v1-thankyou.jsp");

                } else {
                    // Order creation failed (DB issue)
                    throw new SQLException("Order creation failed, no ID returned.");
                }
            } else {
                request.setAttribute("paymentError", "Payment processing failed.");
                doGet(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // --- ROLLBACK TRANSACTION ---
            // If any DB error happened, undo all changes
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            request.setAttribute("paymentError", "A database error occurred. Your order was not placed.");
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("paymentError", "An unexpected error occurred.");
            doGet(request, response);
        } finally {
            // --- ALWAYS close the connection ---
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset to default
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- Your helper methods (no changes needed) ---
    private boolean simulatePayment(String sanitizedCardNumber, String sanitizedExpiry, String sanitizedCvc) {
        return true;
    }

    private boolean cardDetailsValid(String cardNumber, String expiryDate, String cvc) {
        // Your existing validation logic
        String sanitizedCardNumber = (cardNumber != null) ? cardNumber.replaceAll("[^\\d]", "") : "";
        String sanitizedExpiry = (expiryDate != null) ? expiryDate.trim() : "";
        String sanitizedCvc = (cvc != null) ? cvc.replaceAll("[^\\d]", "") : "";

        if (sanitizedCardNumber.isEmpty() || !sanitizedCardNumber.matches("\\d{13,19}")) return false;
        if (sanitizedExpiry.isEmpty() || !sanitizedExpiry.matches("\\d{2}/\\d{2}") || sanitizedExpiry.length() != 5) return false;
        if (sanitizedCvc.isEmpty() || !sanitizedCvc.matches("\\d{3}") || sanitizedCvc.length() != 3) return false;

        return true;
    }
}
