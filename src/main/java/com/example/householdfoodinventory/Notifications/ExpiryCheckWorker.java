package com.example.householdfoodinventory.Notifications;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.Utils.ExpiryCalculator;

import java.util.List;

/**
 * ExpiryCheckWorker runs in the background (via WorkManager) to evaluate
 * expiry dates for all food items belonging to the logged‑in user.
 *
 * Responsibilities:
 * - Retrieve all food items for the current user.
 * - Calculate days until expiry using ExpiryCalculator.
 * - Apply notification rules using NotificationPolicy.
 * - Send notifications using NotificationSender.
 * - Update notification flags (amber/red) in the database.
 *
 * This class contains no UI logic and no SQL logic.
 * It orchestrates expiry evaluation and notification behaviour.
 */
public class ExpiryCheckWorker extends Worker {

    private final IFoodDao foodDao;
    private final ExpiryCalculator calculator;
    private final NotificationPolicy policy;
    private final NotificationSender sender;

    /**
     * Constructor.
     * Retrieves shared dependencies from AppContainer.
     *
     * @param context Application context.
     * @param params  Worker parameters.
     */
    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        AppContainer app = ((MyApplication) context.getApplicationContext()).container;

        this.foodDao = app.foodDao;
        this.calculator = app.expiryCalculator;
        this.policy = new NotificationPolicy();
        this.sender = new NotificationSender(context);
    }

    /**
     * Main worker method executed by WorkManager.
     *
     * Steps:
     * 1. Retrieve current user ID.
     * 2. Load all food items for that user.
     * 3. For each item:
     *    - Calculate days to expiry.
     *    - Check amber notification rules.
     *    - Check red notification rules.
     *    - Reset flags if item leaves a notification state.
     * 4. Persist updated notification flags.
     *
     * @return Result.success() when processing completes.
     */
    @NonNull
    @Override
    public Result doWork() {

        Log.d("ExpiryCheckWorker", "Worker is running");

        // Retrieve Firebase UID from SessionManager
        String userUid = ((MyApplication) getApplicationContext())
                .container.session.getUid();

        if (userUid == null || userUid.trim().isEmpty()) {
            Log.d("ExpiryCheckWorker", "No logged-in user");
            return Result.success();
        }

        // Load all food items for this Firebase user
        List<FoodItem> items = foodDao.getAllFoodItems(userUid);

        if (items == null || items.isEmpty()) {
            Log.d("ExpiryCheckWorker", "No items found");
            return Result.success();
        }

        for (FoodItem item : items) {

            if (item == null) continue;

            long days = calculator.daysToExpiry(item.getExpiryDate());

            Log.d("ExpiryCheckWorker",
                    "Checking item: " + item.getName() + " | days=" + days);

            // ----------------------------------------------------
            // AMBER (1–7 days) — once per day
            // ----------------------------------------------------
            if (policy.shouldSendAmber(item, days)) {
                Log.d("ExpiryCheckWorker", "Sending AMBER for " + item.getName());
                sender.send(
                        "Expiring soon",
                        item.getName() + " expires in " + days + " days"
                );
                item.setAmberNotified(true);
                foodDao.updateFoodItem(item);
            }

            // Reset amber flag if item leaves amber range
            if (days < 1 || days > 7) {
                if (item.isAmberNotified()) {
                    Log.d("ExpiryCheckWorker", "Resetting AMBER flag for " + item.getName());
                    item.setAmberNotified(false);
                    foodDao.updateFoodItem(item);
                }
            }

            // ----------------------------------------------------
            // RED (0 days) — once total
            // ----------------------------------------------------
            if (policy.shouldSendRed(item, days)) {
                Log.d("ExpiryCheckWorker", "Sending RED for " + item.getName());
                sender.send(
                        "Expires today",
                        item.getName() + " expires today!"
                );
                item.setRedNotified(true);
                foodDao.updateFoodItem(item);
            }

            // Reset red flag if item is no longer red
            if (days != 0 && item.isRedNotified()) {
                Log.d("ExpiryCheckWorker", "Resetting RED flag for " + item.getName());
                item.setRedNotified(false);
                foodDao.updateFoodItem(item);
            }
        }

        return Result.success();
    }

}
