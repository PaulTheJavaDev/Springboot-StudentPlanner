package de.pls.stundenplaner.util;

import lombok.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {

    private static final BCryptPasswordEncoder BCRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private PasswordHasher() {
    }

    /**
     * Hashes a raw password using BCrypt (salted and adaptive).
     *
     * @param password Password to hash.
     * @return BCrypt hash string.
     */
    public static String hashPassword(
            final @NonNull String password
    ) {
        return BCRYPT_PASSWORD_ENCODER.encode(password);
    }

    /**
     * Verifies a raw password against a stored BCrypt hash.
     *
     * @param rawPassword Raw password from login request.
     * @param bcryptHash Stored BCrypt hash.
     * @return true if password matches.
     */
    public static boolean matchesPassword(
            final @NonNull String rawPassword,
            final @NonNull String bcryptHash
    ) {
        return BCRYPT_PASSWORD_ENCODER.matches(rawPassword, bcryptHash);
    }

    /**
     * Identifies legacy SHA-256 hex hashes used before BCrypt migration.
     */
    public static boolean isLegacySha256Hash(final String storedHash) {
        return storedHash != null && storedHash.matches("^[a-fA-F0-9]{64}$");
    }

    /**
     * Legacy one-way hash retained only for backward-compatible login migration.
     */
    public static String legacySha256(
            final @NonNull String password
    ) throws NoSuchAlgorithmException {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            final StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("SHA-256 not available.");
        }
    }
}
