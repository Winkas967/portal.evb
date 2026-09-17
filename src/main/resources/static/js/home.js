document.getElementById("logout-link").addEventListener("click", async function (event) {
    event.preventDefault();

    try {
        await fetch("/api/auth/logout", { method: "POST" });
    } catch (err) {
        // mesmo se a chamada falhar, seguimos para o login
    }

    window.location.href = "/login";
});
