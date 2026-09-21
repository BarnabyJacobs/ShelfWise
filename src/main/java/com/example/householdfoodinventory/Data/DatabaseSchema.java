package com.example.householdfoodinventory.Data;

/**
 * DatabaseSchema defines the structure of the application's SQLite database.
 *
 * Purpose:
 * - Centralise all table and column names in one place.
 * - Prevent typos and duplication across DAOs and DatabaseHelper.
 * - Ensure schema consistency throughout the data layer.
 *
 * This class contains no SQL logic, no database operations, and no business logic.
 * It is purely a constants container and should remain immutable.
 */
public final class DatabaseSchema {

    /**
     * Private constructor prevents instantiation.
     * This class is intended to be used statically.
     */
    private DatabaseSchema() { }

    /**
     * UserTable defines the schema for the USER_TABLE.
     * Contains column names used for user authentication and identification.
     */
    public static final class UserTable {

        // Name of the table storing user accounts.
        public static final String NAME = "USER_TABLE";

        // Primary key for the user.
        public static final String COL_ID = "USER_ID";

        // Username chosen by the user.
        public static final String COL_USERNAME = "USERNAME";

        // Password stored for authentication (currently plain text; should be hashed).
        public static final String COL_PASSWORD = "PASSWORD";
    }

    /**
     * FoodTable defines the schema for the FOOD_TABLE.
     * Stores all food items associated with a user.
     */
    public static final class FoodTable {

        // Name of the table storing food items.
        public static final String NAME = "FOOD_TABLE";

        // Primary key for each food item.
        public static final String COL_ID = "FOOD_ID";

        // Foreign key linking the food item to a specific user.
        public static final String COL_USER_ID = "USER_ID";

        // Name of the food item.
        public static final String COL_NAME = "NAME";

        // Expiry date stored as a string (ISO recommended).
        public static final String COL_EXPIRY_DATE = "EXPIRY_DATE";

        // Whether the item uses "Use by" or "Best before".
        public static final String COL_TYPE = "TYPE";

        // Optional price field.
        public static final String COL_PRICE = "PRICE";

        // Quantity of the item.
        public static final String COL_QUANTITY = "QUANTITY";

        // Optional notes field.
        public static final String COL_NOTES = "NOTES";

        // Storage location: Fridge, Freezer, or Cupboard.
        public static final String COL_LOCATION = "LOCATION";

        // Whether the amber notification has been sent.
        public static final String COL_AMBER_NOTIFIED = "AMBER_NOTIFIED";

        // Whether the red notification has been sent.
        public static final String COL_RED_NOTIFIED = "RED_NOTIFIED";
    }
}
