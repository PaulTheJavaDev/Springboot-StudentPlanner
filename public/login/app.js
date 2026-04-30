import { LOGIN_URL, REGISTER_URL } from "../modules/Config.js";
import { getSession } from "../modules/Security.js";
import { UserSpecificErrors, LoginErrors, defaultError } from "../modules/Errors.js";

let SessionID;

function isEmpty(value) {
    return value === null || value === undefined || (typeof value === "string" && value.trim() === "") || value === "null";
}

function showResponse(response) {
    elements.feedbackElement.textContent = response;
    setTimeout(() => {
        elements.feedbackElement.textContent = "";
    }, 1500);
}

function isSessionValid(sessionID) {
    return !isEmpty(sessionID);
}

async function checkSessionAndRedirect() {
    const sessionID = await getSession();
    if (isSessionValid(sessionID)) {
        window.location.href = "/home/index.html";
    }
}

checkSessionAndRedirect();

const elements = {
    usernameElement: document.getElementById("authUsername"),
    passwordElement: document.getElementById("authPassword"),
    feedbackElement: document.getElementById("authFeedback"),
    loginButton: document.getElementById("loginBtn"),
    registerButton: document.getElementById("registerBtn")
};

async function login(username, password) {

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(LoginErrors.emptyFields);
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
        showResponse(defaultError);
        return false;
    }

    if (result.status === 401) {
        showResponse(LoginErrors.invalidCredentials);
        return false;
    }

    if (!result.ok) {
        showResponse(defaultError);
        return false;
    }

    let data = {};
    try {
        const text = await result.text();
        if (text) {
            data = JSON.parse(text);
        }
    } catch (error) {
        console.warn("Server liefert kein JSON:", error);
        data = {};
    }

    SessionID = data.sessionID || sessionStorage.getItem("SessionID") || null;
    if (isSessionValid(SessionID)) {
        sessionStorage.setItem("SessionID", SessionID);
    }

    return true;
}

async function register(username, password) {

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(LoginErrors.emptyFields);
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
        showResponse(defaultError);
        return false;
    }

    if (!result.ok) {
        if (result.status === 409) {
            showResponse(UserSpecificErrors.usernameAlreadyExists);
            return false;
        }
        showResponse(defaultError);
        return false;
    }

    return true;
}

async function handleLoginPress() {
    const username = elements.usernameElement.value.trim();
    const password = elements.passwordElement.value.trim();

    if (isEmpty(username) || isEmpty(password)) {
        showResponse(LoginErrors.emptyFields);
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
        showResponse(LoginErrors.emptyFields);
        return;
    }

    const registered = await register(username, password);
    if (!registered) return;

    const loggedIn = await login(username, password);
    if (loggedIn) {
        window.location.href = "/home/index.html";
    }
}

function checkElementsAndBind() {
    if (!Object.values(elements).every(element => element !== null)) {
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