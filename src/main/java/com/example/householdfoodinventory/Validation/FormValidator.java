package com.example.householdfoodinventory.Validation;

import java.util.regex.Pattern;

/**
 * FormValidator
 *
 * Responsibilities:
 *  - Validate simple text inputs (names, dates, passwords, numbers).
 *  - Provide null‑safe checks and consistent formatting rules.
 *
 * Architectural Role:
 *  - SRP: Pure validation utility.
 *  - DIP: UI depends on this abstraction instead of embedding regex logic.
 *  - Contains no Android, business, or database logic.
 */
public class FormValidator {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9 ,.'-]{2,}$");

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$");

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    private static final Pattern QUANTITY_PATTERN =
            Pattern.compile("^\\d+$");

    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean validateName(String name) {
        if (name == null) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public boolean validateExpiry(String expiry) {
        if (expiry == null) return false;
        return DATE_PATTERN.matcher(expiry.trim()).matches();
    }

    public boolean validatePassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean validatePrice(String price) {
        if (price == null) return false;
        return PRICE_PATTERN.matcher(price.trim()).matches();
    }

    public boolean validateQuantity(String qty) {
        if (qty == null) return false;
        return QUANTITY_PATTERN.matcher(qty.trim()).matches();
    }

    public String getRequiredMessage(String fieldName) {
        return fieldName + " required";
    }
}
