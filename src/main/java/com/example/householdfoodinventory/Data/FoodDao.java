package com.example.householdfoodinventory.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.householdfoodinventory.Model.FoodItem;

import java.util.ArrayList;

import static com.example.householdfoodinventory.Data.DatabaseTables.FoodTable;

/**
 * FoodDao
 *
 * Responsibilities:
 *  - Perform CRUD operations for FoodItem.
 *  - Filter all queries by Firebase UID.
 *
 * Architectural Role:
 *  - SRP: Pure persistence layer.
 *  - DIP: Implements IFoodDao abstraction.
 *  - Contains no business logic, expiry logic, or UI logic.
 */
public class FoodDao implements IFoodDao {

    private final DatabaseHelper dbHelper;

    public FoodDao(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    @Override
    public long insertFoodItem(FoodItem item) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put(FoodTable.COL_USER_UID, item.getUserUid());
            values.put(FoodTable.COL_NAME, item.getName());
            values.put(FoodTable.COL_EXPIRY_DATE, item.getExpiryDate());
            values.put(FoodTable.COL_TYPE, item.getType());
            values.put(FoodTable.COL_PRICE, item.getPrice());
            values.put(FoodTable.COL_QUANTITY, item.getQuantity());
            values.put(FoodTable.COL_NOTES, item.getNotes());
            values.put(FoodTable.COL_LOCATION, item.getLocation());
            values.put(FoodTable.COL_AMBER_NOTIFIED, item.isAmberNotified() ? 1 : 0);
            values.put(FoodTable.COL_RED_NOTIFIED, item.isRedNotified() ? 1 : 0);

            // 🔹 New field
            values.put(FoodTable.COL_REMAINING_PERCENT, item.getRemainingPercent());

            return db.insert(FoodTable.NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int updateFoodItem(FoodItem item) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put(FoodTable.COL_NAME, item.getName());
            values.put(FoodTable.COL_EXPIRY_DATE, item.getExpiryDate());
            values.put(FoodTable.COL_TYPE, item.getType());
            values.put(FoodTable.COL_PRICE, item.getPrice());
            values.put(FoodTable.COL_QUANTITY, item.getQuantity());
            values.put(FoodTable.COL_NOTES, item.getNotes());
            values.put(FoodTable.COL_LOCATION, item.getLocation());
            values.put(FoodTable.COL_AMBER_NOTIFIED, item.isAmberNotified() ? 1 : 0);
            values.put(FoodTable.COL_RED_NOTIFIED, item.isRedNotified() ? 1 : 0);

            // 🔹 New field
            values.put(FoodTable.COL_REMAINING_PERCENT, item.getRemainingPercent());

            return db.update(
                    FoodTable.NAME,
                    values,
                    FoodTable.COL_ID + "=?",
                    new String[]{String.valueOf(item.getId())}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateNotificationFlags(int itemId, boolean redNotified, boolean amberNotified) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put(FoodTable.COL_RED_NOTIFIED, redNotified ? 1 : 0);
            values.put(FoodTable.COL_AMBER_NOTIFIED, amberNotified ? 1 : 0);

            return db.update(
                    FoodTable.NAME,
                    values,
                    FoodTable.COL_ID + "=?",
                    new String[]{String.valueOf(itemId)}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteFoodItem(int id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {

            return db.delete(
                    FoodTable.NAME,
                    FoodTable.COL_ID + "=?",
                    new String[]{String.valueOf(id)}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public ArrayList<FoodItem> getFoodItems(String userUid, @Nullable String area) {
        ArrayList<FoodItem> items = new ArrayList<>();

        if (area == null || area.trim().isEmpty()) return items;

        if (!area.equals("fridge") && !area.equals("freezer") && !area.equals("cupboard")) {
            return items;
        }

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     FoodTable.NAME,
                     null,
                     FoodTable.COL_USER_UID + "=? AND " + FoodTable.COL_LOCATION + "=?",
                     new String[]{userUid, area.trim()},
                     null, null, null
             )) {

            while (cursor.moveToNext()) {
                FoodItem item = mapCursorToFoodItem(cursor);
                if (item != null) items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    @Override
    public ArrayList<FoodItem> getAllFoodItems(String userUid) {
        ArrayList<FoodItem> items = new ArrayList<>();

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     FoodTable.NAME,
                     null,
                     FoodTable.COL_USER_UID + "=?",
                     new String[]{userUid},
                     null, null, null
             )) {

            while (cursor.moveToNext()) {
                FoodItem item = mapCursorToFoodItem(cursor);
                if (item != null) items.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    @Override
    public FoodItem getFoodItemById(int id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     FoodTable.NAME,
                     null,
                     FoodTable.COL_ID + "=?",
                     new String[]{String.valueOf(id)},
                     null, null, null
             )) {

            if (cursor.moveToFirst()) {
                return mapCursorToFoodItem(cursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private FoodItem mapCursorToFoodItem(Cursor cursor) {
        try {
            int remaining = 100;
            int idx = cursor.getColumnIndex(FoodTable.COL_REMAINING_PERCENT);
            if (idx != -1) {
                remaining = cursor.getInt(idx);
            }

            return new FoodItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(FoodTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_USER_UID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_EXPIRY_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_TYPE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(FoodTable.COL_QUANTITY)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(FoodTable.COL_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_NOTES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COL_LOCATION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(FoodTable.COL_AMBER_NOTIFIED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(FoodTable.COL_RED_NOTIFIED)) == 1,
                    remaining
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
