/**
 * Handles the 'Add to cart' button click.
 * Checks if the product is in stock before proceeding.
 */
function addToCart(event) {
    // Get the total stock quantity from the hidden input field
    var totalStock = parseInt(document.getElementById('stockQuantity').value);

    // Get the quantity the user selected from the count input field
    var selectedQuantity = parseInt(document.getElementById('productQuantity').value);

    // Get the product ID from the clicked button's data attribute

    if (totalStock <= 0) {
        // Product is sold out, show an alert
        alert("Product Sold Out\nCheck later");
    } else if (selectedQuantity > totalStock) {
        // Not enough stock for the selected quantity
        alert("Cannot add " + selectedQuantity + " items. Only " + totalStock + " available.");
    } else {

        const productId = event.currentTarget.getAttribute('data-product-id');

        // Product is in stock and selected quantity is valid, create a form and submit
        const form = document.createElement('form');
        form.setAttribute('method', 'POST');
        form.setAttribute('action', 'addProductToCart'); // The servlet URL

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

        document.body.appendChild(form);
        form.submit();
    }
}