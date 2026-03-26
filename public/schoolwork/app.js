import { validateSessionAuth } from "../modules/Security.js";
import { HOST, ASSIGNMENTS_URL, EXAMS_URL } from "../modules/Config.js";

const elements = {
  container:  document.querySelector(".assignmentsContainer"),
  response:   document.getElementById("responseLabel"),
  responder:  document.getElementById("assignmentsResponder"),
  subject:    document.getElementById("subjectSelect"),
  dueDate:    document.getElementById("dueDateInput"),
  notes:      document.getElementById("assignmentNotes"),
  submit:     document.getElementById("submitAssignment"),
  modeSelect: document.getElementById("modeSelect")
};

const getMode = () => elements.modeSelect.value;
const getURL  = () => getMode() === "exams" ? EXAMS_URL : ASSIGNMENTS_URL;

// ── API ──────────────────────────────────────────────────────────────────────

const apiFetch = (url, method = "GET", body) =>
  fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    ...(body && { body: JSON.stringify(body) })
  });

const fetchAllEntries = ()          => apiFetch(getURL()).then(r => r.json());
const fetchSubjects   = ()          => apiFetch(`${HOST}/subjects`).then(r => r.json());
const updateEntry     = (id, data)  => apiFetch(`${getURL()}/${id}`, "PUT", data).then(r => r.json());
const deleteEntry     = async id    => (await apiFetch(`${getURL()}/${id}`, "DELETE")).ok;

const createEntry = async (data) => {
  const response = await apiFetch(getURL(), "POST", data);

  if (!response.ok && response.status === 400) {
    showMessage("Please enter a valid future date!", 2);
    return;
  }

  return response.json();
};

const showMessage = (message, seconds = 2) => {
  elements.response.textContent = message;
  setTimeout(() => elements.response.textContent = "", seconds * 1000);
};

const formatDate = date => new Date(date).toLocaleDateString();
const stripTime  = date => date.split("T")[0];

const createElement = (tag, className, innerHTML, props = {}) => {
  const element = document.createElement(tag);
  if (className)  element.className = className;
  if (innerHTML)  element.innerHTML = innerHTML;
  Object.entries(props).forEach(([key, value]) => element[key] = value);
  return element;
};

const createField = (label, value, type, key) => {
  const wrapper   = createElement("p", `assignment-${key}`, `${label}: `);
  const textNode  = createElement("span");
  const inputNode = createElement(type === "date" ? "input" : "textarea");

  textNode.textContent = type === "date" ? formatDate(value) : value;

  if (type === "date") { inputNode.type = "date"; inputNode.value = stripTime(value); }
  else                 { inputNode.value = value; }

  inputNode.style.cssText = "width:100%;min-height:60px;border:1px solid #ccc;padding:5px;border-radius:6px;display:none";
  wrapper.append(textNode, inputNode);
  return { wrap: wrapper, text: textNode, input: inputNode };
};

// Edit mode
const toggleEdit = (state, fields) => {

  state.isEdit = !state.isEdit;
  const isEditing = state.isEdit;

  fields.subject.contentEditable = isEditing;
  fields.subject.style.cssText   = isEditing ? "cursor:text;border:1px solid #ccc;padding:5px;border-radius:6px" : "";

  [fields.dueDate, fields.notes].forEach(({ text, input }) => {
    text.style.display  = isEditing ? "none"  : "inline";
    input.style.display = isEditing ? (input.tagName === "TEXTAREA" ? "block" : "inline") : "none";
  });

  fields.completedCheckbox.style.display = isEditing ? "flex"  : "none";
  fields.completedText.style.display     = isEditing ? "none"  : "block";
  state.saveButton.style.display         = isEditing ? "block" : "none";
};

const saveEdit = async (entry, fields) => {
  const subject   = fields.subject.textContent.trim();
  const dueDate   = fields.dueDate.input.value;
  const notes     = fields.notes.input.value.trim();
  const completed = fields.completedCheckbox.checked;

  if (!subject || !dueDate || !notes) {
    showMessage("Please fill in all fields.", 1.5);
    return false;
  }

  Object.assign(entry, { subject, dueDate, notes, completed });
  await updateEntry(entry.id, entry);

  fields.dueDate.text.textContent = formatDate(entry.dueDate);
  fields.notes.text.textContent   = entry.notes;
  fields.completedText.innerHTML  = `Completed: ${entry.completed}`;
  return true;
};

const showContextMenu = (card, entry, fields, state) => {
  const existingMenu = document.querySelector(".assignment-popup");
  if (existingMenu) return existingMenu.remove();

  const menu          = createElement("div", "assignment-popup");
  const editButton    = createElement("button", null, "Edit");
  const deleteButton  = createElement("button", null, "Delete");
  const toggleButton  = createElement("button", null, entry.completed ? "Mark as Incomplete" : "Mark as Completed");

  editButton.onclick = () => { toggleEdit(state, fields); menu.remove(); };

  deleteButton.onclick = async () => {
    if (await deleteEntry(entry.id)) { elements.container.removeChild(card); menu.remove(); }
  };

  toggleButton.onclick = async () => {
    entry.completed = !entry.completed;
    if (await updateEntry(entry.id, entry)) {
      fields.completedText.innerHTML = `Completed: ${entry.completed}`;
      menu.remove();
    }
  };

  menu.append(editButton, toggleButton, deleteButton);
  card.appendChild(menu);
};

const createCard = (entry) => {
  const card = createElement("div", "assignment");
  card.style.position = "relative";

  const subjectHeading    = createElement("h3", "assignment-subject", entry.subject);
  const dueDateField      = createField("Due date", entry.dueDate, "date",     "due-date");
  const notesField        = createField("Notes",    entry.notes,   "textarea", "notes");
  const completedText     = createElement("p", "assignment-completed", `Completed: false`);

  const completedLabel = createElement("label");
  completedLabel.style.cssText = "display:none;align-items:center;gap:0.5rem;font-size:0.9rem";
  completedLabel.innerHTML     = "Completed: ";
  const completedCheckbox = createElement("input", null, null, { type: "checkbox", checked: entry.completed });
  completedLabel.appendChild(completedCheckbox);

  const saveButton = createElement("button", "assignment-save-button", "Save");
  saveButton.style.cssText = "display:none;margin-top:0.5rem;padding:0.4rem 0.8rem;border-radius:6px;border:1px solid #ccc;background:white;cursor:pointer";

  const state  = { isEdit: false, saveButton };
  const fields = { subject: subjectHeading, dueDate: dueDateField, notes: notesField, completedCheckbox, completedText };

  const validateFields = () => {
    const isValid = fields.subject.textContent.trim() && fields.dueDate.input.value && fields.notes.input.value.trim();
    if (!isValid) showMessage("Please fill in all fields.", 1.5);
    return isValid;
  };

  saveButton.onclick = async () => {
    if (validateFields() && await saveEdit(entry, fields)) toggleEdit(state, fields);
    else completedCheckbox.checked = entry.completed;
  };

  const menuButton = createElement("button", "assignment-menu-button", "⋮");
  menuButton.onclick = () => showContextMenu(card, entry, fields, state);

  card.append(subjectHeading, dueDateField.wrap, notesField.wrap, completedText, completedLabel, saveButton, menuButton);
  return card;
};

const checkIfEmpty = (count) => {
  elements.responder.textContent = count === 0 ? `No ${getMode().splice(0, 1).toUpperCase() + getMode().slice(1)} yet` : "";
};

const loadEntries = async () => {
  elements.container.innerHTML = "";
  const entries = await fetchAllEntries();
  checkIfEmpty(entries.length);
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
    subject: elements.subject.value.toUpperCase().replace(/ /g, "_"),
    dueDate: elements.dueDate.value,
    notes:   elements.notes.value
  };

  if (!entryData.subject || !entryData.dueDate || !entryData.notes) return showMessage("Please fill in all fields.", 1.5);

  const newEntry = await createEntry(entryData);
  if (newEntry) {
    elements.responder.textContent = "";
    elements.container.appendChild(createCard(newEntry));
    [elements.subject, elements.dueDate, elements.notes].forEach(element => element.value = "");
  }
};

elements.modeSelect.addEventListener("change", loadEntries);
elements.submit.onclick = handleSubmit;

window.addEventListener("DOMContentLoaded", () => {
  validateSessionAuth();
  loadEntries();
  loadSubjects();
});