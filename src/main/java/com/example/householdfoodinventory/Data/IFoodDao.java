package com.example.householdfoodinventory.Data;

import com.example.householdfoodinventory.Model.FoodItem;
import java.util.ArrayList;

/**
 * IFoodDao
 *
 * Responsibilities:
 *  - Define persistence operations for FoodItem.
 *  - Provide a clean abstraction for database access.
 *
 * Architectural Role:
 *  - SRP: Only declares data-access methods.
 *  - DIP: Used by ExpiryService, StorageFragment, Workers, Activities.
 *  - Contains no SQL, no business logic, no UI logic.
 */
public interface IFoodDao {

    long insertFoodItem(FoodItem item);

    int updateFoodItem(FoodItem item);

    int deleteFoodItem(int id);

    ArrayList<FoodItem> getFoodItems(String userUid, String area);

    ArrayList<FoodItem> getAllFoodItems(String userUid);

    FoodItem getFoodItemById(int id);

    /**
     * Updates only the notification flags for a FoodItem.
     * Supports ExpiryService without overwriting unrelated fields.
     */
    int updateNotificationFlags(int itemId, boolean redNotified, boolean amberNotified);
}
