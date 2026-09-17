document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const errorMessage = document.getElementById("error-message");

    errorMessage.textContent = "";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({username, password})
        });

        if (!response.ok) {
            const error = await response.json();
            errorMessage.textContent = error.message || "Usuário ou senha inválidos.";
            return;
        }

        window.location.href = "/home";
    } catch (err) {
    errorMessage.textContent = "Não foi possível conectar ao servidor.";
    }
});

