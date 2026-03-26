import { AUTH_URL } from "../modules/Config.js";

export async function getSession() {
    try {
        const result = await fetch(AUTH_URL + "/check", {
            method: "GET",
            credentials: "include"
        });

        if (!result.ok) {
            return null;
        }

        const data = await result.json();
        return data ? true : null;
    } catch (error) {
        console.error("Error fetching session ID:", error);
        return null;
    }
}

export async function validateSessionAuth() {

    const SessionID = await getSession();

    if (!SessionID) {
        location.href = "/login/index.html";
        console.warn("No valid session found, redirecting to login.");
    }
    
}