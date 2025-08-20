// signup-error.js

// Check if ?error=1 is present in the URL
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has("error")) {
    alert("❌ Signup failed. Email already registered.");
}
