package com.example.householdfoodinventory.Validation;

/**
 * InputParser
 *
 * Responsibilities:
 *  - Convert raw text into numeric values.
 *  - Fail safely using ParseResult<T>.
 *
 * Architectural Role:
 *  - SRP: Pure parsing utility.
 *  - Contains no validation, business logic, or Android dependencies.
 */
public class InputParser {

    public ParseResult<Double> parsePrice(String text) {

        if (text == null || text.trim().isEmpty()) {
            return ParseResult.error("Price required");
        }

        try {
            double value = Double.parseDouble(text.trim());
            return ParseResult.success(value);

        } catch (Exception e) {
            return ParseResult.error("Invalid price");
        }
    }

    public ParseResult<Integer> parseQuantity(String text) {

        if (text == null || text.trim().isEmpty()) {
            return ParseResult.error("Quantity required");
        }

        try {
            int value = Integer.parseInt(text.trim());
            return ParseResult.success(value);

        } catch (Exception e) {
            return ParseResult.error("Invalid quantity");
        }
    }
}
