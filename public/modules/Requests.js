import {LOCALHOST, SCHEDULE_URL} from "./Config.js";

const httpMethods = {
    GET: "GET",
    POST: "POST",
    PUT: "PUT",
    DELETE: "DELETE"
}

async function apiRequest(method, url, data) {
    try {
        const response = await fetch(url, {
            method,
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: data ? JSON.stringify(data) : undefined
        });

        // Try to parse JSON, but fall back to text
        const parseResponse = async () => {
            const text = await response.text();
            try {
                return JSON.parse(text);
            } catch {
                return text;
            }
        };

        if (!response.ok) {
            const errorBody = await parseResponse();
            return {
                ok: false,
                status: response.status,
                statusText: response.statusText,
                error: errorBody
            };
        }

        // DELETE and 204 responses often return no body
        if (method === "DELETE" || response.status === 204) {
            return { ok: true };
        }

        const body = await parseResponse();
        return { ok: true, data: body };

    } catch (error) {
        // Network or fetch-level error
        return {
            ok: false,
            status: 0,
            statusText: "Network error",
            error: error.message || error
        };
    }
}

export async function getSubjects() {
    return apiRequest(httpMethods.GET, `${LOCALHOST}/subjects`);
}

export async function requestScheduler() {
    return apiRequest(httpMethods.GET, SCHEDULE_URL);
}

export async function createScheduleStamp(data, dayOfWeek) {
    return apiRequest(httpMethods.POST, `${SCHEDULE_URL}/${dayOfWeek}`, data);
}

export async function updateScheduleStamp(dayOfWeek, data) {
    return apiRequest(httpMethods.PUT, `${SCHEDULE_URL}/${dayOfWeek}/${data.id}`, data);
}

export async function deleteScheduleStamp(url) {
    return apiRequest(httpMethods.DELETE, url);
}

export async function logout() {
    return apiRequest(httpMethods.POST, `${LOCALHOST}/logout`);
}

export async function logoutAndRedirect() {
    await logout();
    location.href = "/login/index.html";
}