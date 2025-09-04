// for dopost request for deleteting product from admin products


document.addEventListener('DOMContentLoaded', function () {
    const deleteButtons = document.querySelectorAll('.delete-btn');

    deleteButtons.forEach(button => {
        button.addEventListener('click', function () {
            if (confirm('Are you sure you want to delete this product?')) {
                const productId = this.getAttribute('data-product-id');

                var form = document.createElement('form');
                form.setAttribute('method', 'post');
                form.setAttribute('action', 'deleteProduct');

                var hiddenField = document.createElement('input');
                hiddenField.setAttribute('type', 'hidden');
                hiddenField.setAttribute('name', 'productId');
                hiddenField.setAttribute('value', productId);

                form.appendChild(hiddenField);
                document.body.appendChild(form);
                form.submit();
            }
        });
    });
});