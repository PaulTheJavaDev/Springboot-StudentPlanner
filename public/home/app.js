import { validateSessionAuth, getSessionID } from "/modules/Security.js";
import { HOME_URL } from "/modules/Config.js";
import { setFeedbackLabel, showFeedback } from "/modules/Feedback.js";

validateSessionAuth();

setFeedbackLabel(document.getElementById("responseLabel"));

// GET request to fetch schedule data
async function apiGet() {

    let result;
    
    try {
        result = await fetch(HOME_URL, {
        method: 'GET',
        headers: {
            'SessionID': getSessionID()
        }
        });

        if (!result.ok) {
            showFeedback("Could't get Scheduler: " + await result.text());
            return;
        }
        
    } catch (error) {
        showFeedback("Error during the fetch of TimeStamps: " + error);
        return;
    }
    
    return result.json();
}

async function deleteTimeStampAPI(dayOfWeek, timestampID) {
    
    const errorMessage = `Something went wrong.`;
    let result;

    try {
        result = await fetch(`${HOME_URL}/${dayOfWeek}/${timestampID}`, {
        method: 'DELETE',
        headers: {
            'SessionID': getSessionID()
        }
    });
    } catch (error) {
        showFeedback(errorMessage);
        return;
    }

    if (!result.ok) {
        showFeedback(errorMessage);
        return;
    }

}

// Data Creation
async function createTimeStampAPI(dayOfWeek, type, text) {

    const errorMessage = `Something went wrong.`;
    let result;

    try {
        result = await fetch(`${HOME_URL}/${dayOfWeek}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'SessionID': getSessionID()
        },
        body: JSON.stringify({type, text})
    });
    } catch(error) {
        showFeedback(errorMessage);
        return;
    }

    if (!result.ok) {
        showFeedback(errorMessage);
        return;
    }

    return await result.json();
}

async function apiUpdateTimeStamp(
    dayOfWeek, 
    timestampID, 
    type, 
    text
) {

    const fetchURL = `${HOME_URL}/${dayOfWeek}/${timestampID}`;

    const result = await fetch(fetchURL, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'SessionID': getSessionID()
        },
        body: JSON.stringify({ type, text })
    });

    if (!result.ok) {
        showFeedback("Something went wrong.");
        return;
    }

    return await result.json();
}

async function getSubjects() {

    const fetchAPI = `${API_URL}/subjects`;

    const result = await fetch(fetchAPI, {
        method: 'GET',
        headers: {
            'SessionID': getSessionID()
        }
    });

    if (!result.ok) {
        showFeedback("Something went wrong.");
        return;
    }

    return await result.json();
}

// Context menu creation
function createMenu(
    options, 
    button
) {

    const menu = document.createElement('div');
    menu.className = 'contextMenu';

    options.forEach(opt => {
        const btn = document.createElement('button');
        btn.textContent = opt.label;
        btn.onclick = () => {
            opt.action();
            menu.style.display = 'none';
        };
        menu.appendChild(btn);
    });

    document.body.appendChild(menu);
    menu.style.position = 'absolute';
    menu.style.top = '0';
    menu.style.left = '100%';
    menu.style.display = 'none';

    // Show menu
    button.onclick = () => {
        const rect = button.getBoundingClientRect();
        menu.style.top = `${rect.bottom + window.scrollY + 5}px`;
        menu.style.left = `${rect.left + window.scrollX}px`;
        menu.style.display = 'block';

        const handleClickOutside = (event) => {
            if (!menu.contains(event.target) && event.target !== button) {
                menu.style.display = 'none';
                document.removeEventListener('click', handleClickOutside);
            }
        };

        document.addEventListener('click', handleClickOutside);
    };

    return menu;
}

// Create Timestamp Element | Data -> Element
function createTimeStampElement(
    dayOfWeek, 
    data
) {
    
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
                const lessonOptions = await getSubjects();
                const select = document.createElement('select');

                lessonOptions.forEach(name => {
                    const option = document.createElement('option');
                    option.value = name;
                    option.textContent = name;
                    if (name === span.textContent) option.selected = true;
                    select.appendChild(option);
                });

                span.replaceWith(select);
                select.focus();

                const restoreSpan = async () => {
                    span.textContent = select.value;
                    // Backend erwartet beide: type und text
                    await apiUpdateTimeStamp(dayOfWeek, data.id, data.type, select.value);
                    data.text = select.value;
                    select.replaceWith(span);
                };

                select.addEventListener('change', restoreSpan);
                select.addEventListener('blur', restoreSpan);
            }
        });
    }

    const menu = createMenu(options, editButton);

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = 'Delete';
    deleteBtn.onclick = async () => {
        div.remove();
        menu.remove();
        await deleteTimeStampAPI(dayOfWeek, data.id);
    };
    menu.appendChild(deleteBtn);

    return div;
}


// Load Schedule
async function loadSchedule() {
    const days = await apiGet();

    days.forEach(day => {
        const container = document.getElementById(day.dayOfWeek);
        if (!container) return;

        // Clear old elements
        container.querySelectorAll('.lesson, .break').forEach(e => e.remove());

        // Sort timestamps by ID before rendering
        const sortedTimestamps = [...day.timeStamps].sort((a, b) => a.id - b.id);
        
        sortedTimestamps.forEach(timestamp => {
            container.appendChild(createTimeStampElement(day.dayOfWeek, timestamp));
        });
    });
}

async function addItem(dayOfWeek, type) {
    const container = document.getElementById(dayOfWeek);
    const timestamp = await createTimeStampAPI(dayOfWeek, type, type === 'lesson' ? 'Lesson' : 'Break');
    container.appendChild(createTimeStampElement(dayOfWeek, timestamp));
}

// Bindings //
document.querySelectorAll('.addLesson').forEach(button => {
    button.onclick = e => addItem(e.target.closest('.hoursContainer').id, 'lesson');
});

document.querySelectorAll('.addBreak').forEach(button => {
    button.onclick = e => addItem(e.target.closest('.hoursContainer').id, 'break');
});

document.querySelectorAll('.assignmentsButton').forEach(button => {
    button.onclick = () => {
        window.location.href = '/assignments/index.html';
    };
});

document.querySelectorAll('.examsButton').forEach(button => {
    button.onclick = () => {
        window.location.href = '/exams/index.html';
    };
});

document.getElementById('logoutButton').onclick = () => {
    sessionStorage.removeItem('SessionID');
    window.location.href = '/login/index.html';
}

window.addEventListener('DOMContentLoaded', () => {

    const sessionID = sessionStorage.getItem('SessionID');

    // Redirect to login if no SessionID
    if (!sessionID) {
        window.location.href = '/login/index.html';
        return;
    }
    loadSchedule();
});