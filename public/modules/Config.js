export const HOST = "http://localhost:8080";  // ← Von 8081 zu 8080!

// Rest bleibt gleich
export const ASSIGNMENTS_URL = HOST + "/assignments/me";
export const EXAMS_URL = HOST + "/exams/me";

const AUTH_URL = HOST + "/auth";
export const LOGIN_URL = AUTH_URL + "/login";
export const REGISTER_URL = AUTH_URL + "/register";

export const HOME_URL = HOST + "/schedule/me";