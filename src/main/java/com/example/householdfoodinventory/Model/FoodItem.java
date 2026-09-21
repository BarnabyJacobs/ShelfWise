package com.example.householdfoodinventory.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * FoodItem
 *
 * Responsibilities:
 *  - Represent a single food entry.
 *  - Provide null‑safe accessors and sanitisation.
 *
 * Architectural Role:
 *  - SRP: Pure domain model.
 *  - Contains no business logic, expiry logic, or database logic.
 *  - Used by DAO, UI, and services.
 */
public class FoodItem {

    private int id;
    private String userUid;

    @NonNull
    private String name;

    @NonNull
    private String expiryDate;

    @NonNull
    private String type;

    private int quantity;
    private double price;

    @Nullable
    private String notes;

    @NonNull
    private String location;

    private boolean amberNotified;
    private boolean redNotified;

    // 🔹 New field: percentage of item remaining
    private int remainingPercent;

    public FoodItem() {
        this.id = 0;
        this.userUid = "";
        this.name = "";
        this.expiryDate = "";
        this.type = "";
        this.quantity = 0;
        this.price = 0.0;
        this.notes = "";
        this.location = "";
        this.amberNotified = false;
        this.redNotified = false;
        this.remainingPercent = 100; // default full
    }

    public FoodItem(int id,
                    @NonNull String userUid,
                    @NonNull String name,
                    @NonNull String expiryDate,
                    @NonNull String type,
                    int quantity,
                    double price,
                    @Nullable String notes,
                    @NonNull String location,
                    boolean amberNotified,
                    boolean redNotified,
                    int remainingPercent) {

        this.id = id;
        this.userUid = safe(userUid);
        this.name = safe(name);
        this.expiryDate = safe(expiryDate);
        this.type = safe(type);
        this.quantity = quantity;
        this.price = price;
        this.notes = notes == null ? "" : notes;
        this.location = safe(location);
        this.amberNotified = amberNotified;
        this.redNotified = redNotified;
        this.remainingPercent = clamp(remainingPercent);
    }

    public FoodItem(@NonNull String userUid,
                    @NonNull String name,
                    @NonNull String expiryDate,
                    @NonNull String type,
                    int quantity,
                    double price,
                    @Nullable String notes,
                    @NonNull String location) {

        this.id = 0;
        this.userUid = safe(userUid);
        this.name = safe(name);
        this.expiryDate = safe(expiryDate);
        this.type = safe(type);
        this.quantity = quantity;
        this.price = price;
        this.notes = notes == null ? "" : notes;
        this.location = safe(location);
        this.amberNotified = false;
        this.redNotified = false;
        this.remainingPercent = 100; // default full
    }

    private @NonNull String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isValidLocation(String loc) {
        return "fridge".equals(loc) || "freezer".equals(loc) || "cupboard".equals(loc);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // Getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getUserUid() { return userUid; }
    public void setUserUid(@NonNull String userUid) { this.userUid = safe(userUid); }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = safe(name); }

    @NonNull
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(@NonNull String expiryDate) { this.expiryDate = safe(expiryDate); }

    @NonNull
    public String getType() { return type; }
    public void setType(@NonNull String type) { this.type = safe(type); }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Nullable
    public String getNotes() { return notes; }
    public void setNotes(@Nullable String notes) { this.notes = notes == null ? "" : notes; }

    @NonNull
    public String getLocation() { return location; }
    public void setLocation(@NonNull String location) {
        String loc = safe(location);
        if (isValidLocation(loc)) this.location = loc;
    }

    public boolean isAmberNotified() { return amberNotified; }
    public void setAmberNotified(boolean amberNotified) { this.amberNotified = amberNotified; }

    public boolean isRedNotified() { return redNotified; }
    public void setRedNotified(boolean redNotified) { this.redNotified = redNotified; }

    // 🔹 Remaining percentage accessors
    public int getRemainingPercent() { return remainingPercent; }
    public void setRemainingPercent(int remainingPercent) {
        this.remainingPercent = clamp(remainingPercent);
    }
}
