let allPatients = [];
let currentTab = "active";
let editingId = null;
let searchQuery = new URLSearchParams(window.location.search).get("search") || "";

function renderUserMini(me) {
    const nameEl = document.getElementById("user-mini-name");
    const roleEl = document.getElementById("user-mini-role");
    const avatarEl = document.getElementById("user-avatar");

    if (nameEl) nameEl.textContent = me.username;
    if (roleEl) roleEl.textContent = me.displayRole;
    if (avatarEl) avatarEl.textContent = me.username.charAt(0).toUpperCase();
}

async function applyAdminVisibility() {
    try {
        const response = await fetch("/api/auth/me");
        if (!response.ok) return;

        const me = await response.json();
        renderUserMini(me);

        const isAdmin = me.roles.includes("ADMIN");
        if (!isAdmin) {
            document.querySelectorAll(".admin-only").forEach(el => el.classList.add("hidden"));
        }
    } catch (err) {
        console.error("Erro ao verificar permissões:", err);
        showNotification("Não foi possível verificar suas permissões.", "error");
    }
}

async function fetchJSON(url) {
    const response = await fetch(url);

    if (response.status === 401) {
        queueNotification("Sua sessão expirou. Faça login novamente.", "info");
        window.location.href = "/login";
        return null;
    }

    if (!response.ok) {
        throw new Error("Falha ao buscar " + url);
    }

    return response.json();
}

async function sendJSON(url, method, body) {
    const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    if (response.status === 401) {
        queueNotification("Sua sessão expirou. Faça login novamente.", "info");
        window.location.href = "/login";
        return { ok: false, data: null };
    }

    if (response.status === 204) {
        return { ok: true, data: null };
    }

    const data = await response.json();
    return { ok: response.ok, data };
}

function formatDateTime(isoDateTime) {
    return new Date(isoDateTime).toLocaleString("pt-BR", {
        day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit"
    });
}

function toDatetimeLocalValue(isoDateTime) {
    const date = new Date(isoDateTime);
    const pad = (n) => String(n).padStart(2, "0");
    return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate())
        + "T" + pad(date.getHours()) + ":" + pad(date.getMinutes());
}

function setTab(tab) {
    currentTab = tab;
    document.getElementById("tab-active").classList.toggle("active", tab === "active");
    document.getElementById("tab-inactive").classList.toggle("active", tab === "inactive");
    renderTable();
}

function renderTable() {
    const tableBody = document.getElementById("patients-table-body");
    tableBody.innerHTML = "";

    const query = searchQuery.trim().toLowerCase();

    const filtered = allPatients.filter(p => {
        const matchesTab = currentTab === "active" ? p.isActive : !p.isActive;
        if (!matchesTab) return false;
        if (!query) return true;
        return p.name.toLowerCase().includes(query)
            || p.responsible.toLowerCase().includes(query)
            || p.callReport.toLowerCase().includes(query);
    });

    if (filtered.length === 0) {
        const empty = document.createElement("div");
        empty.style.padding = "24px 10px";
        empty.style.color = "var(--color-text-muted)";
        empty.style.fontSize = "13px";
        empty.textContent = query
            ? "Nenhum paciente encontrado para essa busca."
            : (currentTab === "active" ? "Nenhum paciente ativo." : "Nenhum paciente inativo.");
        tableBody.appendChild(empty);
        return;
    }

    const sorted = [...filtered].sort((a, b) => new Date(b.callTime) - new Date(a.callTime));

    for (const patient of sorted) {
        const row = document.createElement("div");
        row.className = "patients-table-row";

        const nameCell = document.createElement("div");
        nameCell.className = "table-cell-primary";
        nameCell.textContent = patient.name;

        const responsibleCell = document.createElement("div");
        responsibleCell.className = "table-cell";
        responsibleCell.textContent = patient.responsible;

        const timeCell = document.createElement("div");
        timeCell.className = "table-cell";
        timeCell.textContent = formatDateTime(patient.callTime);

        const reportCell = document.createElement("div");
        reportCell.className = "table-cell table-cell-truncate";
        reportCell.title = patient.callReport;
        reportCell.textContent = patient.callReport;

        const statusCell = document.createElement("div");
        const badge = document.createElement("span");
        badge.className = "status-badge " + (patient.isActive ? "active" : "inactive");
        badge.textContent = patient.isActive ? "Ativo" : "Inativo";
        statusCell.appendChild(badge);

        const actionsCell = document.createElement("div");
        actionsCell.className = "row-actions";

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "row-action-btn edit";
        editBtn.textContent = "Editar";
        editBtn.disabled = !patient.isActive;
        editBtn.addEventListener("click", () => openEditModal(patient));

        const toggleBtn = document.createElement("button");
        toggleBtn.type = "button";
        toggleBtn.className = "row-action-btn danger";
        toggleBtn.textContent = patient.isActive ? "Excluir" : "Reativar";
        toggleBtn.addEventListener("click", () => togglePatientStatus(patient));

        actionsCell.append(editBtn, toggleBtn);
        row.append(nameCell, responsibleCell, timeCell, reportCell, statusCell, actionsCell);
        tableBody.appendChild(row);
    }
}

function openCreateModal() {
    editingId = null;
    document.getElementById("modal-title").textContent = "Cadastrar paciente";
    document.getElementById("patient-form").reset();
    document.getElementById("modal-error").textContent = "";
    document.getElementById("modal-overlay").classList.remove("hidden");
}

function openEditModal(patient) {
    editingId = patient.id;
    document.getElementById("modal-title").textContent = "Editar paciente";
    document.getElementById("patient-name").value = patient.name;
    document.getElementById("patient-call-time").value = toDatetimeLocalValue(patient.callTime);
    document.getElementById("patient-responsible").value = patient.responsible;
    document.getElementById("patient-report").value = patient.callReport;
    document.getElementById("modal-error").textContent = "";
    document.getElementById("modal-overlay").classList.remove("hidden");
}

function closeModal() {
    document.getElementById("modal-overlay").classList.add("hidden");
}

async function togglePatientStatus(patient) {
    const action = patient.isActive ? "deactivate" : "reactivate";
    const result = await sendJSON("/api/patients/" + patient.id + "/" + action, "PATCH");

    if (!result.ok) {
        showNotification((result.data && result.data.message) || "Não foi possível atualizar o paciente.", "error");
        return;
    }

    await loadPatients();
}

async function loadPatients() {
    try {
        const patients = await fetchJSON("/api/patients");
        if (patients === null) return;
        allPatients = patients;
        renderTable();
    } catch (err) {
        console.error("Erro ao carregar pacientes:", err);
        showNotification("Não foi possível carregar a lista de pacientes.", "error");
    }
}

document.getElementById("tab-active").addEventListener("click", () => setTab("active"));
document.getElementById("tab-inactive").addEventListener("click", () => setTab("inactive"));

document.getElementById("search-input").addEventListener("input", (event) => {
    searchQuery = event.target.value;
    renderTable();
});

document.getElementById("search-input").value = searchQuery;
document.getElementById("new-patient-button").addEventListener("click", openCreateModal);
document.getElementById("modal-cancel-button").addEventListener("click", closeModal);

document.getElementById("modal-overlay").addEventListener("click", (event) => {
    if (event.target.id === "modal-overlay") {
        closeModal();
    }
});

document.getElementById("patient-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("patient-name").value.trim();
    const callTimeInput = document.getElementById("patient-call-time").value;
    const responsible = document.getElementById("patient-responsible").value.trim();
    const callReport = document.getElementById("patient-report").value.trim();
    const errorEl = document.getElementById("modal-error");

    errorEl.textContent = "";

    const body = {
        name,
        callTime: callTimeInput ? new Date(callTimeInput).toISOString() : null,
        responsible,
        callReport
    };

    const url = editingId ? ("/api/patients/" + editingId) : "/api/patients";
    const method = editingId ? "PUT" : "POST";

    const result = await sendJSON(url, method, body);

    if (!result.ok) {
        errorEl.textContent = (result.data && result.data.message) || "Não foi possível salvar o paciente.";
        return;
    }

    closeModal();
    await loadPatients();
});

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
loadPatients();
