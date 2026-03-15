package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import grig.yeganyan.trackit.model.Habit;

public class AddHabit extends AppCompatActivity {

    TextInputEditText emojiInput, titleInput, descInput, goalInput;
    Spinner typeSpinner, unitSpinner, daysSpinner;
    Button saveHabitBtn, greenBtn, purpleBtn, redBtn;
    String colour = "#32a852"; // default green
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

        emojiInput = findViewById(R.id.emojiInput);
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        goalInput = findViewById(R.id.goalInput);

        typeSpinner = findViewById(R.id.typeSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);
        daysSpinner = findViewById(R.id.daysSpinner);
        saveHabitBtn = findViewById(R.id.saveHabitBtn);

        greenBtn = findViewById(R.id.Green);
        purpleBtn = findViewById(R.id.Purple);
        redBtn = findViewById(R.id.Red);

        purpleBtn.setOnClickListener(v -> selectColor("#6200EE", purpleBtn));
        redBtn.setOnClickListener(v -> selectColor("#f51111", redBtn));
        greenBtn.setOnClickListener(v -> selectColor("#32a852", greenBtn));

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(AddHabit.this, MainActivity.class));
            finish();
        });

        db = FirebaseFirestore.getInstance();
        saveHabitBtn.setOnClickListener(v -> saveHabit());
    }

    private void selectColor(String selectedColor, Button clickedBtn) {
        colour = selectedColor;
        greenBtn.setText("");
        purpleBtn.setText("");
        redBtn.setText("");
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