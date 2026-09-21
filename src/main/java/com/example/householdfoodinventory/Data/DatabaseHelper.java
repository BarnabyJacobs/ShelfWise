package com.example.householdfoodinventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper
 *
 * Responsibilities:
 *  - Create and configure the SQLite database.
 *  - Delegate table creation to DatabaseTables.
 *  - Manage schema upgrades and downgrades.
 *
 * Architectural Role:
 *  - SRP: Pure database lifecycle management.
 *  - DIP: Used by FoodDao; contains no business logic.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "food_inventory.db";
    private static final int DB_VERSION = 9; // Increment when schema changes

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            DatabaseTables.createTables(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // 🔹 Safe migration: add new column if upgrading from older versions
            if (oldVersion < 9) {
                db.execSQL("ALTER TABLE " + DatabaseTables.FoodTable.NAME +
                        " ADD COLUMN " + DatabaseTables.FoodTable.COL_REMAINING_PERCENT +
                        " INTEGER NOT NULL DEFAULT 100;");
            }

            // Future migrations can be chained here:
            // if (oldVersion < 10) { ... }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: recreate tables if migration fails
            DatabaseTables.dropTables(db);
            DatabaseTables.createTables(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Mirrors upgrade behaviour for consistency
        onUpgrade(db, oldVersion, newVersion);
    }
}
