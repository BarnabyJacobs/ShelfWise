package com.example.householdfoodinventory.Utils;

import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ExpiryCalculator
 *
 * Responsibilities:
 *  - Convert expiry date strings into calendar‑day differences.
 *  - Provide null‑safe, exception‑safe date arithmetic.
 *
 * Architectural Role:
 *  - SRP: Pure utility class.
 *  - Used by ExpiryService for business logic.
 *  - Contains no Android UI, business logic, or database logic.
 */
public class ExpiryCalculator {

    private static final String PATTERN = "dd/MM/yyyy";
    private static final long SAFE_FALLBACK = 999;

    public long daysToExpiry(String expiryDateString) {

        if (expiryDateString == null || expiryDateString.trim().isEmpty()) {
            return SAFE_FALLBACK;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                LocalDate expiryDate = parseModern(expiryDateString);
                LocalDate today = LocalDate.now();
                return expiryDate.toEpochDay() - today.toEpochDay();

            } else {

                Date expiryDate = parseLegacy(expiryDateString);
                Date today = new Date();
                long diff = expiryDate.getTime() - today.getTime();
                return TimeUnit.MILLISECONDS.toDays(diff);
            }

        } catch (Exception e) {
            return SAFE_FALLBACK;
        }
    }

    private LocalDate parseModern(String text) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);
        return LocalDate.parse(text.trim(), formatter);
    }

    private Date parseLegacy(String text) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.UK);
        sdf.setLenient(false);
        return sdf.parse(text.trim());
    }
}
