package com.example.householdfoodinventory;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.example.householdfoodinventory.Data.FoodDao;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Notifications.NotificationPolicy;
import com.example.householdfoodinventory.Notifications.NotificationSender;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Session.SessionManager;
import com.example.householdfoodinventory.Utils.ExpiryCalculator;
import com.example.householdfoodinventory.Validation.FormValidator;
import com.example.householdfoodinventory.Validation.InputParser;

/**
 * AppContainer
 *
 * Responsibilities:
 *  - Construct and expose shared dependencies.
 *  - Act as the application's composition root.
 *
 * Architectural Role:
 *  - SRP: Pure dependency wiring.
 *  - DIP: Activities depend on abstractions, not concrete implementations.
 */
public class AppContainer {

    // DATA LAYER
    public final IFoodDao foodDao;

    // AUTH
    public final FirebaseAuth auth;

    // SESSION
    public final SessionManager session;

    // VALIDATION
    public final FormValidator formValidator;
    public final InputParser inputParser;

    // EXPIRY + NOTIFICATIONS
    public final ExpiryCalculator expiryCalculator;
    public final NotificationPolicy notificationPolicy;
    public final NotificationSender notificationSender;
    public final ExpiryService expiryService;

    public AppContainer(Context context) {

        auth = FirebaseAuth.getInstance();
        Context appContext = context.getApplicationContext();

        foodDao = new FoodDao(appContext);
        session = new SessionManager(appContext);

        formValidator = new FormValidator();
        inputParser = new InputParser();

        expiryCalculator = new ExpiryCalculator();
        notificationPolicy = new NotificationPolicy();
        notificationSender = new NotificationSender(appContext);

        expiryService = new ExpiryService(
                expiryCalculator,
                notificationPolicy,
                notificationSender,
                foodDao
        );
    }

    /**
     * Optional factory for Activity‑scoped ExpiryService instances.
     */
    public ExpiryService newExpiryService(Context context) {
        return new ExpiryService(
                expiryCalculator,
                notificationPolicy,
                new NotificationSender(context.getApplicationContext()),
                foodDao
        );
    }
}
