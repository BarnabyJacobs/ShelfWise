package com.example.householdfoodinventory.UI;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.widget.RemoteViews;

import com.example.householdfoodinventory.R;

/**
 * ShelfWiseWidget
 *
 * Home‑screen widget for the ShelfWise app.
 *
 * Responsibilities:
 *  - Display a static widget layout (logo).
 *  - Respond to widget lifecycle events (added, updated, etc.).
 *  - Attach a tap action that opens MainActivity.
 *
 * Architectural Role:
 *  - UI-only component for the Android launcher.
 *  - Contains no business logic and no database access.
 *  - Acts as a lightweight entry point into the app.
 *
 * Notes:
 *  - Uses RemoteViews because widgets run in the launcher process.
 *  - Must use PendingIntent for click actions.
 */
public class ShelfWiseWidget extends AppWidgetProvider {

    /**
     * onEnabled()
     *
     * Called when the FIRST instance of the widget is added to the home screen.
     * Ideal for one‑time setup, analytics, or initial configuration.
     *
     * Here:
     *  - Loads the widget layout.
     *  - Updates all widget instances.
     */
    @Override
    public void onEnabled(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        // Identify all instances of this widget
        ComponentName widget = new ComponentName(context, ShelfWiseWidget.class);

        // Inflate the widget layout
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                R.layout.widget_shelfwiselogo
        );

        // Update all widget instances
        manager.updateAppWidget(widget, views);
    }

    /**
     * onUpdate()
     *
     * Called whenever the widget needs to be refreshed:
     *  - When first added
     *  - When resized
     *  - When the update interval triggers (if configured)
     *  - When manually refreshed by the user
     *
     * For each widget instance:
     *  1. Inflate the widget layout.
     *  2. Create a PendingIntent that opens MainActivity.
     *  3. Attach the PendingIntent to the widget logo.
     *  4. Push the updated RemoteViews to the launcher.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int id : appWidgetIds) {

            // Inflate widget layout
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_shelfwiselogo
            );

            // Intent to open the app when the widget is tapped
            Intent intent = new Intent(context, MainActivity.class);

            // PendingIntent required for widget click actions
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            // Attach click action to the widget logo
            views.setOnClickPendingIntent(R.id.widgetLogo, pendingIntent);

            // Push update to this widget instance
            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
