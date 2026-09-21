package com.example.householdfoodinventory.Validation;

/**
 * RegistrationResult
 *
 * Represents the outcome of a user registration attempt.
 *
 * Responsibilities:
 *  - Encapsulate success/failure state.
 *  - Provide a human‑readable message.
 *  - Provide a clean, immutable value object for RegisterService and RegisterActivity.
 *
 * Architectural Role:
 *  - Part of the domain/validation layer.
 *  - Contains no Android dependencies and is fully unit‑testable.
 *  - Allows RegisterService to return structured results instead of throwing exceptions
 *    or relying on UI‑layer error handling.
 *
 * Design Notes:
 *  - Immutable: all fields are final and set only via the private constructor.
 *  - Factory methods (success() / error()) prevent misuse and improve readability.
 *  - Message is always present, even for success ("OK").
 */
public class RegistrationResult {

    private final boolean success;
    private final String message;

    /**
     * Private constructor ensures controlled creation via factory methods.
     */
    private RegistrationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful registration result.
     *
     * @return RegistrationResult representing success, with message "OK".
     */
    public static RegistrationResult success() {
        return new RegistrationResult(true, "OK");
    }

    /**
     * Creates a failed registration result.
     *
     * @param message A human‑readable explanation of the failure.
     * @return RegistrationResult representing an error.
     */
    public static RegistrationResult error(String message) {
        return new RegistrationResult(false, message);
    }

    /**
     * @return true if registration succeeded; false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The message associated with the result (e.g., "Username already taken").
     */
    public String getMessage() {
        return message;
    }
}
