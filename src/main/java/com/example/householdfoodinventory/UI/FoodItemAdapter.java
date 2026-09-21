package com.example.householdfoodinventory.UI;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Utils.UrgencyColourMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * FoodItemAdapter
 *
 * Responsibilities:
 *  - Inflate row layout for each FoodItem.
 *  - Bind FoodItem data to UI.
 *  - Delegate expiry urgency calculation to ExpiryService.
 *  - Delegate colour mapping to UrgencyColourMapper.
 *  - Display remaining percentage visually via ProgressBar.
 *  - Expose click events via listener interfaces.
 *
 * Architectural Role:
 *  - SRP: UI-only component.
 *  - DIP: Depends on abstractions (ExpiryService, UrgencyColourMapper).
 */
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FoodItem item);
    }

    public interface OnDeleteClickListener {
        void onDelete(FoodItem item);
    }

    private final Context context;
    private final List<FoodItem> items = new ArrayList<>();
    private final ExpiryService expiryService;
    private final UrgencyColourMapper colorMapper;
    private final OnItemClickListener itemClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public FoodItemAdapter(Context context,
                           List<FoodItem> items,
                           ExpiryService expiryService,
                           UrgencyColourMapper colorMapper,
                           OnItemClickListener itemClickListener,
                           OnDeleteClickListener deleteClickListener) {

        this.context = context;
        this.expiryService = expiryService;
        this.colorMapper = colorMapper;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;

        if (items != null) {
            for (FoodItem item : items) {
                if (item != null) this.items.add(item);
            }
        }
    }

    public void updateItems(List<FoodItem> newItems) {
        items.clear();
        if (newItems != null) {
            for (FoodItem item : newItems) {
                if (item != null) items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FoodItem item = items.get(position);
        if (item == null) return;

        holder.name.setText(item.getName() == null ? "Unnamed item" : item.getName());
        holder.expiry.setText(item.getExpiryDate() == null ? "No date" : item.getExpiryDate());

        String details = "£" + String.format("%.2f", item.getPrice())
                + " • Qty: " + item.getQuantity()
                + " • " + item.getType();
        holder.details.setText(details);

        // --- Expiry urgency colour ---
        ExpiryService.Urgency urgency;
        try {
            urgency = expiryService.getUrgency(item);
        } catch (Exception e) {
            urgency = ExpiryService.Urgency.SAFE;
        }
        holder.name.setTextColor(colorMapper.getColour(context, urgency));

        // --- Remaining percentage display ---
        int remaining = item.getRemainingPercent();
        holder.remainingProgress.setProgress(remaining);
        holder.remainingLabel.setText(remaining + "%");

        // Colour feedback based on thresholds
        int tintColor;
        if (remaining > 60) {
            tintColor = ContextCompat.getColor(context, R.color.green_light);
        } else if (remaining > 30) {
            tintColor = ContextCompat.getColor(context, R.color.orange);
        } else {
            tintColor = ContextCompat.getColor(context, R.color.red);
        }
        holder.remainingProgress.setProgressTintList(ColorStateList.valueOf(tintColor));

        // --- Click listeners ---
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(item);
        });

        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, expiry, details, remainingLabel;
        ProgressBar remainingProgress;
        ImageView deleteIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            expiry = itemView.findViewById(R.id.itemExpiry);
            details = itemView.findViewById(R.id.itemDetails);
            remainingLabel = itemView.findViewById(R.id.itemRemainingLabel);
            remainingProgress = itemView.findViewById(R.id.itemRemainingProgress);
            deleteIcon = itemView.findViewById(R.id.itemDeleteIcon);
        }
    }
}
