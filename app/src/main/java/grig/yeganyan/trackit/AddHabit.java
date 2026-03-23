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
        tealBtn = findViewById(R.id.Teal);
        deepPurpleBtn = findViewById(R.id.DeepPurple);
        amberBtn = findViewById(R.id.Amber);
        lightBlueBtn = findViewById(R.id.LightBlue);
        purpleDarkBtn = findViewById(R.id.PurpleDark);
        grayBtn = findViewById(R.id.Gray);
        blueGrayBtn = findViewById(R.id.BlueGray);
        lightGreenBtn = findViewById(R.id.LightGreen);
        orangeDarkBtn = findViewById(R.id.OrangeDark);
        redDarkBtn = findViewById(R.id.RedDark);
        indigoDarkBtn = findViewById(R.id.IndigoDark);
        brownDarkBtn = findViewById(R.id.BrownDark);
        peachBtn = findViewById(R.id.Peach);
        mintBtn = findViewById(R.id.Mint);
        lavenderBtn = findViewById(R.id.Lavender);
        coralBtn = findViewById(R.id.Coral);
        skyBlueBtn = findViewById(R.id.SkyBlue);
        lemonBtn = findViewById(R.id.Lemon);

        allColorButtons = new MaterialButton[]{
                purpleBtn, greenBtn, redBtn, orangeBtn, blueBtn, yellowBtn,
                pinkBtn, cyanBtn, limeBtn, deepOrangeBtn, indigoBtn, brownBtn,
                tealBtn, deepPurpleBtn, amberBtn, lightBlueBtn, purpleDarkBtn, grayBtn,
                blueGrayBtn, lightGreenBtn, orangeDarkBtn, redDarkBtn, indigoDarkBtn, brownDarkBtn,
                peachBtn, mintBtn, lavenderBtn, coralBtn, skyBlueBtn, lemonBtn
        };

        String[] colors = {
                "#6200EE","#32A852","#F51111","#EB8F34","#2962FF","#FFD600",
                "#FF4081","#00BCD4","#CDDC39","#FF5722","#3F51B5","#795548",
                "#009688","#673AB7","#FFC107","#03A9F4","#512DA8","#9E9E9E",
                "#607D8B","#8BC34A","#FF6F00","#B71C1C","#1A237E","#4E342E",
                "#FFAB91","#A7FFEB","#B39DDB","#FF6E6E","#81D4FA","#FFF59D"
        };
        Random random = new Random();
        colour = colors[random.nextInt(colors.length)];
        for (int i = 0; i < allColorButtons.length; i++) {
            int index = i;
            allColorButtons[i].setOnClickListener(v -> selectColor(colors[index], allColorButtons[index]));
        }

        db = FirebaseFirestore.getInstance();

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        receiveHabitTemplate();

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
        }
        if (type.isEmpty()) {
            titleInput.setError("This field is required");
            titleInput.requestFocus();
            return; // stop saving until filled
        }
        if(unit.equals("Select")){
            Toast.makeText(this, "Unit is required", Toast.LENGTH_SHORT).show();
            unitSpinner.requestFocus();
            return;
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
}