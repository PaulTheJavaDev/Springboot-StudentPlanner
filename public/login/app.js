import { getSessionID } from "../modules/Security.js";
import { LOGIN_URL, REGISTER_URL } from "../modules/Config.js";

let sessionID = getSessionID();

if (sessionID !== null) {
    window.location.href = "../home/index.html";
}

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
    epmtyFields: "Please fill the input-fields.",
    defaultError: "Something went wrong"
}

async function login(
    username, 
    password
) {

    const result = await fetch(LOGIN_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify( { username, password } )
    });

    if (!result.ok) {
        showResponse(errorMessages.invalidLoginCredentials);
        return false;
    }

    const data = await result.json();
    sessionID = data.sessionID;
    sessionStorage.setItem("SessionID", data.sessionID);

    return true;
}

async function register(username, password) {

    let result;

    try {

        result = await fetch(REGISTER_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify( { username, password } )
        });

    } catch {
        showResponse(errorMessages.defaultError);
        return false;
    }
    
    if (!result.ok) {
        showResponse(errorMessages.invalidRegisterCredentials);
        return false;
    }

    return true;
}

/**
 * Makes a Request to the backend and decide if the user can login or not.
 */
async function handleLoginPress() {

    const username = elements.usernameElement.value.trim();
    const password = elements.passwordElement.value.trim();

    if (!username || !password) {
        showResponse(errorMessages.epmtyFields);
        return;
    }

    const success = await login(username, password);
    if (success) {
        window.location.href = "/home/index.html";
    }

}

/**
 * Makes a Request to the backend and decide if the user can register or not.
 */
async function handleRegisterPress() {
    
    const username = elements.usernameElement.value.trim();
    const password = elements.passwordElement.value.trim();

    if (!username || !password) {
        showResponse(errorMessages.epmtyFields);
        return;
    }

    const registered = await register(username, password);
    if (!registered) {
        return;
    }

    const loggedIn = await login(username, password);
    if (loggedIn) {
        window.location.href = "/home/index.html";
    }
}

/**
 * Connects all buttons with their corresponding functions
 */
function bindUI() {

    if (!checkElementsAndBind()) return;
    
    const loginButton = elements.loginButton;
    const registerButton = elements.registerButton;

    loginButton.addEventListener("click", handleLoginPress);
    registerButton.addEventListener("click", handleRegisterPress);
}

/**
 * Displays a response to the user.
 * @param {*} response A string that will be displayed in the feedback element.
 */
function showResponse(response) {
    const time = 1.5;
    setTimeout(() => {
        elements.feedbackElement.textContent = response;
    }, time);
    elements.feedbackElement.textContent = "";
}

function checkElementsAndBind() {
    if (!Object.values(elements).every(element => element !== null)) {
        console.error("Some required elements are missing!");
        return false;
    }
    return true;
}

bindUI();
