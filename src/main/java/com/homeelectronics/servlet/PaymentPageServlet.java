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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID; // Import for generating IDs

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
        ProductDAO productDAO = new ProductDAO(); // Instantiate ProductDAO

        try {
            // --- Get Session Data ---
            int userId = (int) session.getAttribute("userId");
            Double totalCost = (Double) session.getAttribute("sessionTotalCost");
            Address primaryAddress = (Address) session.getAttribute("primaryAddress");
            // ** Get cart items BEFORE potentially clearing them **
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

            // --- Basic Data Check ---
            if (totalCost == null || primaryAddress == null || cartItems == null || cartItems.isEmpty()) {
                // If cart is empty or essential data missing, redirect to cart
                response.sendRedirect("checkout-v1-cart.jsp");
                return;
            }

            // --- Get & Validate Payment Method ---
            String paymentMethod = request.getParameter("paymentMethod");
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
            boolean paymentSuccessful = false; // Flag for payment success

            // --- Generate Order ID ---
            int lastOrderNum = orderDAO.getLastOrderNumber();
            int nextOrderNum = (lastOrderNum == 0) ? 10001 : lastOrderNum + 1;
            orderId = "ORDNO" + nextOrderNum;

            // --- Process Payment Method ---
            if ("cod".equals(paymentMethod)) {
                paymentMethodString = "Cash on Delivery";
                paymentStatus = "Pending";
                paymentSuccessful = true; // COD is considered successful at this stage
                // transactionId remains null

            } else if ("card".equals(paymentMethod)) {
                paymentMethodString = "Card";
                // --- Card Validation ---
                String cardNumber = request.getParameter("cardNumber");
                String expiryDate = request.getParameter("expiryDate");
                String cvc = request.getParameter("cvc");
                // ... (Keep the detailed card validation logic from the previous step) ...
                String sanitizedCardNumber = (cardNumber != null) ? cardNumber.replaceAll("[^\\d]", "") : "";
                String sanitizedExpiry = (expiryDate != null) ? expiryDate.trim() : "";
                String sanitizedCvc = (cvc != null) ? cvc.replaceAll("[^\\d]", "") : "";
                boolean cardDetailsValid = true;
                String validationError = "Invalid card details. Please check your input.";

                if (sanitizedCardNumber.isEmpty() || !sanitizedCardNumber.matches("\\d{13,19}")) cardDetailsValid = false;
                else if (sanitizedExpiry.isEmpty() || !sanitizedExpiry.matches("\\d{2}/\\d{2}") || sanitizedExpiry.length() != 5) cardDetailsValid = false;
                else if (sanitizedCvc.isEmpty() || !sanitizedCvc.matches("\\d{3}") || sanitizedCvc.length() != 3) cardDetailsValid = false;

                if (!cardDetailsValid) {
                    request.setAttribute("paymentError", validationError);
                    doGet(request, response);
                    return;
                }
                // --- End Validation ---

                // Simulate/Process Payment
                paymentSuccessful = simulatePayment(sanitizedCardNumber, sanitizedExpiry, sanitizedCvc);

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

            // --- If payment seems successful, proceed to create order and update stock ---
            if (paymentSuccessful) {
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

                // --- Create Order in DB ---
                boolean orderCreated = orderDAO.createOrder(newOrder);

                if (orderCreated) {
                    // --- Update Stock Quantity ---
                    boolean stockUpdatedSuccessfully = true; // Assume success initially
                    try {
                        for (CartItem item : cartItems) {
                            int productId = item.getProduct().getId();
                            int quantitySold = item.getQuantity();
                            boolean updated = productDAO.updateStockQuantity(productId, quantitySold);
                            if (!updated) {
                                // Handle failure: Log error, maybe flag the order, etc.
                                System.err.println("Critical Error: Failed to update stock for product ID: " + productId + " for Order ID: " + orderId);
                                stockUpdatedSuccessfully = false;
                                // If using transactions, this error would trigger a rollback in ProductDAO
                                // For now, just log and continue clearing cart etc. but flag the issue.
                            }
                        }
                    } catch (SQLException stockEx) {
                        System.err.println("Critical Error: Database exception during stock update for Order ID: " + orderId);
                        stockEx.printStackTrace();
                        stockUpdatedSuccessfully = false; // Mark as failed
                        // Might need manual intervention or compensation logic here
                    }


                    try {

                        System.out.println("Calling clearCartByUserId for user: " + userId); // For checking logs

                        // clear cart method calling
                        cartDAO.clearCartByUserId(userId);


                        System.out.println("Finished clearing cart for user: " + userId); // For checking logs

                    } catch (SQLException cartEx) {
                        // If clearing the cart database fails, log it!
                        System.err.println("Error clearing cart from DB for user ID: " + userId + " after Order ID: " + orderId);
                        cartEx.printStackTrace();
                        // NOTE: Usually, you STILL continue here because the order is already placed.
                        // You just need to know that the cart wasn't cleared automatically.
                    }


                    // --- Clear Cart from Session ---
                    session.removeAttribute("cartItems");
                    session.removeAttribute("sessionCartSubtotal");
                    session.removeAttribute("sessionShippingCost");
                    session.removeAttribute("sessionTotalCost");
                    // Add any other cart-related session attributes you might have

                    // Store orderId in session for thank you page
                    session.setAttribute("latestOrderId", orderId);

                    // Redirect to Thank You page
                    response.sendRedirect("checkout-v1-thankyou.html");

                } else {
                    // Order creation failed (DB issue)
                    request.setAttribute("paymentError", "Could not save your order after payment. Please contact support.");
                    // Note: Payment might have been processed but order not saved. Needs careful handling.
                    doGet(request, response);
                }
            } else {
                // This case should ideally be caught earlier (e.g., card payment failed)
                // If COD somehow fails this check, handle appropriately.
                request.setAttribute("paymentError", "Payment processing failed.");
                doGet(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("paymentError", "A database error occurred during payment processing.");
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("paymentError", "An unexpected error occurred during payment processing.");
            doGet(request, response);
        }
    }

    // ... (simulatePayment method remains the same) ...
    private boolean simulatePayment(String sanitizedCardNumber, String sanitizedExpiry, String sanitizedCvc) {
        System.out.println("Simulating payment for Card: ****" + sanitizedCardNumber.substring(sanitizedCardNumber.length()-4)
                + ", Expiry: " + sanitizedExpiry
                + ", CVC: ***");
        return true;
    }
}