package com.example.householdfoodinventory.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.householdfoodinventory.Notifications.ExpiryCheckWorker;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Session.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TabLayout storageTabs;
    private ViewPager2 viewPager;
    private StoragePagerAdapter pagerAdapter;

    // MFA expiry constant (kept for future use)
    private static final long MFA_EXPIRY_MS = 90L * 24L * 60L * 60L * 1000L; // 90 days

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // ------------------------------------------------------------
        // SESSION + FIREBASE VALIDATION
        // ------------------------------------------------------------
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || !sessionManager.hasValidSession()) {
            safeLogout();
            return;
        }

        // Force reload to detect deleted accounts
        user.reload().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                safeLogout();
                return;
            }

            // Temporarily skip MFA validation while MFA is disabled
            // checkMfaValidity(user);
            initUi();
        });
    }

    // ------------------------------------------------------------
    // MFA VALIDATION (COMMENTED OUT FOR NOW)
    // ------------------------------------------------------------
    /*
    private void checkMfaValidity(FirebaseUser user) {
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        safeLogout();
                        return;
                    }

                    var snapshot = task.getResult();

                    Boolean mfaEnrolled = snapshot.getBoolean("mfa_enrolled");
                    var lastMfaTimestamp = snapshot.getTimestamp("last_mfa");

                    if (mfaEnrolled == null || !mfaEnrolled) {
                        // Should never happen because LoginActivity enforces MFA
                        safeLogout();
                        return;
                    }

                    long now = System.currentTimeMillis();

                    if (lastMfaTimestamp == null ||
                            (now - lastMfaTimestamp.toDate().getTime()) > MFA_EXPIRY_MS) {

                        // MFA expired → force re-login
                        Toast.makeText(this,
                                "For security, please re-authenticate.",
                                Toast.LENGTH_LONG).show();

                        safeLogout();
                        return;
                    }

                    // MFA valid → continue loading UI
                    initUi();
                });
    }
    */

    // ------------------------------------------------------------
    // MAIN UI INITIALISATION
    // ------------------------------------------------------------
    private void initUi() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        scheduleExpiryWorker();
        requestNotificationPermission();

        setupInsets();
        setupHamburgerMenu();
        setupTabsAndPager();
        setupAddButton();
    }

    private void scheduleExpiryWorker() {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(ExpiryCheckWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupTabsAndPager() {
        storageTabs = findViewById(R.id.storageTabs);
        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new StoragePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(storageTabs, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Fridge"); break;
                        case 1: tab.setText("Freezer"); break;
                        case 2: tab.setText("Cupboard"); break;
                    }
                }).attach();
    }

    private void setupHamburgerMenu() {
        Toolbar toolbar = findViewById(R.id.mainToolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.menu_account) {
                        startActivity(new Intent(this, SettingsActivity.class));
                        return true;
                    }

                    if (item.getItemId() == R.id.menu_sign_out) {
                        safeLogout();
                        return true;
                    }

                    return false;
                });

                popup.show();
            });
        }
    }

    private void setupAddButton() {
        FloatingActionButton addButton = findViewById(R.id.addItemButton);

        addButton.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            String area = pagerAdapter.getAreaForPosition(position);

            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            intent.putExtra("area", area);
            startActivity(intent);
        });
    }

    private void safeLogout() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {}

        sessionManager.logoutUser();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NOTIFY", "Notification permission granted");
            } else {
                Toast.makeText(this, "Notifications disabled by user", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
