package com.example.householdfoodinventory.Session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * SessionManager
 *
 * Responsibilities:
 *  - Manage login state using SharedPreferences.
 *  - Store Firebase UID + email.
 *  - Provide null‑safe accessors.
 *
 * Architectural Role:
 *  - SRP: Pure session utility.
 *  - Contains no Android UI, business logic, or MFA logic.
 */
public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;

    public SessionManager(@NonNull Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Create a new login session.
     * Called ONLY after successful MFA sign‑in or MFA enrolment.
     */
    public void createLoginSession(@NonNull String uid, @NonNull String email) {
        prefs.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /**
     * Clear all session data.
     */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /**
     * Public logout method.
     */
    public void logoutUser() {
        clearSession();
    }

    @NonNull
    public String getEmail() {
        String email = prefs.getString(KEY_EMAIL, "");
        return email == null ? "" : email;
    }

    @NonNull
    public String getUid() {
        String uid = prefs.getString(KEY_UID, "");
        return uid == null ? "" : uid;
    }

    /**
     * Basic session validity check.
     * Firebase reload + MFA checks happen in MainActivity.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Ensures UID exists AND logged‑in flag is true.
     */
    public boolean hasValidSession() {
        return isLoggedIn() && !getUid().isEmpty();
    }
}
