package de.pls.stundenplaner.dto.request.auth;

/**
 * DTO for the password and newPassword of a User for a Password change
 */
@SuppressWarnings("all")
public record ChangePasswordRequest(String username, String password) {
}