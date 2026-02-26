import { LOGIN_URL, REGISTER_URL } from "../modules/Config.js";
import { getSession } from "../modules/Security.js";

let SessionID = null;

// --- Utility functions ---

// Prüft, ob ein Wert "leer" ist
function isEmpty(value) {
    return value === null || value === undefined || (typeof value === "string" && value.trim() === "") || value === "null";
}

// Zeigt Feedback für 1,5 Sekunden an
function showResponse(response) {
    elements.feedbackElement.textContent = response;
    setTimeout(() => {
        elements.feedbackElement.textContent = "";
    }, 1500);
}

// Prüft, ob Session gültig ist
function isSessionValid(sessionID) {
    return !isEmpty(sessionID);
}

// --- Session Check beim Laden ---
async function checkSessionAndRedirect() {
    const sessionID = await getSession();
    if (isSessionValid(sessionID)) {
        window.location.href = "/home/index.html";
    }
}

checkSessionAndRedirect();

// --- DOM Elements ---
const elements = {
    usernameElement: document.getElementById("authUsername"),
    passwordElement: document.getElementById("authPassword"),
    feedbackElement: document.getElementById("authFeedback"),
    loginButton: document.getElementById("loginBtn"),
    registerButton: document.getElementById("registerBtn")
};

const errorMessages = {
    invalidLoginCredentials: "Invalid Username or Password.",
    invalidRegisterCredentials: "Username already exists.",
    emptyFields: "Please fill the input-fields.",
    defaultError: "Something went wrong"
}

// --- Login Funktion ---
async function login(username, password) {

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(errorMessages.emptyFields);
        return false;
    }

    let result;
    try {
        result = await fetch(LOGIN_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, password })
        });
    } catch (error) {
        showResponse(errorMessages.defaultError);
        return false;
    }

    if (!result.ok) {
        showResponse(errorMessages.invalidLoginCredentials);
        return false;
    }

    // Sicheres JSON Handling
    let data = {};
    try {
        const text = await result.text();
        if (text) {
            data = JSON.parse(text);
        }
    } catch (e) {
        console.warn("Server liefert kein JSON:", e);
        data = {};
    }

    // SessionID setzen
    SessionID = data.sessionID || sessionStorage.getItem("SessionID") || null;
    if (isSessionValid(SessionID)) {
        sessionStorage.setItem("SessionID", SessionID);
    }

    return true;
}

// --- Register Funktion ---
async function register(username, password) {

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(errorMessages.emptyFields);
        return false;
    }

    let result;
    try {
        result = await fetch(REGISTER_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, password })
        });
    } catch (error) {
        showResponse(errorMessages.defaultError);
        return false;
    }

    if (!result.ok) {
        showResponse(errorMessages.invalidRegisterCredentials);
        return false;
    }

    return true;
}

// --- Button Handlers ---
async function handleLoginPress() {
    const username = elements.usernameElement.value.trim();
    const password = elements.passwordElement.value.trim();

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(errorMessages.emptyFields);
        return;
    }

    const success = await login(username, password);
    if (success) {
        window.location.href = "/home/index.html";
    }
}

async function handleRegisterPress() {
    const username = elements.usernameElement.value.trim();
    const password = elements.passwordElement.value.trim();

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(errorMessages.emptyFields);
        return;
    }

    const registered = await register(username, password);
    if (!registered) return;

    const loggedIn = await login(username, password);
    if (loggedIn) {
        window.location.href = "/home/index.html";
    }
}

// --- UI Binding ---
function checkElementsAndBind() {
    if (!Object.values(elements).every(el => el !== null)) {
        console.error("Some required elements are missing!");
        return false;
    }
    return true;
}

function bindUI() {
    if (!checkElementsAndBind()) return;

    elements.loginButton.addEventListener("click", handleLoginPress);
    elements.registerButton.addEventListener("click", handleRegisterPress);
}

bindUI();