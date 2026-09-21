package com.example.householdfoodinventory;

import android.app.Application;
import android.util.Log;

import com.google.firebase.appcheck.BuildConfig;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;


import java.util.concurrent.TimeUnit;

/**
 * MyApplication
 *
 * Custom Application class that serves as the entry point for the entire app.
 *
 * Responsibilities:
 *  - Construct the AppContainer (dependency injection root).
 *  - Schedule the ExpiryCheckWorker using WorkManager.
 *  - Ensure background expiry checks run reliably even when the app is closed.
 *
 * Architectural Role:
 *  - Acts as the composition root for dependency injection.
 *  - Ensures all Activities and Fragments receive shared instances of DAOs,
 *    services, validators, and utilities via AppContainer.
 *  - Owns WorkManager scheduling so it is not duplicated by Activities.
 *
 * Notes:
 *  - WorkManager is scheduled once using enqueueUniquePeriodicWork().
 *  - ExistingPeriodicWorkPolicy.KEEP prevents duplicate workers.
 *  - Using application context avoids memory leaks.
 */
public class MyApplication extends Application {

    public AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        if (BuildConfig.DEBUG) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
            );
        } else {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
            );
        }

        container = new AppContainer(this);

        Log.d("MyApplication", "Scheduling ExpiryCheckWorker");

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        com.example.householdfoodinventory.Notifications.ExpiryCheckWorker.class,
                        12, TimeUnit.HOURS
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

}
