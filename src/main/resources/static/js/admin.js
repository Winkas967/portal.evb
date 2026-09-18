let allUsers = [];
let allRoles = [];
let currentTab = new URLSearchParams(window.location.search).get("tab") === "roles" ? "roles" : "users";
let editingUserId = null;
let editingRoleId = null;

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

async function sendJSON(url, method, body) {
    const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    if (response.status === 401) {
        window.location.href = "/login";
        return { ok: false, data: null };
    }

    if (response.status === 204) {
        return { ok: true, data: null };
    }

    const data = await response.json();
    return { ok: response.ok, data };
}

function setTab(tab) {
    currentTab = tab;
    document.getElementById("tab-users").classList.toggle("active", tab === "users");
    document.getElementById("tab-roles").classList.toggle("active", tab === "roles");
    document.getElementById("users-panel").classList.toggle("hidden", tab !== "users");
    document.getElementById("roles-panel").classList.toggle("hidden", tab !== "roles");
    document.getElementById("new-item-button").textContent = tab === "users" ? "+ Novo usuário" : "+ Novo papel";
}

function renderUsersTable() {
    const tableBody = document.getElementById("users-table-body");
    tableBody.innerHTML = "";

    if (allUsers.length === 0) {
        tableBody.innerHTML = "<div style=\"padding: 24px 10px; color: var(--color-text-muted); font-size: 13px;\">Nenhum usuário cadastrado.</div>";
        return;
    }

    const sorted = [...allUsers].sort((a, b) => (a.isActive === b.isActive) ? a.name.localeCompare(b.name) : (a.isActive ? -1 : 1));

    for (const user of sorted) {
        const row = document.createElement("div");
        row.className = "users-table-row";

        const nameCell = document.createElement("div");
        nameCell.className = "table-cell-primary";
        nameCell.textContent = user.name;

        const statusCell = document.createElement("div");
        const badge = document.createElement("span");
        badge.className = "status-badge " + (user.isActive ? "active" : "inactive");
        badge.textContent = user.isActive ? "Ativo" : "Inativo";
        statusCell.appendChild(badge);

        const actionsCell = document.createElement("div");
        actionsCell.className = "row-actions";

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "row-action-btn edit";
        editBtn.textContent = "Editar";
        editBtn.disabled = !user.isActive;
        editBtn.addEventListener("click", () => openEditUserModal(user));

        const toggleBtn = document.createElement("button");
        toggleBtn.type = "button";
        toggleBtn.className = "row-action-btn danger";
        toggleBtn.textContent = user.isActive ? "Excluir" : "Reativar";
        toggleBtn.addEventListener("click", () => toggleUserStatus(user));

        actionsCell.append(editBtn, toggleBtn);
        row.append(nameCell, statusCell, actionsCell);
        tableBody.appendChild(row);
    }
}

function renderRolesTable() {
    const tableBody = document.getElementById("roles-table-body");
    tableBody.innerHTML = "";

    if (allRoles.length === 0) {
        tableBody.innerHTML = "<div style=\"padding: 24px 10px; color: var(--color-text-muted); font-size: 13px;\">Nenhum papel cadastrado.</div>";
        return;
    }

    const sorted = [...allRoles].sort((a, b) => (a.isActive === b.isActive) ? a.name.localeCompare(b.name) : (a.isActive ? -1 : 1));

    for (const role of sorted) {
        const row = document.createElement("div");
        row.className = "roles-table-row";

        const nameCell = document.createElement("div");
        nameCell.className = "table-cell-primary";
        nameCell.textContent = role.name;

        const roleCell = document.createElement("div");
        roleCell.className = "table-cell";
        roleCell.textContent = role.role;

        const userCell = document.createElement("div");
        userCell.className = "table-cell";
        userCell.textContent = role.userName;

        const statusCell = document.createElement("div");
        const badge = document.createElement("span");
        badge.className = "status-badge " + (role.isActive ? "active" : "inactive");
        badge.textContent = role.isActive ? "Ativo" : "Inativo";
        statusCell.appendChild(badge);

        const actionsCell = document.createElement("div");
        actionsCell.className = "row-actions";

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "row-action-btn edit";
        editBtn.textContent = "Editar";
        editBtn.disabled = !role.isActive;
        editBtn.addEventListener("click", () => openEditRoleModal(role));

        const toggleBtn = document.createElement("button");
        toggleBtn.type = "button";
        toggleBtn.className = "row-action-btn danger";
        toggleBtn.textContent = role.isActive ? "Excluir" : "Reativar";
        toggleBtn.addEventListener("click", () => toggleRoleStatus(role));

        actionsCell.append(editBtn, toggleBtn);
        row.append(nameCell, roleCell, userCell, statusCell, actionsCell);
        tableBody.appendChild(row);
    }
}

function populateRoleUserSelect(selectedUserId) {
    const select = document.getElementById("role-user");
    select.innerHTML = "";

    const activeUsers = allUsers.filter(u => u.isActive);
    for (const user of activeUsers) {
        const option = document.createElement("option");
        option.value = String(user.id);
        option.textContent = user.name;
        select.appendChild(option);
    }

    if (selectedUserId != null) {
        select.value = String(selectedUserId);
    }
}

function openCreateUserModal() {
    editingUserId = null;
    document.getElementById("user-modal-title").textContent = "Cadastrar usuário";
    document.getElementById("user-password-label").textContent = "Senha";
    document.getElementById("user-form").reset();
    document.getElementById("user-modal-error").textContent = "";
    document.getElementById("user-modal-overlay").classList.remove("hidden");
}

function openEditUserModal(user) {
    editingUserId = user.id;
    document.getElementById("user-modal-title").textContent = "Editar usuário";
    document.getElementById("user-name").value = user.name;
    document.getElementById("user-password").value = "";
    document.getElementById("user-password-label").textContent = "Nova senha (deixe em branco para manter a atual)";
    document.getElementById("user-modal-error").textContent = "";
    document.getElementById("user-modal-overlay").classList.remove("hidden");
}

function closeUserModal() {
    document.getElementById("user-modal-overlay").classList.add("hidden");
}

function openCreateRoleModal() {
    editingRoleId = null;
    document.getElementById("role-modal-title").textContent = "Cadastrar papel";
    document.getElementById("role-form").reset();
    document.getElementById("role-user-group").classList.remove("hidden");
    document.getElementById("role-user").disabled = false;
    populateRoleUserSelect(null);
    document.getElementById("role-modal-error").textContent = "";
    document.getElementById("role-modal-overlay").classList.remove("hidden");
}

function openEditRoleModal(role) {
    editingRoleId = role.id;
    document.getElementById("role-modal-title").textContent = "Editar papel";
    document.getElementById("role-name").value = role.name;
    document.getElementById("role-value").value = role.role;
    populateRoleUserSelect(role.userId);
    document.getElementById("role-user").disabled = true;
    document.getElementById("role-modal-error").textContent = "";
    document.getElementById("role-modal-overlay").classList.remove("hidden");
}

function closeRoleModal() {
    document.getElementById("role-modal-overlay").classList.add("hidden");
}

async function toggleUserStatus(user) {
    const action = user.isActive ? "deactivate" : "reactivate";
    const result = await sendJSON("/api/users/" + user.id + "/" + action, "PATCH");

    if (!result.ok) {
        alert((result.data && result.data.message) || "Não foi possível atualizar o usuário.");
        return;
    }

    await loadAll();
}

async function toggleRoleStatus(role) {
    const action = role.isActive ? "deactivate" : "reactivate";
    const result = await sendJSON("/api/roles/" + role.id + "/" + action, "PATCH");

    if (!result.ok) {
        alert((result.data && result.data.message) || "Não foi possível atualizar o papel.");
        return;
    }

    await loadAll();
}

async function loadAll() {
    try {
        const [users, roles] = await Promise.all([
            fetchJSON("/api/users"),
            fetchJSON("/api/roles")
        ]);

        if (users === null || roles === null) return;

        allUsers = users;
        allRoles = roles;
        renderUsersTable();
        renderRolesTable();
    } catch (err) {
        console.error("Erro ao carregar administração:", err);
    }
}

document.getElementById("tab-users").addEventListener("click", () => setTab("users"));
document.getElementById("tab-roles").addEventListener("click", () => setTab("roles"));

document.getElementById("new-item-button").addEventListener("click", () => {
    if (currentTab === "users") {
        openCreateUserModal();
    } else {
        openCreateRoleModal();
    }
});

document.getElementById("user-modal-cancel-button").addEventListener("click", closeUserModal);
document.getElementById("role-modal-cancel-button").addEventListener("click", closeRoleModal);

document.getElementById("user-modal-overlay").addEventListener("click", (event) => {
    if (event.target.id === "user-modal-overlay") closeUserModal();
});
document.getElementById("role-modal-overlay").addEventListener("click", (event) => {
    if (event.target.id === "role-modal-overlay") closeRoleModal();
});

document.getElementById("user-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("user-name").value.trim();
    const password = document.getElementById("user-password").value;
    const errorEl = document.getElementById("user-modal-error");
    errorEl.textContent = "";

    const url = editingUserId ? ("/api/users/" + editingUserId) : "/api/users";
    const method = editingUserId ? "PUT" : "POST";
    const body = editingUserId
        ? { name, password: password ? password : null }
        : { name, password };

    const result = await sendJSON(url, method, body);

    if (!result.ok) {
        errorEl.textContent = (result.data && result.data.message) || "Não foi possível salvar o usuário.";
        return;
    }

    closeUserModal();
    await loadAll();
});

document.getElementById("role-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("role-name").value.trim();
    const roleValue = document.getElementById("role-value").value.trim();
    const errorEl = document.getElementById("role-modal-error");
    errorEl.textContent = "";

    let url, method, body;
    if (editingRoleId) {
        url = "/api/roles/" + editingRoleId;
        method = "PUT";
        body = { name, role: roleValue };
    } else {
        const userId = Number(document.getElementById("role-user").value);
        url = "/api/roles";
        method = "POST";
        body = { name, role: roleValue, userId };
    }

    const result = await sendJSON(url, method, body);

    if (!result.ok) {
        errorEl.textContent = (result.data && result.data.message) || "Não foi possível salvar o papel.";
        return;
    }

    closeRoleModal();
    await loadAll();
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

setTab(currentTab);
loadAll();
