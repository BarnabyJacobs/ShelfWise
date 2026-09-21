package com.example.householdfoodinventory.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Session.SessionManager;
import com.example.householdfoodinventory.Utils.UrgencyColourMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * StorageAreaActivity
 *
 * Displays all food items for a specific storage area (Fridge, Freezer, Cupboard).
 *
 * Responsibilities:
 *  - Validate user session.
 *  - Validate the storage area passed via Intent.
 *  - Retrieve items for the selected area from IFoodDao.
 *  - Sort items by expiry urgency using ExpiryService.
 *  - Display items using FoodItemAdapter (RecyclerView).
 *  - Provide a button to add new items to this area.
 *
 * Architectural Role:
 *  - UI-only class: contains no business logic and no SQL.
 *  - Delegates expiry logic to ExpiryService.
 *  - Delegates persistence to IFoodDao.
 *  - Delegates editing to EditItemActivity.
 *
 * SOLID:
 *  - SRP: Only handles UI and navigation.
 *  - DIP: Depends on abstractions (IFoodDao, ExpiryService, SessionManager).
 */
public class StorageAreaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;

    // Injected dependencies
    private IFoodDao foodDao;
    private ExpiryService expiryService;
    private SessionManager session;

    // Storage area (fridge, freezer, cupboard)
    private String area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_area);

        // ------------------------------------------------------------
        // Dependency Injection via AppContainer
        // ------------------------------------------------------------
        AppContainer app = ((MyApplication) getApplication()).container;
        foodDao = app.foodDao;
        expiryService = app.expiryService;
        session = app.session;

        // ------------------------------------------------------------
        // SESSION VALIDATION
        // ------------------------------------------------------------
        if (!session.hasValidSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ------------------------------------------------------------
        // AREA VALIDATION
        // ------------------------------------------------------------
        area = getIntent().getStringExtra("area");

        if (area == null || area.trim().isEmpty()) {
            Toast.makeText(this, "Invalid storage area", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Display area title
        TextView areaTitle = findViewById(R.id.areaTitle);
        areaTitle.setText(area);

        // ------------------------------------------------------------
        // RECYCLER VIEW SETUP
        // ------------------------------------------------------------
        recyclerView = findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load items immediately
        loadItems();

        // ------------------------------------------------------------
        // ADD ITEM BUTTON
        // ------------------------------------------------------------
        findViewById(R.id.addItemButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            intent.putExtra("area", area);
            startActivity(intent);
        });
    }

    /**
     * Reload items whenever returning to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    /**
     * Retrieves items for this storage area, sorts them by urgency,
     * and attaches the adapter to the RecyclerView.
     */
    private void loadItems() {

        // Retrieve Firebase UID
        String userUid = session.getUid();

        if (userUid == null || userUid.trim().isEmpty()) {
            Toast.makeText(this, "Invalid session", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Retrieve items for this user + area
        ArrayList<FoodItem> items = foodDao.getFoodItems(userUid, area);

        if (items == null) {
            items = new ArrayList<>();
        }

        // Sort by urgency
        Collections.sort(items, Comparator.comparing(expiryService::getUrgency));

        // Adapter setup
        adapter = new FoodItemAdapter(
                this,
                items,
                expiryService,
                new UrgencyColourMapper(),
                item -> {
                    Intent intent = new Intent(this, EditItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);
                },
                item -> {
                    Intent intent = new Intent(this, DeleteItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);
                }
        );


        recyclerView.setAdapter(adapter);
    }

}
