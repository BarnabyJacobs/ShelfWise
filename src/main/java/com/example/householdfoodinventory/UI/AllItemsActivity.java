package com.example.householdfoodinventory.UI;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Session.SessionManager;

import java.util.ArrayList;

/**
 * AllItemsActivity
 *
 * UI-only Activity responsible for displaying ALL food items belonging
 * to the currently logged-in user, regardless of storage area.
 */
public class AllItemsActivity extends AppCompatActivity {

    private IFoodDao foodDao;
    private ExpiryService expiryService;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_items);

        ListView listView = findViewById(R.id.allItemsListView);

        // Dependency Injection
        AppContainer app = ((MyApplication) getApplication()).container;
        foodDao = app.foodDao;
        expiryService = app.expiryService;
        session = app.session;

        // ------------------------------------------------------------
        // Retrieve all items for the current Firebase user
        // ------------------------------------------------------------
        String userUid = session.getUid();   // Firebase UID

        ArrayList<FoodItem> allItems = foodDao.getAllFoodItems(userUid);

        // ------------------------------------------------------------
        // Sort items by urgency
        // ------------------------------------------------------------
        allItems.sort((a, b) ->
                expiryService.getUrgency(a).ordinal()
                        - expiryService.getUrgency(b).ordinal()
        );

        // ------------------------------------------------------------
        // Attach adapter to ListView
        // ------------------------------------------------------------
        AllItemsAdapter adapter = new AllItemsAdapter(this, allItems, expiryService);
        listView.setAdapter(adapter);
    }
}
