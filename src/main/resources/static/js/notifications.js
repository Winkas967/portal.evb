(function () {
    function ensureContainer() {
        let container = document.getElementById("toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "toast-container";
            container.className = "toast-container";
            document.body.appendChild(container);
        }
        return container;
    }

    function showNotification(message, type) {
        type = type === "success" || type === "info" ? type : "error";
        const container = ensureContainer();

        const toast = document.createElement("div");
        toast.className = "toast toast-" + type;
        toast.setAttribute("role", "alert");

        const text = document.createElement("span");
        text.className = "toast-message";
        text.textContent = message;

        const closeButton = document.createElement("button");
        closeButton.type = "button";
        closeButton.className = "toast-close";
        closeButton.setAttribute("aria-label", "Fechar notificação");
        closeButton.textContent = "×";

        toast.append(text, closeButton);
        container.appendChild(toast);

        let dismissTimer = setTimeout(dismiss, 6000);

        function dismiss() {
            clearTimeout(dismissTimer);
            toast.classList.add("toast-hide");
            setTimeout(() => toast.remove(), 200);
        }

        closeButton.addEventListener("click", dismiss);
        toast.addEventListener("mouseenter", () => clearTimeout(dismissTimer));
        toast.addEventListener("mouseleave", () => {
            dismissTimer = setTimeout(dismiss, 3000);
        });

        return toast;
    }

    function queueNotification(message, type) {
        try {
            sessionStorage.setItem("pendingNotification", JSON.stringify({ message, type: type || "error" }));
        } catch (err) {
            // sessionStorage indisponível; a notificação simplesmente não sobrevive ao redirecionamento
        }
    }

    window.showNotification = showNotification;
    window.queueNotification = queueNotification;

    document.addEventListener("DOMContentLoaded", function () {
        let pending = null;
        try {
            pending = sessionStorage.getItem("pendingNotification");
            if (pending) sessionStorage.removeItem("pendingNotification");
        } catch (err) {
            pending = null;
        }

        if (pending) {
            try {
                const parsed = JSON.parse(pending);
                showNotification(parsed.message, parsed.type);
            } catch (err) {
                // payload inválido, ignora
            }
        }
    });

    window.addEventListener("error", function () {
        showNotification("Ocorreu um erro inesperado nesta página.", "error");
    });

    window.addEventListener("unhandledrejection", function () {
        showNotification("Ocorreu um erro inesperado. Tente novamente.", "error");
    });
})();
