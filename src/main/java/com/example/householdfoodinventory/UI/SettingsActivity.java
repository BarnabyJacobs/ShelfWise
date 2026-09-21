package com.example.householdfoodinventory.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SessionManager sessionManager;

    private TextView emailStatusText, mfaStatusText, lastMfaText;
    private Button changePasswordButton, deleteAccountButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        bindViews();
        populateSecurityStatus();
        setupListeners();
    }

    private void bindViews() {
        emailStatusText = findViewById(R.id.emailStatusText);
        mfaStatusText = findViewById(R.id.mfaStatusText);
        lastMfaText = findViewById(R.id.lastMfaText);

        changePasswordButton = findViewById(R.id.changePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    /**
     * Displays email verification + MFA enrolment status + last MFA timestamp.
     */
    private void populateSecurityStatus() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            emailStatusText.setText("Not logged in");
            mfaStatusText.setText("Unknown");
            lastMfaText.setText("Unknown");
            return;
        }

        emailStatusText.setText(
                user.isEmailVerified() ? "Email verified" : "Email NOT verified"
        );

        // Load MFA status from Firestore (correct source)
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        mfaStatusText.setText("Unknown");
                        lastMfaText.setText("Unknown");
                        return;
                    }

                    var snapshot = task.getResult();

                    Boolean mfaEnrolled = snapshot.getBoolean("mfa_enrolled");
                    var lastMfaTimestamp = snapshot.getTimestamp("last_mfa");

                    if (mfaEnrolled != null && mfaEnrolled) {
                        mfaStatusText.setText("MFA enabled");
                    } else {
                        mfaStatusText.setText("MFA NOT enabled");
                    }

                    if (lastMfaTimestamp != null) {
                        lastMfaText.setText("Last MFA: " + lastMfaTimestamp.toDate());
                    } else {
                        lastMfaText.setText("Last MFA: Never");
                    }
                });
    }

    /**
     * Sets up button listeners for account actions.
     */
    private void setupListeners() {

        // ------------------------------------------------------------
        // CHANGE PASSWORD
        // ------------------------------------------------------------
        changePasswordButton.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_LONG).show();
                        } else {
                            String error = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Unknown error";
                            Toast.makeText(this, "Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // ------------------------------------------------------------
        // DELETE ACCOUNT
        // ------------------------------------------------------------
        deleteAccountButton.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sessionManager.logoutUser();
                    Toast.makeText(this, "Account deleted.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // ------------------------------------------------------------
        // LOGOUT
        // ------------------------------------------------------------
        logoutButton.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
