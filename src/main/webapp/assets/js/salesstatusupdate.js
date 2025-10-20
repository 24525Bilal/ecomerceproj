document.addEventListener('DOMContentLoaded', function() {

  // --- Central list of statuses ---
  const orderStatuses = [
    { value: 'pending', text: 'Pending' },
    { value: 'packed', text: 'Packed' },
    { value: 'shipped', text: 'Shipped' },
    { value: 'out_for_delivery', text: 'Out for Delivery' },
    { value: 'delivered', text: 'Delivered' },
    { value: 'canceled', text: 'Canceled' },
    { value: 'delayed', text: 'Delayed' }
  ];
  // ----------------------------------------------------

  const statusDropdowns = document.querySelectorAll('.status-select');

  // --- FIX: Get the context path from the <tbody> data attribute ---
  const productListBody = document.querySelector('.product-list');
  const contextPath = productListBody ? productListBody.dataset.contextPath : "";
  // -----------------------------------------------------------------

  statusDropdowns.forEach(dropdown => {
    // Get the current status from the parent <tr>'s 'data-status' attribute
    const currentStatus = dropdown.closest('tr').dataset.status;

    // Get the order's INTEGER ID from the dropdown's data attribute
    const orderIntId = dropdown.dataset.orderIntId;

    // Populate the dropdown with all possible statuses
    orderStatuses.forEach(status => {
      const option = document.createElement('option');
      option.value = status.value;
      option.textContent = status.text;

      // If this option matches the order's current status, select it
      if (status.value === currentStatus) {
        option.selected = true;
      }

      dropdown.appendChild(option);
    });

    // --- ADD EVENT LISTENER FOR CHANGES ---
    dropdown.addEventListener('change', function(event) {
      const newStatus = event.target.value;

      // Find the parent <tr> to update its data-status attribute later
      const tableRow = event.target.closest('tr');

      console.log(`Sending update for Order (ID: ${orderIntId}). New status: ${newStatus}`);

      // Call the function to send the update to the server
      // --- FIX: Pass the contextPath to the function ---
      updateOrderStatus(orderIntId, newStatus, tableRow, event.target, contextPath);
    });
  });

  /**
   * Sends the status update to the /admin/update-status servlet via AJAX (Fetch)
   * @param {string} orderId - The integer primary key (id) of the order (from dataset)
   * @param {string} status - The new status value (e.g., "shipped")
   * @param {HTMLElement} tableRow - The <tr> element to update its data-status
   * @param {HTMLElement} dropdownElement - The <select> element (to show feedback)
   * @param {string} contextPath - The application's base URL (e.g., "/ecomerceproj")
   */
  async function updateOrderStatus(orderId, status, tableRow, dropdownElement, contextPath) {

    // --- FIX: Build the URL with the correct contextPath ---
    const url = `${contextPath}/admin/update-status`;
    // -----------------------------------------------------

    const payload = {
      orderId: Number(orderId), // Convert string ID from dataset to a Number
      status: status
    };

    // Visual feedback: disable dropdown while processing
    dropdownElement.disabled = true;
    dropdownElement.style.border = "1px solid #2f6ed5"; // Blue "processing"

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      // Check for 404s, 500s, etc.
      if (!response.ok) {
        throw new Error(`Request failed: ${response.status} ${response.statusText}`);
      }

      const result = await response.json();

      if (response.ok && result.success) {
        console.log('Update successful:', result.message);
        // Update the <tr> data-status attribute to match the new status
        tableRow.dataset.status = status;
        // Visual feedback for success
        dropdownElement.style.border = "1px solid #33b36b"; // Green
      } else {
        console.error('Update failed:', result.message);
        // Visual feedback for error
        dropdownElement.style.border = "1px solid #f03d3d"; // Red
      }

    } catch (error) {
      console.error('Error sending update request:', error);
      dropdownElement.style.border = "1px solid #f03d3d"; // Red
    } finally {
      // Re-enable dropdown and remove temp border after a moment
      setTimeout(() => {
        dropdownElement.disabled = false;
        dropdownElement.style.border = "1px solid #444"; // Reset to original
      }, 1500);
    }
  }
});