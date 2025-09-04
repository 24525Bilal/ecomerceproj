
// this js is for data showing int the edit product form in the admin product page

document.addEventListener('DOMContentLoaded', function () {
    const editButtons = document.querySelectorAll('.edit-btn');

    editButtons.forEach(button => {
        button.addEventListener('click', function () {
            const productId = this.getAttribute('data-product-id');

            fetch(`getProductDetails?id=${productId}`)
                .then(response => response.json())
                .then(product => {
                    if (!product) {
                        console.error('Product not found');
                        return;
                    }
                    // Populate all form fields in the modal
                    document.getElementById('editProductId').value = product.id;
                    document.getElementById('editProductName').value = product.name || '';
                    document.getElementById('editProductDescription').value = product.description || '';
                    document.getElementById('editPrice').value = product.price || '';
                    document.getElementById('editCategory').value = product.category || '';
                    document.getElementById('editStockQuantity').value = product.stockQuantity || '';
                    document.getElementById('editTags').value = product.tags || '';
                    document.getElementById('editColor').value = product.color || '';
                    document.getElementById('editSize').value = product.size || '';
                    document.getElementById('editModel').value = product.model || '';
                    document.getElementById('editManufacturer').value = product.manufacturer || '';
                    document.getElementById('editFinish').value = product.finish || '';
                    document.getElementById('editCapacity').value = product.capacity || '';
                    document.getElementById('editChip').value = product.chip || '';
                    document.getElementById('editDiagonal').value = product.diagonal || '';
                    document.getElementById('editScreenType').value = product.screenType || '';
                    document.getElementById('editResolution').value = product.resolution || '';
                    document.getElementById('editRefreshRate').value = product.refreshRate || '';
                })
                .catch(error => console.error('Error fetching product details:', error));
        });
    });
});