package com.example.householdfoodinventory.Validation;

/**
 * LoginResult
 *
 * Represents the outcome of a login attempt.
 *
 * Responsibilities:
 *  - Encapsulate success/failure state.
 *  - Provide a human‑readable message.
 *  - Carry the authenticated user's ID on success.
 *
 * Architectural Role:
 *  - Part of the domain/validation layer.
 *  - Contains no Android dependencies and is safe for unit testing.
 *  - Used by LoginService to return structured results instead of throwing exceptions.
 *  - Used by LoginActivity to decide UI behaviour (navigation, error messages).
 *
 * Design Notes:
 *  - Immutable: all fields are final and set only via the private constructor.
 *  - Factory methods (success() / error()) prevent misuse and improve readability.
 *  - On success, userId is included; on failure, userId = -1.
 */
public class LoginResult {

    private final boolean success;
    private final String message;
    private final int userId;

    /**
     * Private constructor ensures controlled creation via factory methods.
     */
    private LoginResult(boolean success, String message, int userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    /**
     * Creates a successful login result.
     *
     * @param userId The authenticated user's ID.
     * @return LoginResult representing success.
     */
    public static LoginResult success(int userId) {
        return new LoginResult(true, "OK", userId);
    }

    /**
     * Creates a failed login result.
     *
     * @param message A human‑readable explanation of the failure.
     * @return LoginResult representing an error.
     */
    public static LoginResult error(String message) {
        return new LoginResult(false, message, -1);
    }

    /**
     * @return true if login succeeded; false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The message associated with the result.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The authenticated user's ID, or -1 if login failed.
     */
    public int getUserId() {
        return userId;
    }
}
