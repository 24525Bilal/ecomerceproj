// JavaScript to automatically submit the form whenever a increase or decrease button is clicked.

document.addEventListener('DOMContentLoaded', function() {
    const cartForms = document.querySelectorAll('.count-input-form');

    cartForms.forEach(form => {
        const incrementBtn = form.querySelector('[data-increment]');
        const decrementBtn = form.querySelector('[data-decrement]');
        const quantityInput = form.querySelector('input[name="quantity"]');

        const updateCart = () => {
            const productId = incrementBtn.dataset.productId;
            const newQuantity = quantityInput.value;

            fetch('updateCart', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `productId=${productId}&quantity=${newQuantity}`
            })
                .then(response => {
                    if (response.ok) {
                        window.location.reload(); // Reload the page to show updated totals
                    } else {
                        alert('Failed to update cart.');
                    }
                })
                .catch(error => console.error('Error:', error));
        };

        incrementBtn.addEventListener('click', () => {
            quantityInput.value = parseInt(quantityInput.value) + 1;
            updateCart();
        });

        decrementBtn.addEventListener('click', () => {
            if (parseInt(quantityInput.value) > 1) {
                quantityInput.value = parseInt(quantityInput.value) - 1;
                updateCart();
            } else {
                updateCart(); // If quantity is 1 and decremented, remove item
            }
        });
    });
});