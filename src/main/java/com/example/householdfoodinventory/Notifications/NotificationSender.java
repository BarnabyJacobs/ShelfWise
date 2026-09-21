package com.example.householdfoodinventory.Notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.householdfoodinventory.R;

/**
 * NotificationSender
 *
 * Responsibilities:
 *  - Build and dispatch expiry notifications.
 *  - Create notification channel (Android 8+).
 *
 * Architectural Role:
 *  - SRP: Pure delivery mechanism.
 *  - DIP: Used by ExpiryService; contains no business logic.
 */
public class NotificationSender {

    private static final String CHANNEL_ID = "expiry_channel";

    private final Context context;

    public NotificationSender(@NonNull Context context) {
        this.context = context.getApplicationContext();
        createChannel();
    }

    public void send(@NonNull String title, @NonNull String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = buildNotification(title, message);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        try {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NotificationCompat.Builder buildNotification(String title, String message) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("ShelfWise")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
    }

    public @NonNull String safeName(String name) {
        return (name == null || name.trim().isEmpty()) ? "This item" : name.trim();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager == null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for food expiry warnings");

            try {
                manager.createNotificationChannel(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
