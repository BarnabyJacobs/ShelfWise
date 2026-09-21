package com.example.householdfoodinventory.UI;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Session.SessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton, registerButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialise Firebase and App Check before any auth calls
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        auth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginButton.setEnabled(true);

                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Login failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "unknown error"),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Unexpected error: user is null.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    handleSuccessfulLogin();
                });
    }

    private void handleSuccessfulLogin() {
        Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();

        // ✅ Create session for MainActivity to recognise
        SessionManager sessionManager = new SessionManager(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            sessionManager.createLoginSession(user.getUid(), user.getEmail());
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
