document.addEventListener('DOMContentLoaded', function() {

  // --- Central list of statuses ---
  // These values MUST match the logic in your account-orders.jsp
  const orderStatuses = [
    { value: 'pending', text: 'Pending' },
    { value: 'packed', text: 'Packed' },
    { value: 'shipped', text: 'Shipped' },
    { value: 'out_for_delivery', text: 'Out for Delivery' },
    { value: 'delivered', text: 'Delivered' },
    { value: 'canceled', text: 'Canceled' },
    { value: 'delayed', text: 'Delayed' }
    // Add any other statuses you use
  ];
  // ----------------------------------------------------

  const statusDropdowns = document.querySelectorAll('.status-select');

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
      updateOrderStatus(orderIntId, newStatus, tableRow, event.target);
    });
  });

  /**
   * Sends the status update to the /admin/update-status servlet via AJAX (Fetch)
   * @param {number} orderId - The integer primary key (id) of the order
   * @param {string} status - The new status value (e.g., "shipped")
   * @param {HTMLElement} tableRow - The <tr> element to update its data-status
   * @param {HTMLElement} dropdownElement - The <select> element (to show feedback)
   */
  async function updateOrderStatus(orderId, status, tableRow, dropdownElement) {

    // Get context path (if your app is not at the root, e.g., "/BuyHive")
    const contextPath = ""; // Assuming root.
    const url = `${contextPath}/admin/update-status`;

    const payload = {
      orderId: orderId, // This is the integer ID
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
        // Optional: revert the dropdown if the update failed
        // dropdownElement.value = tableRow.dataset.status;
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