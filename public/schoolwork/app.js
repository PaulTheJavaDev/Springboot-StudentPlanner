import { validateSessionAuth } from "../modules/Security.js";
import { HOST, ASSIGNMENTS_URL, EXAMS_URL } from "../modules/Config.js";

validateSessionAuth();

const elements = {
  container: document.querySelector(".assignmentsContainer"),
  response: document.getElementById("responseLabel"),
  responder: document.getElementById("assignmentsResponder"),
  subject: document.getElementById("subjectSelect"),
  dueDate: document.getElementById("dueDateInput"),
  notes: document.getElementById("assignmentNotes"),
  submit: document.getElementById("submitAssignment"),
  modeSelect: document.getElementById("modeSelect")
};

const getMode = () => elements.modeSelect.value;
const getURL = () => getMode() === "exams" ? EXAMS_URL : ASSIGNMENTS_URL;

const fetchAll = async () => {
  const response = await fetch(getURL(), {
    method: "GET",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include"
  });
  return response.json();
};

const createEntry = async (data) => {
  const response = await fetch(getURL(), {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include",
    body: JSON.stringify(data)
  });

  if (!response.ok && (response.status === 400 || response.status === 422)) {
    showMessage("Please enter a valid future date!", 2);
    return;
  }

  return response.json();
};

const updateEntry = async (id, data) => {
  const response = await fetch(`${getURL()}/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include",
    body: JSON.stringify(data)
  });
  return response.json();
};

const deleteEntry = async (id) => {
  const response = await fetch(`${getURL()}/${id}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include"
  });
  return response.ok;
};

const fetchSubjects = async () => {
  const response = await fetch(`${HOST}/subjects`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include"
  });
  return response.json();
};

// Hilfsfunktionen
const showMessage = (message, seconds = 2) => {
  elements.response.textContent = message;
  setTimeout(() => {
    elements.response.textContent = "";
  }, seconds * 1000);
};

const formatDate = (date) => new Date(date).toLocaleDateString();
const getDate = (date) => date.split("T")[0];

const createElement = (tag, className, html, attributes = {}) => {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (html) element.innerHTML = html;
  Object.entries(attributes).forEach(([key, value]) => element[key] = value);
  return element;
};

const createField = (label, value, inputType, key) => {
  const wrapper = createElement("p", `assignment-${key}`);
  wrapper.innerHTML = `${label}: `;

  const text = createElement("span");
  text.textContent = inputType === "date" ? formatDate(value) : value;

  const input = createElement(inputType === "date" ? "input" : "textarea");
  if (inputType === "date") {
    input.type = "date";
    input.value = getDate(value);
  } else {
    input.value = value;
  }

  input.style.cssText = "width:100%;min-height:60px;border:1px solid #ccc;padding:5px;border-radius:6px";
  input.style.display = "none";

  wrapper.append(text, input);
  return { wrap: wrapper, text, input };
};

const toggleEdit = (state, fieldElements) => {
  state.isEdit = !state.isEdit;

  fieldElements.subject.contentEditable = state.isEdit;
  fieldElements.subject.style.cssText = state.isEdit
    ? "cursor:text;border:1px solid #ccc;padding:5px;border-radius:6px"
    : "";

  [fieldElements.dueDate, fieldElements.notes].forEach(({ text, input }) => {
    text.style.display = state.isEdit ? "none" : "inline";
    input.style.display = state.isEdit ? (input.tagName === "TEXTAREA" ? "block" : "inline") : "none";
  });

  fieldElements.completedCheckbox.style.display = state.isEdit ? "flex" : "none";
  fieldElements.completedText.style.display = state.isEdit ? "none" : "block";
  state.saveButton.style.display = state.isEdit ? "block" : "none";
};

const saveEdit = async (entry, fieldElements) => {
  const subject = fieldElements.subject.textContent.trim();
  const dueDate = fieldElements.dueDate.input.value;
  const notes = fieldElements.notes.input.value.trim();
  const completed = fieldElements.completedCheckbox.checked;

  if (!subject || !dueDate || !notes) {
    showMessage("Please fill in all fields.", 1.5);
    return false; 
  }

  Object.assign(entry, { subject, dueDate, notes, completed });
  await updateEntry(entry.id, entry);

  fieldElements.dueDate.text.textContent = formatDate(entry.dueDate);
  fieldElements.notes.text.textContent = entry.notes;
  fieldElements.completedText.innerHTML = `Completed: ${entry.completed}`;

  return true;
};

const showMenu = (wrapper, entry, fieldElements, state) => {
  const existing = document.querySelector(".assignment-popup");
  if (existing) return existing.remove();

  const popup = createElement("div", "assignment-popup");

  const editButton = createElement("button", null, "Edit");
  editButton.onclick = () => {
    toggleEdit(state, fieldElements);
    popup.remove();
  };

  const deleteButton = createElement("button", null, "Delete");
  deleteButton.onclick = async () => {
    const success = await deleteEntry(entry.id);
    if (success) {
      elements.container.removeChild(wrapper);
      popup.remove();
    }
  };

  const completedButton = createElement("button", null, entry.completed ? "Mark as Incomplete" : "Mark as Completed");
  completedButton.onclick = async () => {
    entry.completed = !entry.completed;
    const success = await updateEntry(entry.id, entry);
    if (success) {
      fieldElements.completedText.innerHTML = `Completed: ${entry.completed}`;
      popup.remove();
    }
  };

  popup.append(editButton, completedButton, deleteButton);
  wrapper.appendChild(popup);
};

const createCard = (entry) => {
  const wrapper = createElement("div", "assignment");
  wrapper.style.position = "relative";

  const subject = createElement("h3", "assignment-subject", entry.subject);
  const dueDate = createField("Due date", entry.dueDate, "date", "due-date");
  const notes = createField("Notes", entry.notes, "textarea", "notes");

  const completedText = createElement("p", "assignment-completed", `Completed: ${entry.completed}`);

  const completedCheckbox = createElement("label");
  completedCheckbox.style.cssText = "display:none;align-items:center;gap:0.5rem;font-size:0.9rem";
  const checkbox = createElement("input");
  checkbox.type = "checkbox";
  checkbox.checked = entry.completed;
  completedCheckbox.innerHTML = "Completed: ";
  completedCheckbox.appendChild(checkbox);

  const state = { isEdit: false };

  const saveButton = createElement("button", "assignment-save-button", "Save");
  saveButton.style.cssText = "display:none;margin-top:0.5rem;padding:0.4rem 0.8rem;border-radius:6px;border:1px solid #ccc;background:white;cursor:pointer";
  state.saveButton = saveButton;

  const fieldElements = { subject, dueDate, notes, completedCheckbox: checkbox, completedText };

  const ableToSave = () => {
    const s = fieldElements.subject.textContent.trim();
    const d = fieldElements.dueDate.input.value;
    const n = fieldElements.notes.input.value.trim();
    if (!s || !d || !n) {
      showMessage("Please fill in all fields.", 1.5);
      return false;
    }
    return true;
  };

  saveButton.onclick = async () => {
    if (ableToSave()) {
      const success = await saveEdit(entry, fieldElements);
      if (success) {
        toggleEdit(state, fieldElements);
      } else {
        checkbox.checked = entry.completed;
      }
    } else {
      checkbox.checked = entry.completed;
    }
  };

  const menuBtn = createElement("button", "assignment-menu-button", "⋮");
  menuBtn.onclick = () => showMenu(wrapper, entry, fieldElements, state);

  wrapper.append(subject, dueDate.wrap, notes.wrap, completedText, completedCheckbox, saveButton, menuBtn);
  return wrapper;
};

const noEntriesCheck = (length) => {
  const mode = getMode();
  elements.responder.textContent = length === 0
    ? `No ${mode === "exams" ? "Exams" : "Assignments"} yet`
    : "";
};

const loadEntries = async () => {
  elements.container.innerHTML = "";
  const entries = await fetchAll();
  noEntriesCheck(entries.length);
  entries.forEach(entry => elements.container.appendChild(createCard(entry)));
};

const loadSubjects = async () => {
  const subjects = await fetchSubjects();
  subjects.forEach(subject =>
    elements.subject.appendChild(createElement("option", "subjectOption", subject, { value: subject }))
  );
};

const handleSubmit = async () => {
  const entryData = {
    subject: elements.subject.value.toUpperCase().replace(" ", "_"),
    dueDate: elements.dueDate.value,
    notes: elements.notes.value,
    completed: false
  };

  if (!entryData.subject || !entryData.dueDate || !entryData.notes) {
    return showMessage("Please fill in all fields.", 1.5);
  }

  const newEntry = await createEntry(entryData);
  if (newEntry) {
    elements.responder.textContent = "";
    elements.container.appendChild(createCard(newEntry));
    [elements.subject, elements.dueDate, elements.notes].forEach(el => el.value = "");
  }
};

elements.modeSelect.addEventListener("change", loadEntries);
elements.submit.onclick = handleSubmit;

window.addEventListener("DOMContentLoaded", () => {
  loadEntries();
  loadSubjects();
});