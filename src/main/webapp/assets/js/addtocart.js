/**
 * Handles the 'Add to cart' button click.
 * Checks if the product is in stock before proceeding.
 * @param {Event} event The click event.
 * @param {boolean} shouldRedirect - True to redirect to the cart page, false otherwise.
 */
function handleAddToCart(event, shouldRedirect) {
    const productId = event.currentTarget.getAttribute('data-product-id');

    var totalStockElement = document.getElementById('stockQuantity');
    var totalStock = totalStockElement ? parseInt(totalStockElement.value) : 9999;

    var selectedQuantityElement = document.getElementById('productQuantity');
    var selectedQuantity = selectedQuantityElement ? parseInt(selectedQuantityElement.value) : 1;

    if (totalStock <= 0) {
        alert("Product Sold Out\nCheck later");
        return;
    } else if (selectedQuantity > totalStock) {
        alert("Cannot add " + selectedQuantity + " items. Only " + totalStock + " available.");
        return;
    }

    if (shouldRedirect) {
        const form = document.createElement('form');
        form.setAttribute('method', 'POST');
        form.setAttribute('action', 'addProductToCart');

        const productIdInput = document.createElement('input');
        productIdInput.setAttribute('type', 'hidden');
        productIdInput.setAttribute('name', 'productId');
        productIdInput.setAttribute('value', productId);
        form.appendChild(productIdInput);

        const quantityInput = document.createElement('input');
        quantityInput.setAttribute('type', 'hidden');
        quantityInput.setAttribute('name', 'quantity');
        quantityInput.setAttribute('value', selectedQuantity);
        form.appendChild(quantityInput);

        const redirectInput = document.createElement('input');
        redirectInput.setAttribute('type', 'hidden');
        redirectInput.setAttribute('name', 'redirect');
        redirectInput.setAttribute('value', 'true');
        form.appendChild(redirectInput);

        document.body.appendChild(form);
        form.submit();
    } else {
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
                    return;
                }
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(cartItems => {
                if (cartItems) {
                    updateCartUI(cartItems);
                }
            })
            .catch(error => {
                console.error('There has been a problem with your fetch operation:', error);
                alert("Error adding product to cart.");
            });
    }
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