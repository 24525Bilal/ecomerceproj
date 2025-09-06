
//  Clears the shopping cart by sending an asynchronous POST request.
 // @param {Event} event - The click event from the button.

function clearCart(event) {
    event.preventDefault(); // Prevents the default action of the link

    // Use the Fetch API to send a POST request
    fetch('clearCart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        // The body can be empty as the servlet doesn't need any data for this action
        body: JSON.stringify({})
    })
        .then(response => {
            if (response.ok) {
                // Redirect to the cart page after the cart is cleared
                window.location.href = 'cartPage';
            } else {
                // Handle error response, e.g., show an alert
                alert('Failed to clear cart.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while trying to clear the cart.');
        });
}