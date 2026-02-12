package de.pls.stundenplaner.dto.response.auth;

import java.util.UUID;

public record LoginResponse(UUID sessionID) {
}