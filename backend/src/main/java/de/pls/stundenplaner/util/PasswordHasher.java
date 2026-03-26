package de.pls.stundenplaner.util;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {

    /**
     * Irreversible Encoding method using the built-in Java "SHA-256" Algorithm.
     *
     * @param password Password to encode.
     * @return The encoded Password.
     */
    public static String sha256(
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
