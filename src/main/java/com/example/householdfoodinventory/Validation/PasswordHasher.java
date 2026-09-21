package com.example.householdfoodinventory.Validation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordHasher
 *
 * Purpose:
 *  - Provides a secure, one‑way hashing function for user passwords.
 *  - Uses SHA‑256 to convert a raw password into a hexadecimal hash string.
 *  - Ensures that plaintext passwords are never stored in the database.
 *
 * Architectural Role:
 *  - Part of the security/validation layer.
 *  - Used by RegisterService when creating new accounts.
 *  - Used by LoginService to hash the user’s input and compare it with the stored hash.
 *
 * Security Notes:
 *  - SHA‑256 is a strong cryptographic hash function suitable for offline apps.
 *  - For production systems, a salted hashing algorithm (e.g., bcrypt, Argon2) is recommended.
 *  - This implementation is intentionally simple for local‑device storage.
 *
 * Behaviour:
 *  - Converts the password into bytes.
 *  - Hashes the bytes using SHA‑256.
 *  - Converts the resulting byte array into a lowercase hexadecimal string.
 *
 * Testability:
 *  - Stateless and deterministic — ideal for unit testing.
 *  - No Android dependencies.
 */
public class PasswordHasher {

    public PasswordHasher() {
        // No state required — this class is intentionally stateless.
    }

    /**
     * Hashes a plaintext password using SHA‑256.
     *
     * @param password The raw password entered by the user.
     * @return A 64‑character hexadecimal string representing the SHA‑256 hash.
     *
     * @throws RuntimeException if SHA‑256 is not supported on the device
     *                          (extremely unlikely on modern Android).
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Hash the password bytes
            byte[] hashBytes = digest.digest(password.getBytes());

            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // pad single-digit hex values
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA‑256 should always be available — fail fast if not
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
