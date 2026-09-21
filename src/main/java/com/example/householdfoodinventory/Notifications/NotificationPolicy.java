package com.example.householdfoodinventory.Notifications;

import androidx.annotation.Nullable;
import com.example.householdfoodinventory.Model.FoodItem;

/**
 * NotificationPolicy
 *
 * Responsibilities:
 *  - Define business rules for expiry notifications.
 *
 * Architectural Role:
 *  - SRP: Pure decision logic.
 *  - DIP: Used by ExpiryService; contains no Android or database logic.
 *  - Fully testable.
 */
public class NotificationPolicy {

    public boolean shouldSendAmber(@Nullable FoodItem item, long days) {

        if (item == null) return false;
        if (days < 0) return false;

        return days >= 1 && days <= 7 && !item.isAmberNotified();
    }

    public boolean shouldSendRed(@Nullable FoodItem item, long days) {

        if (item == null) return false;
        if (days < 0) return false;

        return days == 0 && !item.isRedNotified();
    }
}
