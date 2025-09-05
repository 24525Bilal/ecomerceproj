/**
 * Handles the 'Add to cart' button click.
 * Checks if the product is in stock before proceeding.
 */
function addToCart() {
    // Get the stock quantity from the JSP using Expression Language
    var stockQuantity = parseInt('<c:out value="${product.quantity}"/>');

    if (stockQuantity <= 0) {
        // Product is sold out, show an alert
        alert("product soldout\nCheck After Sometimes");
    } else {
        // Product is in stock, proceed to cart page
        window.location.href = "checkout-v1-cart.html";
    }
}