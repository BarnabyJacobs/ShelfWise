package com.example.householdfoodinventory.UI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Session.SessionManager;

/**
 * SplashActivity
 *
 * Entry point of the ShelfWise application.
 *
 * Responsibilities:
 *  - Display the branded splash screen using the Android 12+ SplashScreen API.
 *  - Keep the splash visible briefly for branding polish.
 *  - Check whether the user is already logged in.
 *  - Route the user to MainActivity or LoginActivity accordingly.
 *
 * Architectural Role:
 *  - UI-only class: contains no business logic and no database access.
 *  - Delegates session handling to SessionManager.
 *
 * Notes:
 *  - Uses Handler.postDelayed() to control splash duration.
 *  - finish() ensures the splash screen cannot be returned to via back button.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ------------------------------------------------------------
        // Duration the splash screen remains visible
        // ------------------------------------------------------------
        final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

        // ------------------------------------------------------------
        // Install the Android 12+ splash screen
        // This automatically displays the splash drawable defined in XML.
        // ------------------------------------------------------------
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Session manager for checking login state
        SessionManager sessionManager = new SessionManager(this);

        // ------------------------------------------------------------
        // Delay navigation so the splash screen is visible long enough
        // for branding and visual polish.
        // ------------------------------------------------------------
        new Handler().postDelayed(() -> {

            Intent intent;

            // ------------------------------------------------------------
            // Decide where to send the user:
            //  - If logged in → MainActivity
            //  - Otherwise → LoginActivity
            // ------------------------------------------------------------
            if (sessionManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);

            // Prevent returning to splash screen
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }
}
