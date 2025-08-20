
// this js code is to point out the error in the singin page when trying to sign in

const urlParams = new URLSearchParams(window.location.search);
const errorType = urlParams.get("error");

if (errorType === "email") {
    // Show only the email error message and add a red border
    document.getElementById("email-error").style.display = "block";
    document.getElementById("login-email").style.borderColor = "red";
} else if (errorType === "password") {
    // Show only the password error message and add a red border
    document.getElementById("password-error").style.display = "block";
    document.getElementById("login-password").style.borderColor = "red";
}

