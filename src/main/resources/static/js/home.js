async function fetchJSON(url) {
    const response = await fetch(url);

    if (response.status === 401) {
        window.location.href = "/login";
        return null;
    }

    if (!response.ok) {
        throw new Error("Falha ao buscar " + url);
    }

    return response.json();
}

async function applyAdminVisibility() {
    try {
        const response = await fetch("/api/auth/me");
        if (!response.ok) return;

        const me = await response.json();
        const isAdmin = me.roles.includes("ADMIN");

        if (!isAdmin) {
            document.querySelectorAll(".admin-only").forEach(el => el.classList.add("hidden"));
        }
    } catch (err) {
        console.error("Erro ao verificar permissões:", err);
    }
}

function formatTime(isoDateTime) {
    return new Date(isoDateTime).toLocaleTimeString("pt-BR", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

function isToday(isoDateTime) {
    const date = new Date(isoDateTime);
    const now = new Date();
    return date.getFullYear() === now.getFullYear()
        && date.getMonth() === now.getMonth()
        && date.getDate() === now.getDate();
}

function renderStats(patients, users) {
    const activePatients = patients.filter(p => p.isActive);
    const inactivePatients = patients.filter(p => !p.isActive);
    const activeUsers = users.filter(u => u.isActive);
    const todayCalls = patients.filter(p => isToday(p.callTime));

    document.getElementById("stat-active-patients").textContent = activePatients.length;
    document.getElementById("stat-today-calls").textContent = todayCalls.length;
    document.getElementById("stat-active-users").textContent = activeUsers.length;

    document.getElementById("status-active-count").textContent = activePatients.length;
    document.getElementById("status-inactive-count").textContent = inactivePatients.length;
}

function renderRecentPatients(patients) {
    const tableBody = document.getElementById("recent-patients-body");
    tableBody.innerHTML = "";

    const recent = [...patients]
        .sort((a, b) => new Date(b.callTime) - new Date(a.callTime))
        .slice(0, 5);

    if (recent.length === 0) {
        tableBody.innerHTML = "<div style=\"padding: 20px 10px; color: var(--color-text-muted); font-size: 13px;\">Nenhum paciente cadastrado ainda.</div>";
        return;
    }

    for (const patient of recent) {
        const row = document.createElement("div");
        row.className = "table-row";

        const statusClass = patient.isActive ? "done" : "in-progress";
        const statusLabel = patient.isActive ? "Ativo" : "Inativo";

        row.innerHTML = `
            <div class="table-cell-primary"></div>
            <div class="table-cell"></div>
            <div class="table-cell"></div>
            <div><span class="status-badge ${statusClass}"></span></div>
        `;

        row.children[0].textContent = patient.name;
        row.children[1].textContent = patient.responsible;
        row.children[2].textContent = formatTime(patient.callTime);
        row.children[3].firstElementChild.textContent = statusLabel;

        tableBody.appendChild(row);
    }
}

async function loadDashboard() {
    try {
        const [patients, users] = await Promise.all([
            fetchJSON("/api/patients"),
            fetchJSON("/api/users")
        ]);

        if (patients === null || users === null) {
            return;
        }

        renderStats(patients, users);
        renderRecentPatients(patients);
    } catch (err) {
        console.error("Erro ao carregar o painel:", err);
    }
}

document.getElementById("logout-link").addEventListener("click", async function (event) {
    event.preventDefault();

    try {
        await fetch("/api/auth/logout", { method: "POST" });
    } catch (err) {
        // segue para o login mesmo se a chamada falhar
    }

    window.location.href = "/login";
});

applyAdminVisibility();
loadDashboard();
