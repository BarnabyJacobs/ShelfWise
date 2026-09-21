package com.example.householdfoodinventory.Utils;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;

/**
 * UrgencyColourMapper
 *
 * Maps an ExpiryService.Urgency value to a UI colour resource.
 *
 * Responsibilities:
 *  - Convert urgency levels (EXPIRED, WITHIN_24_HOURS, etc.)
 *    into the correct colour for display.
 *
 * Architectural Role:
 *  - Pure UI utility class.
 *  - Contains no business logic, no date logic, and no database access.
 *  - Keeps colour decisions out of adapters and Activities.
 *
 * SOLID:
 *  - SRP: Only handles colour mapping.
 *  - DIP: Depends on the ExpiryService.Urgency enum, not on any concrete logic.
 *
 * Notes:
 *  - Colours are defined in res/values/colors.xml.
 *  - ContextCompat ensures compatibility across API levels.
 */
public class UrgencyColourMapper {

    /**
     * Returns the colour associated with a given urgency level.
     *
     * @param context Android context used to resolve colour resources.
     * @param urgency The urgency enum returned by ExpiryService.
     * @return An integer colour value.
     */
    public int getColour(Context context, ExpiryService.Urgency urgency) {

        switch (urgency) {

            case EXPIRED:
                return ContextCompat.getColor(context, R.color.black);

            case WITHIN_24_HOURS:
                return ContextCompat.getColor(context, R.color.red);

            case WITHIN_7_DAYS:
                return ContextCompat.getColor(context, R.color.orange);

            case SAFE:
                return ContextCompat.getColor(context, R.color.green);

            default:
                // Fallback to SAFE colour
                return ContextCompat.getColor(context, R.color.green);
        }
    }
}
