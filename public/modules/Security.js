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
            return data.sessionID;
        } catch (error) {
            console.error("Error fetching session ID:", error);
            return null;
        }
}

export function validateSessionAuth() {

    const SessionID = getSessionID();

    if (!SessionID || SessionID.trim() === "" || SessionID === "null") {
        window.location.href = "/login";
    }
    
}