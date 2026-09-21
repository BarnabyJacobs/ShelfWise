package com.example.householdfoodinventory.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Utils.UrgencyColourMapper;

import java.util.List;

/**
 * AllItemsAdapter
 *
 * Custom ArrayAdapter responsible for displaying FoodItem objects inside
 * the ListView used by AllItemsActivity.
 *
 * Responsibilities:
 *  - Inflate the row layout for each FoodItem.
 *  - Bind FoodItem data (name, expiry, area, quantity) to UI elements.
 *  - Determine expiry urgency using ExpiryService.
 *  - Apply colour coding using UrgencyColourMapper.
 *
 * Architectural Role:
 *  - UI-only component: contains no business logic and no database logic.
 *  - Delegates expiry logic to ExpiryService.
 *  - Delegates colour mapping to UrgencyColourMapper.
 *
 * This class follows SOLID principles:
 *  - SRP: Only handles view rendering.
 *  - DIP: Depends on abstractions (ExpiryService), not concrete logic.
 */
public class AllItemsAdapter extends ArrayAdapter<FoodItem> {

    // Service used to determine expiry urgency (EXPIRED, RED, AMBER, SAFE)
    private final ExpiryService expiryService;

    // Maps urgency levels to colour resources
    private final UrgencyColourMapper colourMapper;

    // Context used for inflating layouts and resolving colours
    private final Context context;

    /**
     * Constructor
     *
     * @param context        Activity context
     * @param items          List of FoodItem objects to display
     * @param expiryService  Injected service for urgency calculation
     */
    public AllItemsAdapter(Context context, List<FoodItem> items, ExpiryService expiryService) {
        super(context, 0, items);
        this.expiryService = expiryService;
        this.colourMapper = new UrgencyColourMapper();
        this.context = context;
    }

    /**
     * getView()
     *
     * Called by the ListView to render each row.
     * Steps:
     *  1. Inflate row layout if needed.
     *  2. Retrieve the FoodItem for this position.
     *  3. Bind name, expiry, area, and quantity to TextViews.
     *  4. Determine urgency using ExpiryService.
     *  5. Apply colour coding using UrgencyColourMapper.
     *
     * @param position     Row index
     * @param convertView  Recycled view (if available)
     * @param parent       Parent ViewGroup
     * @return             Fully populated row view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate row layout if no recycled view is available
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_all_food, parent, false);
        }

        // Retrieve the FoodItem for this row
        FoodItem item = getItem(position);

        // Bind UI elements
        TextView name = convertView.findViewById(R.id.allItemName);
        TextView details = convertView.findViewById(R.id.allItemDetails);

        // Set item name
        name.setText(item.getName());

        // Set details line (expiry date, storage area, quantity)
        details.setText(
                "Expiry: " + item.getExpiryDate() +
                        " | Area: " + item.getLocation() +
                        " | Qty: " + item.getQuantity()
        );

        // ------------------------------------------------------------
        // Apply urgency colour
        //
        // ExpiryService determines urgency:
        //   EXPIRED, WITHIN_24_HOURS, WITHIN_7_DAYS, SAFE
        //
        // UrgencyColourMapper maps urgency → colour resource.
        // ------------------------------------------------------------
        ExpiryService.Urgency urgency = expiryService.getUrgency(item);
        int color = colourMapper.getColour(context, urgency);
        name.setTextColor(color);

        return convertView;
    }
}
