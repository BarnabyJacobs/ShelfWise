package com.example.householdfoodinventory.Service;

import androidx.annotation.NonNull;

import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.Notifications.NotificationPolicy;
import com.example.householdfoodinventory.Notifications.NotificationSender;
import com.example.householdfoodinventory.Utils.ExpiryCalculator;

/**
 * ExpiryService
 *
 * Responsibilities:
 *  - Calculate expiry urgency.
 *  - Apply notification rules.
 *  - Send notifications.
 *  - Persist notification flags.
 *
 * Architectural Role:
 *  - SRP: Pure business logic.
 *  - DIP: Depends on abstractions.
 */
public class ExpiryService {

    private final ExpiryCalculator calculator;
    private final NotificationPolicy policy;
    private final NotificationSender sender;
    private final IFoodDao foodDao;

    public ExpiryService(ExpiryCalculator calculator,
                         NotificationPolicy policy,
                         NotificationSender sender,
                         IFoodDao foodDao) {
        this.calculator = calculator;
        this.policy = policy;
        this.sender = sender;
        this.foodDao = foodDao;
    }

    public Urgency getUrgency(@NonNull FoodItem item) {

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.trim().isEmpty()) {
            return Urgency.SAFE;
        }

        Long days = safeDaysToExpiry(expiry);
        if (days == null) return Urgency.SAFE;

        if (days < 0) return Urgency.EXPIRED;
        if (days == 0) return Urgency.WITHIN_24_HOURS;
        if (days <= 7) return Urgency.WITHIN_7_DAYS;
        return Urgency.SAFE;
    }

    public enum Urgency {
        EXPIRED,
        WITHIN_24_HOURS,
        WITHIN_7_DAYS,
        SAFE
    }

    public void evaluateImmediateNotification(@NonNull FoodItem item) {

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.trim().isEmpty()) return;

        Long days = safeDaysToExpiry(expiry);
        if (days == null) return;

        boolean changed = false;

        if (days != 0 && item.isRedNotified()) {
            item.setRedNotified(false);
            changed = true;
        }

        if ((days < 1 || days > 7) && item.isAmberNotified()) {
            item.setAmberNotified(false);
            changed = true;
        }

        if (policy.shouldSendRed(item, days)) {
            sender.send("Expires today",
                    sender.safeName(item.getName()) + " expires today!");
            item.setRedNotified(true);
            changed = true;
        }

        if (policy.shouldSendAmber(item, days)) {
            sender.send("Expiring soon",
                    sender.safeName(item.getName()) + " expires in " + days + " days");
            item.setAmberNotified(true);
            changed = true;
        }

        if (changed) {
            foodDao.updateFoodItem(item);
        }
    }

    private Long safeDaysToExpiry(String expiry) {
        try {
            return calculator.daysToExpiry(expiry);
        } catch (Exception e) {
            return null;
        }
    }
}
