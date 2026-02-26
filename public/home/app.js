import { HOME_URL, HOST } from "/modules/Config.js";
import { validateSessionAuth } from "/modules/Security.js";

const elements = {
    feedbackElement: document.getElementById("responseLabel")
};

const errorMessages = {
    emptyFields: "Please fill the input fields.",
    defaultError: "Something went wrong"
};

function showResponse(message, duration = 1500) {
    if (!elements.feedbackElement) return;
    elements.feedbackElement.textContent = message;
    setTimeout(() => elements.feedbackElement.textContent = "", duration);
}

async function apiRequest(method, url, data) {
    try {
        const response = await fetch(url, {
            method,
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: data ? JSON.stringify(data) : undefined
        });

        if (!response.ok) {
            showResponse(errorMessages.defaultError);
            return null;
        }

        return method === "DELETE" ? response.ok : await response.json();
    } catch {
        showResponse(errorMessages.defaultError);
        return null;
    }
}

const apiGet = (url = HOME_URL) => apiRequest("GET", url);
const apiPost = (url, data) => apiRequest("POST", url, data);
const apiPut = (url, data) => apiRequest("PUT", url, data);
const apiDelete = (url) => apiRequest("DELETE", url);

async function getSubjects() {
    return apiRequest("GET", `${HOST}/subjects`);
}

function createMenu(options, button) {
    const menu = document.createElement("div");
    menu.className = "contextMenu";

    options.forEach(({ label, action }) => {
        const item = document.createElement("button");
        item.textContent = label;
        item.onclick = () => {
            action();
            menu.remove();
        };
        menu.appendChild(item);
    });

    document.body.appendChild(menu);

    button.onclick = (event) => {
        event.stopPropagation();

        const rect = button.getBoundingClientRect();
        menu.style.top = `${rect.bottom + 6}px`;
        menu.style.left = `${rect.left}px`;
        menu.style.display = "block";

        const close = () => {
            menu.remove();
            document.removeEventListener("click", close);
        };

        setTimeout(() => document.addEventListener("click", close));
    };

    return menu;
}

/* ---------------- TIMESTAMP ---------------- */

function createTimeStampElement(dayOfWeek, data) {
    const div = document.createElement("div");
    div.className = data.type;

    const span = document.createElement("span");
    span.textContent = data.text;
    div.appendChild(span);

    const editButton = document.createElement("button");
    editButton.className = "editButton";
    editButton.textContent = "⋮";
    div.appendChild(editButton);

    const options = [];

    if (data.type === "lesson") {
        options.push({
            label: "Edit",
            action: async () => {
                const subjects = await getSubjects();
                if (!subjects) return;

                const select = document.createElement("select");
                subjects.forEach(name => {
                    const option = document.createElement("option");
                    option.value = option.textContent = name;
                    option.selected = name === span.textContent;
                    select.appendChild(option);
                });

                span.replaceWith(select);
                select.focus();

                const save = async () => {
                    await apiPut(`${HOME_URL}/${dayOfWeek}/${data.id}`, {
                        type: data.type,
                        text: select.value
                    });
                    span.textContent = select.value;
                    select.replaceWith(span);
                };

                select.onchange = save;
                select.onblur = save;
            }
        });
    }

    const menu = createMenu(options, editButton);

    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Delete";
    deleteButton.onclick = async () => {
        div.remove();
        menu.remove();
        await apiDelete(`${HOME_URL}/${dayOfWeek}/${data.id}`);
    };

    menu.appendChild(deleteButton);

    return div;
}

async function loadSchedule() {
    const timeStamps = await apiGet();
    if (!timeStamps) return;

    // Group flat TimeStamp list by dayOfWeek
    const grouped = timeStamps.reduce((acc, ts) => {
        if (!acc[ts.dayOfWeek]) acc[ts.dayOfWeek] = [];
        acc[ts.dayOfWeek].push(ts);
        return acc;
    }, {});

    Object.entries(grouped).forEach(([dayOfWeek, stamps]) => {
        const container = document.getElementById(dayOfWeek);
        if (!container) return;

        container.querySelectorAll(".lesson, .break").forEach(e => e.remove());

        stamps
            .sort((a, b) => a.id - b.id)
            .forEach(ts => container.insertBefore(
                createTimeStampElement(dayOfWeek, ts),
                container.querySelector(".addLesson")
            ));
    });
}

async function addItem(dayOfWeek, type) {
    const container = document.getElementById(dayOfWeek);
    if (!container) return;

    const timestamp = await apiPost(`${HOME_URL}/${dayOfWeek}`, {
        type,
        text: type === "lesson" ? "Lesson" : "Break"
    });

    if (!timestamp) return;

    const element = createTimeStampElement(dayOfWeek, timestamp);
    container.insertBefore(element, container.querySelector(".addLesson"));
}

window.addEventListener("DOMContentLoaded", async () => {
    await validateSessionAuth();

    document.querySelectorAll(".addLesson")
        .forEach(button => button.onclick = element => addItem(element.target.closest(".hoursContainer").id, "lesson"));

    document.querySelectorAll(".addBreak")
        .forEach(button => button.onclick = element => addItem(element.target.closest(".hoursContainer").id, "break"));

    document.getElementById("logoutButton").onclick = async () => {
        await fetch(`${HOST}/auth/logout`, { method: "POST", credentials: "include" });
        location.href = "/login/index.html";
    };

    loadSchedule();
});