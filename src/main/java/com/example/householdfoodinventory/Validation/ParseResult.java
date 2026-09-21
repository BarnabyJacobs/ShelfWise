package com.example.householdfoodinventory.Validation;

/**
 * ParseResult<T>
 *
 * Generic value object representing the outcome of a parsing operation.
 *
 * Responsibilities:
 *  - Encapsulate success/failure state.
 *  - Carry either a parsed value (on success) or an error message (on failure).
 *  - Provide a clean, immutable structure for InputParser and other utilities.
 *
 * Architectural Role:
 *  - Part of the domain/validation layer.
 *  - Contains no Android dependencies and is fully unit‑testable.
 *  - Used by InputParser to avoid throwing exceptions for invalid input.
 *
 * Design Notes:
 *  - Immutable: all fields are final and set only via the private constructor.
 *  - Generic: supports any parsed type (Double, Integer, etc.).
 *  - Factory methods (success() / error()) improve readability and prevent misuse.
 */
public class ParseResult<T> {

    private final boolean success;
    private final T value;
    private final String error;

    /**
     * Private constructor ensures controlled creation via factory methods.
     */
    private ParseResult(boolean success, T value, String error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful parse result.
     *
     * @param value The parsed value.
     * @return ParseResult representing success.
     */
    public static <T> ParseResult<T> success(T value) {
        return new ParseResult<>(true, value, null);
    }

    /**
     * Creates a failed parse result.
     *
     * @param error A human‑readable explanation of the failure.
     * @return ParseResult representing an error.
     */
    public static <T> ParseResult<T> error(String error) {
        return new ParseResult<>(false, null, error);
    }

    /**
     * @return true if parsing succeeded; false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The parsed value, or null if parsing failed.
     */
    public T getValue() {
        return value;
    }

    /**
     * @return The error message, or null if parsing succeeded.
     */
    public String getError() {
        return error;
    }
}
