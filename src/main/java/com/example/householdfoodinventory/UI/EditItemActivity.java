package com.example.householdfoodinventory.UI;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.householdfoodinventory.AppContainer;
import com.example.householdfoodinventory.MyApplication;
import com.example.householdfoodinventory.Data.IFoodDao;
import com.example.householdfoodinventory.Model.FoodItem;
import com.example.householdfoodinventory.R;
import com.example.householdfoodinventory.Service.ExpiryService;
import com.example.householdfoodinventory.Validation.FormValidator;
import com.example.householdfoodinventory.Validation.InputParser;
import com.example.householdfoodinventory.Validation.ParseResult;

import java.util.Calendar;

/**
 * EditItemActivity
 *
 * Responsibilities:
 *  - Load existing item.
 *  - Display fields for editing.
 *  - Validate and parse input.
 *  - Delegate persistence and expiry logic.
 *  - Allow editing of remaining percentage.
 */
public class EditItemActivity extends AppCompatActivity {

    private IFoodDao foodDao;
    private int itemId;

    private EditText name, expiry, price, qty, notes;
    private Spinner type;
    private SeekBar remainingSeekBar;
    private TextView remainingValue;

    private Button saveBtn, cancelBtn;

    private FormValidator formValidator;
    private InputParser inputParser;
    private ExpiryService expiryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        AppContainer app = ((MyApplication) getApplication()).container;
        foodDao = app.foodDao;
        formValidator = app.formValidator;
        inputParser = app.inputParser;
        expiryService = app.expiryService;

        TextView title = findViewById(R.id.screenTitle);
        title.setText("Edit Item");

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

        name   = findViewById(R.id.inputItemName);
        expiry = findViewById(R.id.inputExpiryDate);
        price  = findViewById(R.id.inputItemPrice);
        qty    = findViewById(R.id.inputQuantity);
        notes  = findViewById(R.id.inputNotes);
        type   = findViewById(R.id.inputUseByType);
        remainingSeekBar = findViewById(R.id.remainingSeekBar);
        remainingValue = findViewById(R.id.remainingValue);
        saveBtn = findViewById(R.id.saveItemButton);
        cancelBtn = findViewById(R.id.cancelItemButton);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Use by", "Best before"}
        );
        type.setAdapter(typeAdapter);
        type.setSelection("Best before".equals(item.getType()) ? 1 : 0);

        expiry.setOnClickListener(v -> showDatePicker());

        // --- Pre-fill fields ---
        name.setText(item.getName());
        expiry.setText(item.getExpiryDate());
        price.setText(String.valueOf(item.getPrice()));
        qty.setText(String.valueOf(item.getQuantity()));
        notes.setText(item.getNotes());

        // --- Initialise slider ---
        remainingSeekBar.setMax(100);
        int currentPercent = item.getRemainingPercent(); // new field in FoodItem
        remainingSeekBar.setProgress(currentPercent);
        remainingValue.setText(currentPercent + "%");

        remainingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                remainingValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saveBtn.setOnClickListener(v -> onSave(item));
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String formatted = String.format("%02d/%02d/%04d", day, month + 1, year);
                    expiry.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void applyUpdates(FoodItem item,
                              String name, String expiry, String type,
                              int qty, double price, String notes, int remainingPercent) {

        item.setName(name);
        item.setExpiryDate(expiry);
        item.setType(type);
        item.setQuantity(qty);
        item.setPrice(price);
        item.setNotes(notes);
        item.setRemainingPercent(remainingPercent);
    }

    private void onSave(FoodItem item) {

        String nameText = name.getText().toString().trim();
        String expiryText = expiry.getText().toString().trim();
        String priceText = price.getText().toString().trim();
        String qtyText = qty.getText().toString().trim();
        String notesText = notes.getText().toString().trim();
        String typeText = type.getSelectedItem().toString();
        int remainingPercent = remainingSeekBar.getProgress();

        if (formValidator.isEmpty(nameText)) {
            name.setError("Name required");
            return;
        }

        if (!formValidator.validateExpiry(expiryText)) {
            expiry.setError("Invalid date (dd/MM/yyyy)");
            return;
        }

        if (formValidator.isEmpty(priceText)) {
            price.setError("Price required");
            return;
        }

        if (formValidator.isEmpty(qtyText)) {
            qty.setError("Quantity required");
            return;
        }

        ParseResult<Double> priceResult = inputParser.parsePrice(priceText);
        if (!priceResult.isSuccess()) {
            price.setError(priceResult.getError());
            return;
        }

        ParseResult<Integer> qtyResult = inputParser.parseQuantity(qtyText);
        if (!qtyResult.isSuccess()) {
            qty.setError(qtyResult.getError());
            return;
        }

        double priceValue = priceResult.getValue();
        int qtyValue = qtyResult.getValue();

        applyUpdates(item, nameText, expiryText, typeText, qtyValue, priceValue, notesText, remainingPercent);

        foodDao.updateFoodItem(item);
        expiryService.evaluateImmediateNotification(item);

        Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
