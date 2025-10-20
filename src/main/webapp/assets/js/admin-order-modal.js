/**
 * This script handles fetching data for the ADMIN order details modal.
 * It populates the modal structure in account-marketplace-sales.jsp
 */
document.addEventListener('DOMContentLoaded', () => {

    // --- START: DYNAMIC CONTEXT PATH LOGIC (Robust Version) ---
    // This function finds the app's base path (e.g., "" or "/YourApp")
    // by reading the 'action' URL from the search form, which is
    // guaranteed to be correctly set by the JSP.
    const getDynamicContextPath = () => {
        try {
            // 1. Find the admin search form on the page.
            const searchForm = document.querySelector('form[action*="/admin-sales"]');

            if (!searchForm) {
                console.error("Critical: Could not find search form 'form[action*=\"/admin-sales\"]' to determine context path. Assuming root.");
                return "";
            }

            // 2. Get its full 'action' URL (e.g., "http://server:port/YourApp/admin-sales")
            // By creating a URL object, we let the browser resolve the full path.
            const actionUrl = new URL(searchForm.action, window.location.origin);

            // 3. Get the pathname (e.g., "/YourApp/admin-sales")
            const pathname = actionUrl.pathname;

            // 4. Remove the known part ("/admin-sales") to get the base path.
            // lastIndexOf ensures we only cut from the end.
            const lastIndex = pathname.lastIndexOf('/admin-sales');
            if (lastIndex === -1) {
                console.error("Critical: Could not parse context path from form action. Assuming root.");
                return "";
            }

            const contextPath = pathname.substring(0, lastIndex);

            // console.log("Dynamic Context Path determined:", contextPath); // For debugging
            return contextPath; // This will be "" (for root) or "/YourApp"

        } catch (e) {
            console.error("Error determining context path, defaulting to root.", e);
            return ""; // Default to root on any error
        }
    };

    // Define the context path ONCE for the whole script to use
    const dynamicContextPath = getDynamicContextPath();
    // --- END: DYNAMIC CONTEXT PATH LOGIC ---


    const orderModal = document.getElementById('orderModal');
    if (!orderModal) {
        // This is normal if the script is loaded on other pages.
        // console.warn("Admin order modal not found on this page.");
        return;
    }

    // Get all the modal's dynamic parts
    const loader = document.getElementById('modal-loader');
    const contentArea = document.getElementById('modal-content-area');
    const orderIdDisplay = document.getElementById('modal-order-id-display');
    const orderIdField = document.getElementById('modal-order-id');
    const productListBody = document.getElementById('modal-product-list');
    const totalAmountField = document.getElementById('modal-total-amount');
    const paymentMethodField = document.getElementById('modal-payment-method');
    const transactionIdField = document.getElementById('modal-transaction-id');
    const shippingAddressField = document.getElementById('modal-shipping-address');

    // Listen for when Bootstrap is *about to show* the modal
    orderModal.addEventListener('show.bs.modal', async (event) => {
        // Get the button that triggered the modal
        const button = event.relatedTarget;

        // Get the integer order ID from the button's data-order-id attribute
        const orderIntId = button.dataset.orderId;

        if (!orderIntId) {
            console.error("No data-order-id found on the modal trigger button.");
            return;
        }


        // 1. Reset the modal: Show loader, hide content
        loader.style.display = 'block';
        contentArea.style.display = 'none';
        orderIdDisplay.textContent = '#...'; // Reset title
        productListBody.innerHTML = ''; // Clear old items

        // 2. Fetch order details from your *existing* servlet
        // Use the dynamicContextPath variable defined above
        const url = `${dynamicContextPath}/get-order-details?id=${orderIntId}`;

        try {
            const response = await fetch(url);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Failed to fetch order details. Status: ${response.status}. ${errorText}`);
            }

            const order = await response.json();

            if (!order) {
                throw new Error(`No order data returned for ID ${orderIntId}`);
            }

            // 3. Populate the modal with the fetched data
            populateModal(order);

        } catch (error) {
            console.error(error);
            // Show an error message inside the modal
            loader.style.display = 'none';
            contentArea.style.display = 'block';
            orderIdDisplay.textContent = 'Error';
            orderIdField.textContent = 'Error loading order details.';
            productListBody.innerHTML = `<tr><td colspan="5" class="text-danger p-3">${error.message}</td></tr>`;
        }
    });

    /**
     * Injects the fetched order data into the modal's HTML
     * @param {object} order - The Order object from the /get-order-details servlet
     */
    function populateModal(order) {
        // --- DOM references ---
        const orderIdDisplay = document.getElementById("modal-order-id-display");
        const orderIdField = document.getElementById("modal-order-id");
        const paymentMethodField = document.getElementById("modal-payment-method");
        const transactionIdField = document.getElementById("modal-transaction-id");
        const totalAmountField = document.getElementById("modal-total-amount");
        const totalAmountSummary = document.getElementById("modal-total-amount-summary");
        const shippingAddressField = document.getElementById("modal-shipping-address");
        const productListBody = document.getElementById("modal-product-list");
        const loader = document.getElementById("modal-loader");
        const contentArea = document.getElementById("modal-content-area");

        // --- Order info ---
        orderIdDisplay.textContent = order.orderId || "N/A";
        if (orderIdField) orderIdField.textContent = order.orderId || "N/A";
        paymentMethodField.textContent = order.paymentMethod || "N/A";
        transactionIdField.textContent = order.transactionId || "N/A";

        // --- Format grand total ---
        const total = new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "INR",
        }).format(order.totalAmount || 0);

        // Update both summary and table footer
        if (totalAmountField) totalAmountField.textContent = total;
        if (totalAmountSummary) totalAmountSummary.textContent = total;

        // --- Shipping address ---
        if (order.address && order.address.address) {
            const ad = order.address;
            shippingAddressField.innerHTML = `
      ${ad.address || ''}<br>
      ${ad.state || ''} ${ad.zipCode || ''}<br>
      ${ad.country || ''}
    `;
        } else {
            shippingAddressField.textContent = "Address not provided for this order.";
        }

        // --- Product list ---
        productListBody.innerHTML = "";

        if (order.items && order.items.length > 0) {
            order.items.forEach(item => {
                const product = item.product;
                const qty = item.quantity || 0;
                const price = item.price || 0;
                const totalItem = qty * price;
                const imagePath = product.thumbnailUrl
                    ? `${dynamicContextPath}/${product.thumbnailUrl}`
                    : `${dynamicContextPath}/assets/img/account/products/01.jpg`;

                const row = `
        <tr>
          <td>${product.id || ''}</td>
          <td>
            <div class="d-flex align-items-center">
              <div style="width:60px; height:60px; overflow:hidden; border-radius:6px; margin-right:10px;">
                <img src="${imagePath}" alt="${product.name}" style="width:100%; height:100%; object-fit:cover;">
              </div>
              <div>
                <a href="${dynamicContextPath}/productDetails?id=${product.id}" 
                   target="_blank" 
                   class="text-decoration-none text-body fw-semibold">
                   ${product.name || ''}
                </a>
              </div>
            </div>
          </td>
          <td class="text-end">${qty}</td>
          <td class="text-end">${price.toFixed(2)}</td>
          <td class="text-end">${totalItem.toFixed(2)}</td>
        </tr>
      `;
                productListBody.insertAdjacentHTML("beforeend", row);
            });
        } else {
            productListBody.innerHTML = `<tr><td colspan="5" class="text-center p-3">No items found for this order.</td></tr>`;
        }

        // --- Show content ---
        loader.style.display = "none";
        contentArea.style.display = "block";
    }

});