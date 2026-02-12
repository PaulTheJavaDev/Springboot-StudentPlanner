import { validateSessionAuth, getSessionID } from "/modules/Security.js";
import { HOME_URL, HOST } from "/modules/Config.js";

const elements = {
    feedbackElement: document.getElementById("responseLabel")
};

const errorMessages = {
    emptyFields: "Please fill the input fields.",
    defaultError: "Something went wrong"
};

function showResponse(message, duration = 1500) {
    elements.feedbackElement.textContent = message;
    setTimeout(() => {
        elements.feedbackElement.textContent = "";
    }, duration);
}

// Generic GET request - default is the schedule/me URL
async function apiGet(url = HOME_URL) {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: { 'SessionID': getSessionID() }
        });

        if (!response.ok) {
            showResponse(`Couldn't fetch data: ${await response.text()}`);
            return;
        }

        return await response.json();
    } catch (error) {
        showResponse(`Fetch error: ${error}`);
        return;
    }
}

// Generic DELETE request
async function apiDelete(url) {

    try {
        const response = await fetch(url, { 
            method: 'DELETE', 
            headers: { 
                'SessionID': getSessionID() 
            } 
        });

        if (!response.ok) {
            showResponse(errorMessages.defaultError);
        }

        return response.ok;

    } catch {
        showResponse(errorMessages.defaultError);
        return false;
    }

}

// Generic POST request
async function apiPost(
    url, 
    data
) {

    try {

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'SessionID': getSessionID()
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            showResponse(errorMessages.defaultError);
            return;
        }

        return await response.json();

    } catch {
        showResponse(errorMessages.defaultError);
    }
}

// Generic PUT request
async function apiPut(
    url, 
    data
) {

    try {

        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'SessionID': getSessionID()
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            showResponse(errorMessages.defaultError);
            return;
        }

        return await response.json();

    } catch {
        showResponse(errorMessages.defaultError);
        return;
    }
}

// Fetch subjects
async function getSubjects() {
    return await apiGet(`${HOST}/subjects`);
}

// Context menu creation
// Context menu creation
function createMenu(options, menuButton) {
    const menu = document.createElement('div');
    menu.className = 'contextMenu';
    menu.style.display = 'none';  // Initial versteckt
    
    options.forEach(option => {
        const optionButton = document.createElement('button');
        optionButton.className = 'contextMenuOption';
        optionButton.textContent = option.label;
        optionButton.onclick = () => {
            option.action();
            menu.style.display = 'none';
        };
        menu.appendChild(optionButton);
    });
    
    document.body.appendChild(menu);
    
    menuButton.onclick = (event) => {
        event.stopPropagation();  // Verhindert sofortiges Schließen
        
        const rect = menuButton.getBoundingClientRect();
        menu.style.top = `${rect.bottom + window.scrollY + 5}px`;
        menu.style.left = `${rect.left + window.scrollX}px`;
        menu.style.display = 'block';
        
        const handleClickOutside = (event) => {
            if (!menu.contains(event.target) && event.target !== menuButton) {
                menu.style.display = 'none';
                document.removeEventListener('click', handleClickOutside);
            }
        };
        
        // Kleine Verzögerung, damit dieser Click nicht sofort das Menu schließt
        setTimeout(() => {
            document.addEventListener('click', handleClickOutside);
        }, 0);
    };
    
    return menu;
}

// Create timestamp element
function createTimeStampElement(dayOfWeek, data) {

    const div = document.createElement('div');
    div.className = data.type;
    div.dataset.type = data.type;
    div.style.position = 'relative';
    div.style.backgroundColor = data.type === 'lesson' ? '#e0f7fa' : '#fff3e0';

    const span = document.createElement('span');
    span.textContent = data.text;
    div.appendChild(span);

    const editButton = document.createElement('button');
    editButton.className = 'editButton';
    editButton.textContent = '⋮';
    div.appendChild(editButton);

    const options = [];
    if (data.type === 'lesson') {
        options.push({
            label: 'Edit',
            action: async () => {
                const subjects = await getSubjects();
                if (!subjects) return;

                const select = document.createElement('select');
                subjects.forEach(name => {
                    const option = document.createElement('option');
                    option.value = option.textContent = name;
                    if (name === span.textContent) option.selected = true;
                    select.appendChild(option);
                });

                span.replaceWith(select);
                select.focus();

                const save = async () => {
                    span.textContent = select.value;
                    await apiPut(`${HOME_URL}/${dayOfWeek}/${data.id}`, { type: data.type, text: select.value });
                    data.text = select.value;
                    select.replaceWith(span);
                };

                select.addEventListener('change', save);
                select.addEventListener('blur', save);
            }
        });
    }

    const menu = createMenu(options, editButton);

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = 'Delete';
    deleteBtn.onclick = async () => {
        div.remove();
        menu.remove();
        await apiDelete(`${HOME_URL}/${dayOfWeek}/${data.id}`);
    };
    menu.appendChild(deleteBtn);

    return div;
}

// Load full schedule
async function loadSchedule() {
    const days = await apiGet();
    if (!days) return;

    days.forEach(day => {
        const container = document.getElementById(day.dayOfWeek);
        if (!container) return;

        container.querySelectorAll('.lesson, .break').forEach(e => e.remove());
        day.timeStamps.sort((a, b) => a.id - b.id)
                      .forEach(ts => container.appendChild(createTimeStampElement(day.dayOfWeek, ts)));
    });
}

// Add new timestamp
async function addItem(dayOfWeek, type) {
    const container = document.getElementById(dayOfWeek);
    if (!container) return;
    const timestamp = await apiPost(`${HOME_URL}/${dayOfWeek}`, { type, text: type === 'lesson' ? 'Lesson' : 'Break' });
    if (!timestamp) return;
    container.appendChild(createTimeStampElement(dayOfWeek, timestamp));
}

// Event bindings
document.querySelectorAll('.addLesson').forEach(button => button.onclick = element => addItem(element.target.closest('.hoursContainer').id, 'lesson'));
document.querySelectorAll('.addBreak').forEach(button => button.onclick = element => addItem(element.target.closest('.hoursContainer').id, 'break'));
document.querySelectorAll('.assignmentsButton').forEach(button => button.onclick = () => window.location.href = '/assignments/index.html');
document.querySelectorAll('.examsButton').forEach(button => button.onclick = () => window.location.href = '/exams/index.html');

document.getElementById('logoutButton').onclick = () => {
    sessionStorage.removeItem('SessionID');
    window.location.href = '/login/index.html';
};

// Initialize
window.addEventListener('DOMContentLoaded', () => {
    validateSessionAuth();

    if (!sessionStorage.getItem('SessionID')) {
        window.location.href = '/login/index.html';
        return;
    }

    loadSchedule();
});