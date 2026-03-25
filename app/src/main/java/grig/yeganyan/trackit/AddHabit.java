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

import java.util.Random;
import java.util.UUID;

import grig.yeganyan.trackit.model.Habit;

public class AddHabit extends AppCompatActivity {

    TextInputEditText emojiInput, titleInput, descInput, goalInput;
    Spinner typeSpinner, unitSpinner, daysSpinner;
    Button saveHabitBtn;
    MaterialButton purpleBtn, greenBtn, redBtn, orangeBtn, blueBtn, yellowBtn,
            pinkBtn, cyanBtn, limeBtn, deepOrangeBtn, indigoBtn, brownBtn,
            tealBtn, deepPurpleBtn, amberBtn, lightBlueBtn, purpleDarkBtn, grayBtn,
            blueGrayBtn, lightGreenBtn, orangeDarkBtn, redDarkBtn, indigoDarkBtn, brownDarkBtn,
            peachBtn, mintBtn, lavenderBtn, coralBtn, skyBlueBtn, lemonBtn;
    MaterialButton[] allColorButtons;
    String colour;
    FirebaseFirestore db;
    String userId;

    String mode = "ADD";
    String habitId = null;

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

        // --- INIT UI ---
        emojiInput = findViewById(R.id.emojiInput);
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        goalInput = findViewById(R.id.goalInput);
        typeSpinner = findViewById(R.id.typeSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);
        daysSpinner = findViewById(R.id.daysSpinner);
        saveHabitBtn = findViewById(R.id.saveHabitBtn);

        // --- COLOR BUTTONS ---
        MaterialButton[] buttons = new MaterialButton[]{
                purpleBtn = findViewById(R.id.Purple),
                greenBtn = findViewById(R.id.Green),
                redBtn = findViewById(R.id.Red),
                orangeBtn = findViewById(R.id.Orange),
                blueBtn = findViewById(R.id.Blue),
                yellowBtn = findViewById(R.id.Yellow),
                pinkBtn = findViewById(R.id.Pink),
                cyanBtn = findViewById(R.id.Cyan),
                limeBtn = findViewById(R.id.Lime),
                deepOrangeBtn = findViewById(R.id.DeepOrange),
                indigoBtn = findViewById(R.id.Indigo),
                brownBtn = findViewById(R.id.Brown),
                tealBtn = findViewById(R.id.Teal),
                deepPurpleBtn = findViewById(R.id.DeepPurple),
                amberBtn = findViewById(R.id.Amber),
                lightBlueBtn = findViewById(R.id.LightBlue),
                purpleDarkBtn = findViewById(R.id.PurpleDark),
                grayBtn = findViewById(R.id.Gray),
                blueGrayBtn = findViewById(R.id.BlueGray),
                lightGreenBtn = findViewById(R.id.LightGreen),
                orangeDarkBtn = findViewById(R.id.OrangeDark),
                redDarkBtn = findViewById(R.id.RedDark),
                indigoDarkBtn = findViewById(R.id.IndigoDark),
                brownDarkBtn = findViewById(R.id.BrownDark),
                peachBtn = findViewById(R.id.Peach),
                mintBtn = findViewById(R.id.Mint),
                lavenderBtn = findViewById(R.id.Lavender),
                coralBtn = findViewById(R.id.Coral),
                skyBlueBtn = findViewById(R.id.SkyBlue),
                lemonBtn = findViewById(R.id.Lemon)
        };
        allColorButtons = buttons;

        String[] colors = {
                "#6200EE","#32A852","#F51111","#EB8F34","#2962FF","#FFD600",
                "#FF4081","#00BCD4","#CDDC39","#FF5722","#3F51B5","#795548",
                "#009688","#673AB7","#FFC107","#03A9F4","#512DA8","#9E9E9E",
                "#607D8B","#8BC34A","#FF6F00","#B71C1C","#1A237E","#4E342E",
                "#FFAB91","#A7FFEB","#B39DDB","#FF6E6E","#81D4FA","#FFF59D"
        };

        for (int i = 0; i < allColorButtons.length; i++) {
            int index = i;
            allColorButtons[i].setOnClickListener(v -> selectColor(colors[index], allColorButtons[index]));
        }

        db = FirebaseFirestore.getInstance();

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        // --- HANDLE INTENT ---
        Intent intent = getIntent();
        if (intent != null) {
            mode = intent.getStringExtra("MODE");
            habitId = intent.getStringExtra("habitId");

            if ("EDIT".equals(mode) && habitId != null) {
                // --- SET FIELDS FROM INTENT ---
                emojiInput.setText(intent.getStringExtra("emoji"));
                titleInput.setText(intent.getStringExtra("title"));
                descInput.setText(intent.getStringExtra("description"));
                goalInput.setText(intent.getStringExtra("goal"));

                colour = intent.getStringExtra("color");
                String type = intent.getStringExtra("type");
                String unit = intent.getStringExtra("unit");
                String days = intent.getStringExtra("days");

                // --- SPINNER SETUP ---
                typeSpinner.post(() -> setSpinnerSelection(typeSpinner, type));
                unitSpinner.post(() -> setSpinnerSelection(unitSpinner, unit));
                daysSpinner.post(() -> setSpinnerSelection(daysSpinner, days));

                // --- COLOR BUTTON ---
                if (colour != null) {
                    for (int i = 0; i < colors.length; i++) {
                        if (colors[i].equalsIgnoreCase(colour)) {
                            selectColor(colors[i], allColorButtons[i]);
                            break;
                        }
                    }
                }

                saveHabitBtn.setText("Update Habit");
            } else {
                // ADD mode: populate from popular habit template
                receiveHabitTemplate();
                // Pick random color in ADD mode
                Random random = new Random();
                colour = colors[random.nextInt(colors.length)];
            }
        }

        saveHabitBtn.setOnClickListener(v -> saveHabit());
    }

    private void selectColor(String selectedColor, MaterialButton clickedBtn) {
        colour = selectedColor;
        for (MaterialButton btn : allColorButtons) {
            btn.setText("");
        }
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
        }else{

            goalInput.setError("goal is required");
            return;
        }

        if (unit.equals("Select")) {
            Toast.makeText(this, "Unit is required", Toast.LENGTH_SHORT).show();
            return;
        }


        Habit habit;
        if ("EDIT".equals(mode) && habitId != null) {
            habit = new Habit(habitId, emoji, title, desc, colour, type, goal, unit, days, streak);
            db.collection("users")
                    .document(userId)
                    .collection("habits")
                    .document(habitId)
                    .set(habit)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Habit updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            String newHabitId = UUID.randomUUID().toString();
            habit = new Habit(newHabitId, emoji, title, desc, colour, type, goal, unit, days, streak);
            db.collection("users")
                    .document(userId)
                    .collection("habits")
                    .document(newHabitId)
                    .set(habit)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void receiveHabitTemplate() {
        Intent intent = getIntent();
        if(intent == null) return;
        String emoji = intent.getStringExtra("emoji");
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");

        if(emoji != null) emojiInput.setText(emoji);
        if(title != null) titleInput.setText(title);
        if(desc != null) descInput.setText(desc);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;

        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}