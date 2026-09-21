package com.example.householdfoodinventory.Data;

import android.database.sqlite.SQLiteDatabase;

/**
 * DatabaseTables
 *
 * Responsibilities:
 *  - Define SQL schema for all tables.
 *  - Provide creation and drop statements.
 *
 * Architectural Role:
 *  - SRP: Pure schema definition.
 *  - Used by DatabaseHelper; contains no business logic.
 */
public final class DatabaseTables {

    private DatabaseTables() {}

    // -------------------------------------------------------------------------
    // FOOD TABLE (Firebase‑ready)
    // -------------------------------------------------------------------------

    public static final class FoodTable {

        public static final String NAME = "FOOD_TABLE";

        public static final String COL_ID = "FOOD_ID";
        public static final String COL_USER_UID = "USER_UID";
        public static final String COL_NAME = "NAME";
        public static final String COL_EXPIRY_DATE = "EXPIRY_DATE";
        public static final String COL_TYPE = "TYPE";
        public static final String COL_PRICE = "PRICE";
        public static final String COL_QUANTITY = "QUANTITY";
        public static final String COL_NOTES = "NOTES";
        public static final String COL_LOCATION = "LOCATION";
        public static final String COL_AMBER_NOTIFIED = "AMBER_NOTIFIED";
        public static final String COL_RED_NOTIFIED = "RED_NOTIFIED";

        // 🔹 New column for remaining percentage
        public static final String COL_REMAINING_PERCENT = "REMAINING_PERCENT";

        private static final String CREATE =
                "CREATE TABLE IF NOT EXISTS " + NAME + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USER_UID + " TEXT NOT NULL, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_EXPIRY_DATE + " TEXT NOT NULL, " +
                        COL_TYPE + " TEXT NOT NULL, " +
                        COL_PRICE + " REAL NOT NULL, " +
                        COL_QUANTITY + " INTEGER NOT NULL, " +
                        COL_NOTES + " TEXT, " +
                        COL_LOCATION + " TEXT NOT NULL CHECK (" + COL_LOCATION + " IN ('fridge','freezer','cupboard')), " +
                        COL_AMBER_NOTIFIED + " INTEGER NOT NULL DEFAULT 0 CHECK (" + COL_AMBER_NOTIFIED + " IN (0,1)), " +
                        COL_RED_NOTIFIED + " INTEGER NOT NULL DEFAULT 0 CHECK (" + COL_RED_NOTIFIED + " IN (0,1)), " +
                        // 🔹 New column definition
                        COL_REMAINING_PERCENT + " INTEGER NOT NULL DEFAULT 100 CHECK (" + COL_REMAINING_PERCENT + " BETWEEN 0 AND 100)" +
                        ");";

        private static final String CREATE_INDEX_USER =
                "CREATE INDEX IF NOT EXISTS idx_food_user ON " +
                        NAME + " (" + COL_USER_UID + ");";

        private static final String CREATE_INDEX_EXPIRY =
                "CREATE INDEX IF NOT EXISTS idx_food_expiry ON " +
                        NAME + " (" + COL_EXPIRY_DATE + ");";
    }

    // -------------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------------

    public static void createTables(SQLiteDatabase db) {
        try {
            db.execSQL(FoodTable.CREATE);
            db.execSQL(FoodTable.CREATE_INDEX_USER);
            db.execSQL(FoodTable.CREATE_INDEX_EXPIRY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dropTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + FoodTable.NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
