package com.example.householdfoodinventory.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Session.SessionManager;
import com.example.householdfoodinventory.Utils.UrgencyColourMapper;

import java.util.ArrayList;

/**
 * StorageFragment
 *
 * Responsibilities:
 *  - Display items for a single storage area (fridge/freezer/cupboard).
 *  - Load items via IFoodDao.
 *  - Apply expiry urgency via ExpiryService.
 *  - Navigate to EditItemActivity when an item is tapped.
 *
 * Architectural Role:
 *  - SRP: UI-only component.
 *  - DIP: Depends on abstractions (IFoodDao, ExpiryService, SessionManager).
 *  - All business logic delegated to services and DAO.
 */
public class StorageFragment extends Fragment {

    private static final String ARG_AREA = "area";

    private String area;
    private IFoodDao foodDao;
    private SessionManager session;
    private ExpiryService expiryService;

    private RecyclerView recyclerView;

    public static StorageFragment newInstance(String area) {
        StorageFragment fragment = new StorageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AREA, area);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppContainer app = ((MyApplication) requireActivity().getApplication()).container;

        foodDao = app.foodDao;
        session = app.session;
        expiryService = app.expiryService; // Fully DI now

        if (getArguments() != null) {
            area = getArguments().getString(ARG_AREA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_storage, container, false);

        recyclerView = view.findViewById(R.id.storageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        divider.setDrawable(requireContext().getDrawable(R.drawable.item_divider));
        recyclerView.addItemDecoration(divider);

        loadItems();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {

        String userUid = session.getUid();
        if (userUid.isEmpty()) return; // Session enforcement belongs in MainActivity

        ArrayList<FoodItem> items = foodDao.getFoodItems(userUid, area);
        if (items == null) items = new ArrayList<>();

        FoodItemAdapter adapter = new FoodItemAdapter(
                requireContext(),
                items,
                expiryService,
                new UrgencyColourMapper(),
                item -> {
                    Intent intent = new Intent(requireContext(), EditItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);
                },
                item -> {
                    Intent intent = new Intent(requireContext(), DeleteItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);
                }
        );


        recyclerView.setAdapter(adapter);
    }
}
