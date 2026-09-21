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
import com.example.householdfoodinventory.Session.SessionManager;
import com.example.householdfoodinventory.Validation.FormValidator;
import com.example.householdfoodinventory.Validation.InputParser;
import com.example.householdfoodinventory.Validation.ParseResult;

import java.util.Calendar;

/**
 * AddItemActivity
 *
 * Responsibilities:
 *  - Collect user input.
 *  - Validate and parse fields.
 *  - Construct a FoodItem.
 *  - Delegate persistence and expiry logic.
 *  - Initialise remaining percentage slider.
 */
public class AddItemActivity extends AppCompatActivity {

    private EditText name, expiry, price, qty, notes;
    private Spinner type;
    private SeekBar remainingSeekBar;
    private TextView remainingValue;

    private IFoodDao foodDao;
    private SessionManager session;
    private FormValidator formValidator;
    private InputParser inputParser;
    private ExpiryService expiryService;

    private String area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        AppContainer app = ((MyApplication) getApplication()).container;
        foodDao = app.foodDao;
        session = app.session;
        formValidator = app.formValidator;
        inputParser = app.inputParser;
        expiryService = app.expiryService;

        area = getIntent().getStringExtra("area");
        if (area == null) {
            Toast.makeText(this, "ERROR: No storage area passed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        name = findViewById(R.id.inputItemName);
        expiry = findViewById(R.id.inputExpiryDate);
        price = findViewById(R.id.inputItemPrice);
        qty = findViewById(R.id.inputQuantity);
        notes = findViewById(R.id.inputNotes);
        type = findViewById(R.id.inputUseByType);

        // --- New slider setup ---
        remainingSeekBar = findViewById(R.id.remainingSeekBar);
        remainingValue = findViewById(R.id.remainingValue);

        remainingSeekBar.setMax(100);
        remainingSeekBar.setProgress(100);
        remainingValue.setText("100%");

        remainingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                remainingValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Use by", "Best before"}
        );
        type.setAdapter(typeAdapter);

        Button saveButton = findViewById(R.id.saveItemButton);
        Button cancelButton = findViewById(R.id.cancelItemButton);

        expiry.setOnClickListener(v -> showDatePicker());
        cancelButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    expiry.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private FoodItem buildItem(String userUid, String name, String expiry, String type,
                               int qty, double price, String notes, String area, int remainingPercent) {

        return new FoodItem(
                0,
                userUid,
                name,
                expiry,
                type,
                qty,
                price,
                notes,
                area,
                false,
                false,
                remainingPercent // new field
        );
    }

    private void saveItem() {
        try {
            String nameText = name.getText().toString().trim();
            String expiryText = expiry.getText().toString().trim();
            String priceText = price.getText().toString().trim();
            String qtyText = qty.getText().toString().trim();
            String notesText = notes.getText().toString().trim();
            String typeText = type.getSelectedItem().toString();

            if (formValidator.isEmpty(nameText)) {
                name.setError("Name required");
                return;
            }

            if (formValidator.isEmpty(expiryText)) {
                expiry.setError("Expiry date required");
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
            int remainingPercent = remainingSeekBar.getProgress();

            String userUid = session.getUid();

            FoodItem item = buildItem(
                    userUid,
                    nameText,
                    expiryText,
                    typeText,
                    qtyValue,
                    priceValue,
                    notesText,
                    area,
                    remainingPercent
            );

            long id = foodDao.insertFoodItem(item);
            item.setId((int) id);

            expiryService.evaluateImmediateNotification(item);

            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
