package com.example.householdfoodinventory.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.MultiFactorAssertion;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneMultiFactorGenerator;

import java.util.concurrent.TimeUnit;

/**
 * EnableMfaActivity
 *
 * Responsibilities:
 *  - Enrol a phone-based second factor for MFA.
 *  - Handle SMS verification and MFA assertion creation.
 *
 * Architectural Role:
 *  - SRP: Only handles MFA enrolment.
 *  - Keeps LoginActivity focused on authentication + MFA sign-in.
 *  - Launched from SettingsActivity (account management).
 */
public class EnableMfaActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText phoneEditText;
    private Button enableMfaButton;

    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_mfa);

        auth = FirebaseAuth.getInstance();

        phoneEditText = findViewById(R.id.editMobileNumber);
        enableMfaButton = findViewById(R.id.enableMfaButton);
        // Prevent re-enrolment
        if (!auth.getCurrentUser().getMultiFactor().getEnrolledFactors().isEmpty()) {
            Toast.makeText(this, "MFA is already enabled.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        enableMfaButton.setOnClickListener(v -> startMfaEnrollment());
    }

    private void startMfaEnrollment() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "You must be logged in to enable MFA.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!auth.getCurrentUser().isEmailVerified()) {
            Toast.makeText(this,
                    "Please verify your email before enabling MFA.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = phoneEditText.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            phoneEditText.setError("Phone number required");
            return;
        }

        if (!phoneNumber.startsWith("+")) {
            phoneEditText.setError("Include country code (e.g., +44)");
            return;
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    enrollMfa(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(EnableMfaActivity.this,
                            "Verification failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String id,
                                       PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = id;
                    showSmsCodeDialog();
                }
            };

    private void showSmsCodeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter verification code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationId, code);
            enrollMfa(credential);
        });

        builder.show();
    }

    private void enrollMfa(PhoneAuthCredential credential) {

        MultiFactorAssertion assertion =
                PhoneMultiFactorGenerator.getAssertion(credential);

        auth.getCurrentUser().getMultiFactor().enroll(assertion, "My phone")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "MFA enabled successfully.",
                                Toast.LENGTH_LONG).show();

                        // Return to SettingsActivity
                        startActivity(new Intent(this, SettingsActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "MFA enrolment failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
