import { SCHEDULE_URL } from "../modules/Config.js";
import { validateSessionAuth } from "../modules/Security.js";
import {
    createScheduleStamp,
    deleteScheduleStamp,
    getSubjects,
    logoutAndRedirect,
    requestScheduler,
    updateScheduleStamp
} from "../modules/Requests.js";

const types = {
    LESSON: "LESSON",
    BREAK: "BREAK",
};

function toUiType(apiType) {
    return apiType === types.BREAK ? "break" : "lesson";
}

function toLabel(stamp) {
    return stamp.type === types.BREAK ? "Break" : stamp.subject ?? "Lesson";
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

function createTimeStampElement(dayOfWeek, data) {
    const div = document.createElement("div");
    div.className = toUiType(data.type);

    const span = document.createElement("span");
    span.textContent = toLabel(data);
    div.appendChild(span);

    const editButton = document.createElement("button");
    editButton.className = "editButton";
    editButton.textContent = "⋮";
    div.appendChild(editButton);

    const options = [];
    if (data.type === types.LESSON) {
        applyMenuEntries(options, span, dayOfWeek, data);
    }

    const menu = createMenu(options, editButton);

    const deleteButton = document.createElement("button");
    deleteButton.textContent = "Delete";
    deleteButton.onclick = async () => {
        const result = await deleteScheduleStamp(`${SCHEDULE_URL}/${dayOfWeek}/${data.id}`);
        if (!result.ok) {
            return;
        }
        div.remove();
        menu.remove();
    };

    menu.appendChild(deleteButton);

    return div;
}

function applyMenuEntries(options, span, dayOfWeek, data) {
    options.push({
        label: "Edit",
        action: async () => {

            const subjects = await getSubjects();
            if (!subjects.ok || !Array.isArray(subjects.data)) {
                return;
            }

            const select = document.createElement("select");
            subjects.data.forEach(name => {
                const option = document.createElement("option");
                option.value = option.textContent = name;
                option.selected = name === span.textContent;
                select.appendChild(option);
            });

            span.replaceWith(select);
            select.focus();

            const save = async () => {
                data.subject = select.value;
                const result = await updateScheduleStamp(dayOfWeek, data);
                if (!result.ok) {
                    return;
                }
                span.textContent = select.value;
                select.replaceWith(span);
            };

            select.onchange = save;
            select.onblur = save;
        }
    });
}

async function loadSchedule() {
    const response = await requestScheduler();
    if (!response.ok || !Array.isArray(response.data)) {
        return;
    }

    const groupedByDay = response.data.reduce((acc, stamp) => {
        if (!acc[stamp.dayOfWeek]) {
            acc[stamp.dayOfWeek] = [];
        }
        acc[stamp.dayOfWeek].push(stamp);
        return acc;
    }, {});

    Object.entries(groupedByDay).forEach(([dayOfWeek, scheduleStamps]) => {
        const container = document.getElementById(dayOfWeek);
        if (!container) {
            return;
        }

        container.querySelectorAll(".lesson, .break").forEach(e => e.remove());

        scheduleStamps
            .sort((stampOne, stampTwo) => stampOne.id - stampTwo.id)
            .forEach(scheduleStamp => {

                const entriesContainer = container.querySelector(".entriesContainer");
                entriesContainer.appendChild(createTimeStampElement(dayOfWeek, scheduleStamp));

            }
        );
    });
}

async function addItem(dayOfWeek, type) {
    const container = document.getElementById(dayOfWeek);
    if (!container) {
        return;
    }

    const response = await createScheduleStamp({ type }, dayOfWeek);
    if (!response.ok) {
        return;
    }

    const element = createTimeStampElement(dayOfWeek, response.data);
    container.querySelector(".entriesContainer").appendChild(element);
}

window.addEventListener("DOMContentLoaded", async () => {
    await validateSessionAuth();

    document.getElementById("schoolworkButton").onclick = () => {
        location.href = "/schoolwork/index.html";
    };

    document.querySelectorAll(".addLesson")
        .forEach(button => button.onclick = e =>
            addItem(e.target.closest(".hoursContainer").id, types.LESSON)
        );

    document.querySelectorAll(".addBreak")
        .forEach(button => button.onclick = element =>
            addItem(element.target.closest(".hoursContainer").id, types.BREAK)
        );

    document.getElementById("logoutButton").onclick = async () => {
        await logoutAndRedirect();
    };

    await loadSchedule();
});
