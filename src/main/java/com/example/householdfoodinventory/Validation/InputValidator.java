package com.example.householdfoodinventory.Validation;

/**
 * InputValidator
 *
 * Purpose:
 *  - Provides reusable validation logic for usernames and passwords.
 *  - Enforces password strength rules used during registration and login.
 *  - Enforces username format rules to ensure consistency and prevent invalid input.
 *
 * Architectural Role:
 *  - Part of the domain/validation layer.
 *  - Contains no Android UI code and is safe for unit testing.
 *  - Used by RegisterService and LoginService to validate user input before database operations.
 *
 * Notes:
 *  - The hashPassword() method is currently a placeholder and does not perform hashing.
 *    Password hashing is handled elsewhere (e.g., in RegisterService or a dedicated PasswordHasher).
 *  - Validation rules are intentionally strict to prevent weak credentials.
 */
public class InputValidator {

    // Placeholder field — currently unused
    private String hashedPassword;

    public InputValidator() {
        // No state required — this class is intentionally stateless.
    }

    /**
     * Placeholder hashing method.
     *
     * @param password The raw password.
     * @return Currently returns the uninitialised hashedPassword field.
     *
     * Notes:
     *  - This method is not used in the current architecture.
     *  - Password hashing is performed in the service layer instead.
     *  - This method may be removed or replaced in future refactoring.
     */
    public String hashPassword(String password) {
        return hashedPassword;
    }

    /**
     * Validates a password against the application's security rules.
     *
     * Rules:
     *  - Minimum length: 8 characters
     *  - Must contain at least one number
     *  - Must contain at least one special character
     *  - Must contain at least one uppercase letter
     *
     * @param password The password to validate.
     * @return true if all rules are satisfied; false otherwise.
     */
    public boolean validatePassword(String password) {
        return hasMinLength(password)
                && hasNumber(password)
                && hasSpecial(password)
                && hasUppercase(password);
    }

    /**
     * Validates a username against the application's formatting rules.
     *
     * Rules:
     *  - Length between 3 and 20 characters
     *  - Letters, numbers, and underscores only
     *
     * @param username The username to validate.
     * @return true if valid; false otherwise.
     */
    public boolean validateUsername(String username) {
        return hasValidLength(username)
                && hasValidCharacters(username);
    }

    // -------------------------
    // Password rule helpers
    // -------------------------

    private boolean hasMinLength(String password) {
        return password.length() >= 8;
    }

    private boolean hasNumber(String password) {
        return password.matches(".*[0-9].*");
    }

    private boolean hasSpecial(String password) {
        return password.matches(".*[^A-Za-z0-9].*");
    }

    private boolean hasUppercase(String password) {
        return password.matches(".*[A-Z].*");
    }

    // -------------------------
    // Username rule helpers
    // -------------------------

    private boolean hasValidLength(String username) {
        return username.length() >= 3 && username.length() <= 20;
    }

    private boolean hasValidCharacters(String username) {
        return username.matches("^[A-Za-z0-9_]+$");
    }
}
