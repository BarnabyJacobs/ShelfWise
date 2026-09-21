package com.example.householdfoodinventory.UI;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;

/**
 * DeleteItemActivity
 *
 * Responsibilities:
 *  - Load selected item.
 *  - Display summary.
 *  - Allow user to choose deletion reason.
 *  - Delegate deletion to IFoodDao.
 *
 * Architectural Role:
 *  - SRP: UI-only component.
 *  - DIP: Depends on IFoodDao abstraction.
 */
public class DeleteItemActivity extends AppCompatActivity {

    private IFoodDao foodDao;

    private int itemId;

    private TextView summary;
    private Spinner reasonSpinner;
    private Button deleteBtn, cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_item);

        AppContainer app = ((MyApplication) getApplication()).container;
        foodDao = app.foodDao;

        itemId = getIntent().getIntExtra("itemId", -1);
        if (itemId == -1) {
            Toast.makeText(this, "Error: No item selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        FoodItem item = foodDao.getFoodItemById(itemId);
        if (item == null) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        summary = findViewById(R.id.deleteItemSummary);
        reasonSpinner = findViewById(R.id.deleteReasonSpinner);
        deleteBtn = findViewById(R.id.deleteButton);
        cancelBtn = findViewById(R.id.cancelButton);

        summary.setText(
                item.getName() + "\n" +
                        item.getExpiryDate() + "\n" +
                        "£" + String.format("%.2f", item.getPrice()) +
                        " • Qty: " + item.getQuantity() +
                        " • " + item.getType()
        );

        ArrayAdapter<String> deleteAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"No reason", "Consumed", "Thrown away / Expired"}
        );
        reasonSpinner.setAdapter(deleteAdapter);

        cancelBtn.setOnClickListener(v -> finish());

        deleteBtn.setOnClickListener(v -> {
            String reason = reasonSpinner.getSelectedItem().toString();
            deleteItem(itemId, reason);
        });
    }

    private void deleteItem(int id, String reason) {
        foodDao.deleteFoodItem(id);
        Toast.makeText(this, "Deleted (" + reason + ")", Toast.LENGTH_SHORT).show();
        finish();
    }
}
