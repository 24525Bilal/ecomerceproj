// Wait for the entire page to load before running the script
document.addEventListener('DOMContentLoaded', () => {

    // Get references to all the dynamic elements
    const orderDetailsOffcanvas = document.getElementById('orderDetails');
    const orderIdElement = document.getElementById('offcanvas-order-id');
    const orderStatusElement = document.getElementById('offcanvas-order-status');
    const itemListElement = document.getElementById('offcanvas-item-list');
    const paymentMethodElement = document.getElementById('offcanvas-payment-method');
    const totalElement = document.getElementById('offcanvas-total');
    const loaderElement = document.getElementById('offcanvas-loader');

    // Get the base URL (context path) from the script tag's data attribute
    // This is how we find our servlet from an external JS file.
    const scriptTag = document.querySelector('script[src*="account-orders.js"]');
    const contextPath = scriptTag.dataset.contextPath || '';

    // Helper function to format currency as Rupees (since your page uses ₹)
    const currencyFormatter = new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    // Make sure the offcanvas element exists before adding a listener
    if (orderDetailsOffcanvas) {

        // Listen for the Bootstrap event *before* the offcanvas is shown
        orderDetailsOffcanvas.addEventListener('show.bs.offcanvas', async function (event) {

            // Get the button that triggered the offcanvas
            const button = event.relatedTarget;

            // Get the order's integer ID from our data-order-id attribute
            const orderId = button.getAttribute('data-order-id');

            // --- 1. Set a "loading" state ---
            orderIdElement.textContent = '...'; // Clear old ID
            orderStatusElement.innerHTML = '<span class="bg-secondary rounded-circle p-1 me-2"></span> Loading...';
            itemListElement.innerHTML = ''; // Clear old items
            if (loaderElement) loaderElement.style.display = 'flex'; // Show spinner
            totalElement.textContent = '...';
            paymentMethodElement.textContent = '...';

            try {
                // --- 2. Fetch the order details from our new servlet ---
                const response = await fetch(`${contextPath}/get-order-details?id=${orderId}`);

                if (!response.ok) {
                    throw new Error(`Network response was not ok (status: ${response.status})`);
                }

                const order = await response.json(); // This is our full Order object from Java

                if (!order) {
                    throw new Error('Received empty order data.');
                }

                // --- 3. Populate the offcanvas with the new data ---

                // Populate header
                orderIdElement.textContent = order.orderId; // The "ORDNO123" string
                paymentMethodElement.textContent = order.paymentMethod;

                // Set status text and color
                let statusHtml = '';
                let status = order.paymentStatus || 'Pending'; // Default to Pending if null
                let statusLower = status.toLowerCase();

                if (statusLower === 'completed' || statusLower === 'delivered') {
                    statusHtml = `<span class="bg-success rounded-circle p-1 me-2"></span> ${status}`;
                } else if (statusLower === 'pending' || statusLower === 'inprogress') {
                    statusHtml = `<span class="bg-info rounded-circle p-1 me-2"></span> ${status}`;
                } else if (statusLower === 'canceled' || statusLower === 'failed') {
                    statusHtml = `<span class="bg-danger rounded-circle p-1 me-2"></span> ${status}`;
                } else if (statusLower === 'shipped' || statusLower === 'out_for_delivery') {
                    statusHtml = `<span class"bg-warning rounded-circle p-1 me-2"></span> ${status}`;
                } else {
                    statusHtml = `<span class="bg-secondary rounded-circle p-1 me-2"></span> ${status}`;
                }
                orderStatusElement.innerHTML = statusHtml;

                // Populate total
                totalElement.textContent = currencyFormatter.format(order.totalAmount);

                // --- 4. Build the item list HTML ---
                let itemsHtml = '';
                if (order.items && order.items.length > 0) {
                    order.items.forEach(item => {
                        const itemPrice = currencyFormatter.format(item.price);

                        // This HTML is a perfect copy of your static item structure
                        itemsHtml += `
                            <div class="d-flex align-items-center">
                                <a class="flex-shrink-0" href="shop-product-electronics.jsp?id=${item.product.id}">
                                    <img src="${contextPath}/${item.product.thumbnailUrl}" width="110" alt="${item.product.name}">
                                </a>
                                <div class="w-100 min-w-0 ps-2 ps-sm-3">
                                    <h5 class="d-flex animate-underline mb-2">
                                        <a class="d-block fs-sm fw-medium text-truncate animate-target" href="shop-product-electronics.jsp?id=${item.product.id}">
                                            ${item.product.name}
                                        </a>
                                    </h5>
                                    <div class="h6 mb-2">${itemPrice}</div>
                                    <div class="fs-xs">Qty: ${item.quantity}</div>
                                </div>
                            </div>
                        `;
                    });
                } else {
                    itemsHtml = '<p>No items were found for this order.</p>';
                }

                if (loaderElement) loaderElement.style.display = 'none'; // Hide spinner
                itemListElement.innerHTML = itemsHtml; // Set the generated HTML

            } catch (error) {
                console.error('Failed to fetch order details:', error);
                if (loaderElement) loaderElement.style.display = 'none'; // Hide spinner
                itemListElement.innerHTML = '<div class="alert alert-danger">Could not load order details. Please try again.</div>';
            }
        });
    } else {
        console.error('Order details offcanvas element not found.');
    }
});