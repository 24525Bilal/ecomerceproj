/**
 * Handles the 'Add to cart' button click.
 * Checks if the product is in stock before proceeding.
 * @param {Event} event The click event.
 * @param {boolean} shouldRedirect - True to redirect to the cart page, false otherwise.
 */
function handleAddToCart(event, shouldRedirect) {
    const button = event.currentTarget;
    const productId = button.getAttribute('data-product-id');

    // --- Stock Checking Logic (Keep as is) ---
    const buttonStock = button.getAttribute('data-product-stock');
    const totalStockElement = document.getElementById('stockQuantity');
    let totalStock;
    if (buttonStock) {
        totalStock = parseInt(buttonStock);
    } else if (totalStockElement) {
        totalStock = parseInt(totalStockElement.value);
    } else {
        console.warn('Could not determine stock for product ' + productId);
        alert("Could not verify stock. Please try again from the product page.");
        return;
    }
    const selectedQuantityElement = document.getElementById('productQuantity');
    const selectedQuantity = selectedQuantityElement ? parseInt(selectedQuantityElement.value) : 1;

    // Client-side stock validation (Keep as is)
    // This part still works for stock = 0
    if (totalStock <= 0) {
        alert("Product Sold Out\nCheck later");
        return;
    } else if (selectedQuantity > totalStock) {
        // This client-side check also still works
        alert("Cannot add " + selectedQuantity + " items. Only " + totalStock + " available.");
        return;
    }
    // --- End Stock Checking ---


    // --- ALWAYS USE FETCH ---
    fetch('addProductToCart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}&quantity=${selectedQuantity}&redirect=false`
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
                return Promise.reject(new Error('Redirecting...'));
            }

            if (!response.ok) { // Check if response status is 2xx
                // Try to parse error JSON from server.
                // If this fails, it will now go directly to the outer catch.
                return response.json().then(errorData => {
                    // Throw error with the specific message from the server
                    throw new Error(errorData.message || 'Error adding product to cart.');
                });
                // --- REMOVED THE INNER CATCH BLOCK ---
            }

            // Response is OK (2xx), parse success JSON (cart items)
            return response.json();
        })
        .then(cartItems => {
            // --- SUCCESS ---
            if (cartItems) {
                updateCartUI(cartItems);
            }

            if (shouldRedirect) {
                window.location.href = 'checkout-v1-cart.jsp';
            } else {
                alert("Product added to cart!");
            }
        })
        .catch(error => {
            // --- OUTER CATCH (Handles all errors now) ---
            if (error.message === 'Redirecting...') {
                return; // Don't show alert for expected redirects
            }
            console.error('There has been a problem with your fetch operation:', error);

            // This will show EITHER the specific server message (if JSON parsing succeeded)
            // OR a generic parsing error (if JSON parsing failed because server sent HTML).
            alert(error.message);
        });
    // --- END FETCH LOGIC ---
}

/**
 * Updates the cart UI with new data.
 * @param {Array} cartItems - An array of cart item objects from the server.
 */
function updateCartUI(cartItems) {
    const cartOffcanvasBody = document.querySelector('#shoppingCart .offcanvas-body');
    const cartBadge = document.querySelector('.navbar-brand .badge, [data-bs-target="#shoppingCart"] .badge');
    const subtotalElement = document.querySelector('#shoppingCart .h6.mb-0');

    if (!cartOffcanvasBody || !cartBadge || !subtotalElement) {
        console.error("Cart UI elements not found.");
        return;
    }

    cartBadge.textContent = cartItems.length;

    let subtotal = 0;
    let cartHtml = '';

    cartItems.forEach(item => {
        subtotal += item.product.price * item.quantity;
        cartHtml += `
            <div class="d-flex align-items-center">
                <a class="flex-shrink-0" href="productDetails?id=${item.product.id}">
                    <img src="${item.product.thumbnailUrl}" width="110" alt="${item.product.name}">
                </a>
                <div class="w-100 min-w-0 ps-2 ps-sm-3">
                    <h5 class="d-flex animate-underline mb-2">
                        <a class="d-block fs-sm fw-medium text-truncate animate-target" href="productDetails?id=${item.product.id}">${item.product.name}</a>
                    </h5>
                    <div class="h6 pb-1 mb-2">$${item.product.price.toFixed(2)}</div>
                    <div class="d-flex align-items-center justify-content-between">
                        <div class="count-input rounded-2">
                            <button type="button" class="btn btn-icon btn-sm" data-decrement="" aria-label="Decrement quantity">
                                <i class="ci-minus"></i>
                            </button>
                            <input type="number" class="form-control form-control-sm" value="${item.quantity}" readonly="">
                            <button type="button" class="btn btn-icon btn-sm" data-increment="" aria-label="Increment quantity">
                                <i class="ci-plus"></i>
                            </button>
                        </div>
                        <form action="removeFromCart" method="post" class="d-inline">
                            <input type="hidden" name="productId" value="${item.product.id}">
                            <button type="submit" class="btn-close ms-auto" data-bs-toggle="tooltip" data-bs-custom-class="tooltip-sm" data-bs-title="Remove from cart" aria-label="Remove from cart"></button>
                        </form>
                    </div>
                </div>
            </div>
        `;
    });

    cartOffcanvasBody.innerHTML = cartHtml;
    subtotalElement.textContent = `$${subtotal.toFixed(2)}`;
}

// --- EDITED: Removed extra closing brace } that was at the end of your file ---