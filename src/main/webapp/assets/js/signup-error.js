window.addEventListener("DOMContentLoaded", function () {
    function getQueryParam(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    }

    if (getQueryParam("error") === "1") {
        alert("Signup failed! Mone you are allready registerd.");
    }
});
