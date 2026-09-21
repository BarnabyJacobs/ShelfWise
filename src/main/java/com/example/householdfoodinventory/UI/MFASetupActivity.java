package com.example.householdfoodinventory.UI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.TimeUnit;

public class MFASetupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String phoneNumber;

    private ProgressDialog loadingDialog;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this,
                    "No logged-in user. Please sign in again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadProfileAndStartEnrol(user);
    }

    private void loadProfileAndStartEnrol(FirebaseUser user) {
        String uid = user.getUid();

        loadingDialog.show();

        firestore.collection("profiles")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    loadingDialog.dismiss();
                    handleProfile(snapshot);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this,
                            "Failed to load profile.",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void handleProfile(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            Toast.makeText(this,
                    "Profile not found. Please register again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        phoneNumber = snapshot.getString("phone");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this,
                    "No phone number found. Please register again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Stronger validation
        if (!phoneNumber.matches("^\\+[1-9]\\d{7,14}$")) {
            Toast.makeText(this,
                    "Invalid phone number format.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Confirm with user before starting MFA enrolment
        new AlertDialog.Builder(this)
                .setTitle("Enable Multi-Factor Authentication")
                .setMessage("We will send a verification code to:\n\n" + phoneNumber)
                .setPositiveButton("Continue", (d, w) -> startPhoneVerificationForEnrol())
                .setNegativeButton("Cancel", (d, w) -> finish())
                .show();
    }

    private void startPhoneVerificationForEnrol() {

        loadingDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(enrolCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks enrolCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {

                    if (isFinishing() || isDestroyed()) return;

                    loadingDialog.dismiss();
                    enrolMfa(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {

                    if (isFinishing() || isDestroyed()) return;

                    loadingDialog.dismiss();

                    Toast.makeText(MFASetupActivity.this,
                            "Verification failed. Please try again.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    if (isFinishing() || isDestroyed()) return;

                    loadingDialog.dismiss();

                    resendToken = token;
                    showEnrolCodeDialog(verificationId);
                }
            };

    private void showEnrolCodeDialog(String verificationId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter verification code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationId, code);
            enrolMfa(credential);
        });

        builder.setNeutralButton("Resend", (dialog, which) -> resendCode(verificationId));

        builder.show();
    }

    private void resendCode(String verificationId) {

        if (resendToken == null) {
            Toast.makeText(this,
                    "Unable to resend code. Please try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        loadingDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setForceResendingToken(resendToken)
                        .setCallbacks(enrolCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void enrolMfa(PhoneAuthCredential credential) {

        loadingDialog.show();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            loadingDialog.dismiss();
            Toast.makeText(this,
                    "No logged-in user. Please sign in again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        MultiFactorAssertion assertion =
                PhoneMultiFactorGenerator.getAssertion(credential);

        user.getMultiFactor()
                .enroll(assertion, "Primary phone")
                .addOnCompleteListener(task -> {

                    loadingDialog.dismiss();

                    if (task.isSuccessful()) {
                        updateProfileMfaFlag(user.getUid());
                    } else {
                        Toast.makeText(this,
                                "MFA enrolment failed. Please try again.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void updateProfileMfaFlag(String uid) {

        loadingDialog.show();

        firestore.collection("profiles")
                .document(uid)
                .update("mfa_enrolled", true)
                .addOnSuccessListener(unused -> {

                    loadingDialog.dismiss();

                    Toast.makeText(this,
                            "MFA enabled successfully.",
                            Toast.LENGTH_LONG).show();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {

                    loadingDialog.dismiss();

                    Toast.makeText(this,
                            "Failed to update profile.",
                            Toast.LENGTH_LONG).show();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }
}
