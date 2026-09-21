package com.example.householdfoodinventory.UI;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, passwordField, phoneField;
    private Button registerButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);
        phoneField = findViewById(R.id.editMobileNumber);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    registerButton.setEnabled(true);

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this,
                                "Unexpected error: user is null.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ------------------------------------------------------------
                    // SEND VERIFICATION EMAIL FIRST (always succeeds if user exists)
                    // ------------------------------------------------------------
                    user.sendEmailVerification();

                    // ------------------------------------------------------------
                    // SAVE PROFILE (async, does NOT block user experience)
                    // ------------------------------------------------------------
                    saveProfile(user.getUid(), email, phone);

                    // ------------------------------------------------------------
                    // USER FEEDBACK + RETURN TO LOGIN
                    // ------------------------------------------------------------
                    Toast.makeText(this,
                            "Account created. Verification email sent.",
                            Toast.LENGTH_LONG).show();

                    auth.signOut();
                    finish();
                });
    }

    private void saveProfile(String uid, String email, String phone) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);
        profile.put("phone", phone);
        profile.put("mfa_enrolled", false);
        profile.put("last_mfa", null);

        firestore.collection("profiles")
                .document(uid)
                .set(profile)
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Warning: Profile save failed, but account was created.",
                                Toast.LENGTH_LONG).show()
                );
    }
}
