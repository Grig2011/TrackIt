package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import grig.yeganyan.trackit.model.Habit;

public class AddHabit extends AppCompatActivity {

    TextInputEditText emojiInput, titleInput, descInput, goalInput;
    Spinner typeSpinner, unitSpinner, daysSpinner;
    Button saveHabitBtn;
    MaterialButton purpleBtn, greenBtn, redBtn, orangeBtn, blueBtn, yellowBtn,
            pinkBtn, cyanBtn, limeBtn, deepOrangeBtn, indigoBtn, brownBtn;
    String colour = "#32A852"; // default green
    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inputs
        emojiInput = findViewById(R.id.emojiInput);
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        goalInput = findViewById(R.id.goalInput);

        // Emoji-only input
        emojiInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtered = s.toString().replaceAll("[^\\p{So}\\p{Cn}]", "");
                if (!s.toString().equals(filtered)) {
                    emojiInput.setText(filtered);
                    emojiInput.setSelection(filtered.length());
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });
        emojiInput.requestFocus();
        emojiInput.post(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(emojiInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Spinners
        typeSpinner = findViewById(R.id.typeSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);
        daysSpinner = findViewById(R.id.daysSpinner);

        // Buttons
        saveHabitBtn = findViewById(R.id.saveHabitBtn);

        purpleBtn = findViewById(R.id.Purple);
        greenBtn = findViewById(R.id.Green);
        redBtn = findViewById(R.id.Red);
        orangeBtn = findViewById(R.id.Orange);
        blueBtn = findViewById(R.id.Blue);
        yellowBtn = findViewById(R.id.Yellow);
        pinkBtn = findViewById(R.id.Pink);
        cyanBtn = findViewById(R.id.Cyan);
        limeBtn = findViewById(R.id.Lime);
        deepOrangeBtn = findViewById(R.id.DeepOrange);
        indigoBtn = findViewById(R.id.Indigo);
        brownBtn = findViewById(R.id.Brown);

        // Back button
        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(AddHabit.this, MainActivity.class));
            finish();
        });

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Color button click listeners
        purpleBtn.setOnClickListener(v -> selectColor("#6200EE", purpleBtn));
        greenBtn.setOnClickListener(v -> selectColor("#32A852", greenBtn));
        redBtn.setOnClickListener(v -> selectColor("#F51111", redBtn));
        orangeBtn.setOnClickListener(v -> selectColor("#EB8F34", orangeBtn));
        blueBtn.setOnClickListener(v -> selectColor("#2962FF", blueBtn));
        yellowBtn.setOnClickListener(v -> selectColor("#FFD600", yellowBtn));
        pinkBtn.setOnClickListener(v -> selectColor("#FF4081", pinkBtn));
        cyanBtn.setOnClickListener(v -> selectColor("#00BCD4", cyanBtn));
        limeBtn.setOnClickListener(v -> selectColor("#CDDC39", limeBtn));
        deepOrangeBtn.setOnClickListener(v -> selectColor("#FF5722", deepOrangeBtn));
        indigoBtn.setOnClickListener(v -> selectColor("#3F51B5", indigoBtn));
        brownBtn.setOnClickListener(v -> selectColor("#795548", brownBtn));

        // Save habit
        saveHabitBtn.setOnClickListener(v -> saveHabit());
    }

    private void selectColor(String selectedColor, MaterialButton clickedBtn) {
        colour = selectedColor;

        // Clear previous checkmarks
        purpleBtn.setText("");
        greenBtn.setText("");
        redBtn.setText("");
        orangeBtn.setText("");
        blueBtn.setText("");
        yellowBtn.setText("");
        pinkBtn.setText("");
        cyanBtn.setText("");
        limeBtn.setText("");
        deepOrangeBtn.setText("");
        indigoBtn.setText("");
        brownBtn.setText("");

        // Mark selected button
        clickedBtn.setText("✔");
    }

    private void saveHabit() {
        String emoji = emojiInput.getText().toString().trim();
        String title = titleInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String unit = unitSpinner.getSelectedItem().toString();
        String days = daysSpinner.getSelectedItem().toString();
        int streak = 0;

        if (title.isEmpty()) {
            titleInput.setError("Title required");
            return;
        }

        double goal = 0;
        if (!goalInput.getText().toString().isEmpty()) {
            try {
                goal = Double.parseDouble(goalInput.getText().toString());
            } catch (NumberFormatException e) {
                goalInput.setError("Invalid number");
                return;
            }
        }

        String habitId = UUID.randomUUID().toString();
        Habit habit = new Habit(habitId, emoji, title, desc, colour, type, goal, unit, days, streak);

        db.collection("users")
                .document(userId)
                .collection("habits")
                .document(habitId)
                .set(habit)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}