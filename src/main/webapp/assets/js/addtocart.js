/**
 * Handles the 'Add to cart' button click.
 * Checks if the product is in stock before proceeding.
 */
function addToCart() {
    // Get the total stock quantity from the hidden input field
    var totalStock = parseInt(document.getElementById('stockQuantity').value);

    // Get the quantity the user selected
    var selectedQuantity = parseInt(document.getElementById('productQuantity').value);

    if (totalStock <= 0) {
        // Product is sold out, show an alert
        alert("Product Sold Out\nCheck later");
    } else if (selectedQuantity > totalStock) {
        // Not enough stock for the selected quantity
        alert("Cannot add " + selectedQuantity + " items. Only " + totalStock + " available.");
    } else {
        // Product is in stock and selected quantity is valid, proceed to cart page
        window.location.href = "checkout-v1-cart.html";
    }
}