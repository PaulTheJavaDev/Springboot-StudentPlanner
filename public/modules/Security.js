import { HOST } from "../modules/Config.js";

export async function getSession() {
    try {
        const result = await fetch(HOST + "/auth/check", {
            method: "GET",
            credentials: "include"
        });

        if (!result.ok) {
            return null;
        }

        console.log("Session check result:", result);

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