// This JS code is to point out the error in the signin page when trying to sign in

const urlParams = new URLSearchParams(window.location.search);
const errorType = urlParams.get("error");

// Select inputs
const emailInput = document.querySelector("input[name='email']");
const passwordInput = document.querySelector("input[name='password']");

// Handle error display based on URL
if (errorType === "email") {
    document.getElementById("email-error").style.display = "block";
    emailInput.classList.add("is-invalid"); // Bootstrap red border
} else if (errorType === "password") {
    document.getElementById("password-error").style.display = "block";
    passwordInput.classList.add("is-invalid"); // Bootstrap red border
}

// --- Clear error when user types again ---
emailInput.addEventListener("input", () => {
    document.getElementById("email-error").style.display = "none";
    emailInput.classList.remove("is-invalid");
});

passwordInput.addEventListener("input", () => {
    document.getElementById("password-error").style.display = "none";
    passwordInput.classList.remove("is-invalid");
});
