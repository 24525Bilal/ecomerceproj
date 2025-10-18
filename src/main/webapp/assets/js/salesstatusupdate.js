// Wait for the document to be fully loaded before running the script
document.addEventListener('DOMContentLoaded', function() {
  
  // --- THIS IS THE ONE PLACE TO EDIT YOUR STATUSES ---
  const orderStatuses = [
    { value: 'pending', text: 'Pending' },
    { value: 'packed', text: 'Packed' },
    { value: 'shipped', text: 'Shipped' },
    { value: 'out_for_delivery', text: 'Out for Delivery' },
    { value: 'delivered', text: 'Delivered' }
  ];
  // ----------------------------------------------------

  // Find all dropdowns with the 'status-select' class
  const statusDropdowns = document.querySelectorAll('.status-select');

  // Loop through each dropdown found
  statusDropdowns.forEach(dropdown => {
    // Get the current status from the parent table row's 'data-status' attribute
    const currentStatus = dropdown.closest('tr').dataset.status;

    // Create and add each status option from the central list
    orderStatuses.forEach(status => {
      const option = document.createElement('option');
      option.value = status.value;
      option.textContent = status.text;

      // If the option's value matches the row's current status, select it
      if (status.value === currentStatus) {
        option.selected = true;
      }
      
      dropdown.appendChild(option);
    });
    
    // --- ADD EVENT LISTENER FOR CHANGES ---
    dropdown.addEventListener('change', function(event) {
      // Get the newly selected status value
      const newStatus = event.target.value;
      
      // Find the parent row to get the Order ID
      const tableRow = event.target.closest('tr');
      
      // Find the button with the Order ID in the first cell and get its text
      const orderId = tableRow.querySelector('td button').textContent.trim();
      
      // Log the information to the console
      console.log(`Status changed for Order ${orderId}. New status: ${newStatus}`);
      
      // You can also log it as an object, which is useful for backend APIs
      console.log({
          orderId: orderId,
          status: newStatus
      });
    });
  });
});