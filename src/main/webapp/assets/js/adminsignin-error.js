/**
 * Admin Sign-in Error Handler
 *
 * This script is dedicated solely to the admin sign-in page. It checks for
 * the 'error=invalid' query parameter and displays the corresponding error message.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Get the current URL's query parameters
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');

    // Find the error message container on the page
    const errorContainer = document.getElementById('error-container');

    // If the error parameter is 'invalid' and the container exists, show it
    if (error === 'invalid' && errorContainer) {
        errorContainer.style.display = 'block';
    }
});