package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import grig.yeganyan.trackit.model.Habit;

public class AddHabit extends AppCompatActivity {

    TextInputEditText emojiInput, titleInput, descInput, goalInput;
    Spinner unitSpinner;
    Button saveHabitBtn;

    // Time UI
    TextView tvSelectedTime;
    MaterialButton btnPickTime;
    Calendar calendar = Calendar.getInstance();
    String timeString = "Not set";
    boolean isTimeSelected = false;

    MaterialButton purpleBtn, greenBtn, redBtn, orangeBtn, blueBtn, yellowBtn,
            pinkBtn, cyanBtn, limeBtn, deepOrangeBtn, indigoBtn, brownBtn,
            tealBtn, deepPurpleBtn, amberBtn, lightBlueBtn, purpleDarkBtn, grayBtn,
            blueGrayBtn, lightGreenBtn, orangeDarkBtn, redDarkBtn, indigoDarkBtn, brownDarkBtn,
            peachBtn, mintBtn, lavenderBtn, coralBtn, skyBlueBtn, lemonBtn;
    MaterialButton[] allColorButtons;
    String colour;
    FirebaseFirestore db;
    String userId;

    // Day buttons
    MaterialButton monBtn, tueBtn, wedBtn, thuBtn, friBtn, satBtn, sunBtn;
    MaterialButton[] dayButtons;
    boolean[] selectedDays = new boolean[7]; // Mon-Sun
    String[] weekDays = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

    // Type buttons (Good/Bad)
    MaterialButton btnGood, btnBad;
    String habitType = "Good"; // default

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

        createNotificationChannel();

        // --- INIT UI ---
        emojiInput = findViewById(R.id.emojiInput);
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        goalInput = findViewById(R.id.goalInput);
        unitSpinner = findViewById(R.id.unitSpinner);
        saveHabitBtn = findViewById(R.id.saveHabitBtn);

        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnPickTime.setOnClickListener(v -> openTimePicker());

        // --- TYPE BUTTONS ---
        btnGood = findViewById(R.id.btnGood);
        btnBad = findViewById(R.id.btnBad);

        selectTypeButton(btnGood);

        btnGood.setOnClickListener(v -> selectTypeButton(btnGood));
        btnBad.setOnClickListener(v -> selectTypeButton(btnBad));

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

        // --- EMOJI FILTER ---
        emojiInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                StringBuilder onlyEmoji = new StringBuilder();
                int i = 0;
                while (i < input.length()) {
                    int codePoint = Character.codePointAt(input, i);
                    if (Character.getType(codePoint) == Character.OTHER_SYMBOL) {
                        onlyEmoji.append(Character.toChars(codePoint));
                    }
                    i += Character.charCount(codePoint);
                }
                String filtered = onlyEmoji.toString();
                if (!filtered.equals(input)) {
                    emojiInput.setText(filtered);
                    emojiInput.setSelection(filtered.length());
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        // --- DAY BUTTONS ---
        monBtn = findViewById(R.id.monBtn);
        tueBtn = findViewById(R.id.tueBtn);
        wedBtn = findViewById(R.id.wedBtn);
        thuBtn = findViewById(R.id.thuBtn);
        friBtn = findViewById(R.id.friBtn);
        satBtn = findViewById(R.id.satBtn);
        sunBtn = findViewById(R.id.sunBtn);

        dayButtons = new MaterialButton[]{monBtn, tueBtn, wedBtn, thuBtn, friBtn, satBtn, sunBtn};

        for (int i = 0; i < dayButtons.length; i++) {
            final int index = i;
            dayButtons[i].setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];
                if (selectedDays[index]) {
                    dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#187D24")));
                    dayButtons[index].setTextColor(Color.WHITE);
                } else {
                    dayButtons[index].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                    dayButtons[index].setTextColor(Color.BLACK);
                }
            });
        }

        // --- HANDLE INTENT ---
        Intent intent = getIntent();
        if (intent != null) {
            mode = intent.getStringExtra("MODE");
            habitId = intent.getStringExtra("habitId");

            if ("EDIT".equals(mode) && habitId != null) {
                emojiInput.setText(intent.getStringExtra("emoji"));
                titleInput.setText(intent.getStringExtra("title"));
                descInput.setText(intent.getStringExtra("description"));
                goalInput.setText(intent.getStringExtra("goal"));

                timeString = intent.getStringExtra("time");
                if (timeString != null) {
                    tvSelectedTime.setText(timeString);
                    isTimeSelected = true;
                    try {
                        String[] parts = timeString.split(":");
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    } catch (Exception ignored) {}
                }

                String unitValue = intent.getStringExtra("unit");
                if (unitValue != null && unitSpinner.getAdapter() != null) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) unitSpinner.getAdapter();
                    int spinnerPosition = adapter.getPosition(unitValue);
                    if (spinnerPosition != -1) unitSpinner.setSelection(spinnerPosition);
                }

                colour = intent.getStringExtra("color");
                habitType = intent.getStringExtra("type");
                String days = intent.getStringExtra("days");

                if ("Good".equalsIgnoreCase(habitType)) selectTypeButton(btnGood);
                else selectTypeButton(btnBad);

                if (colour != null) {
                    for (int i = 0; i < colors.length; i++) {
                        if (colors[i].equalsIgnoreCase(colour)) {
                            selectColor(colors[i], allColorButtons[i]);
                            break;
                        }
                    }
                }

                if (days != null && !days.isEmpty()) {
                    String[] daysArr = days.split(",");
                    for (int i = 0; i < weekDays.length; i++) {
                        for (String d : daysArr) {
                            if (weekDays[i].equalsIgnoreCase(d.trim())) {
                                selectedDays[i] = true;
                                dayButtons[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#187D24")));
                                dayButtons[i].setTextColor(Color.WHITE);
                            }
                        }
                    }
                }

                saveHabitBtn.setText("Update Habit");
            } else {
                receiveHabitTemplate();
                Random random = new Random();
                colour = colors[random.nextInt(colors.length)];
            }
        }

        saveHabitBtn.setOnClickListener(v -> saveHabit());
    }

    private void openTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.SECOND, 0);

                    isTimeSelected = true;
                    timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    tvSelectedTime.setText(timeString);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void scheduleNotification(String habitTitle) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, HabitAlarmReceiver.class);
        intent.putExtra("HABIT_NAME", habitTitle);

        // We need to pass the same ID so the Receiver can reschedule it correctly
        int id = (int) System.currentTimeMillis();
        intent.putExtra("REQUEST_CODE", id);

        PendingIntent pi = PendingIntent.getBroadcast(this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Safety check: ensure we aren't setting a time that ALREADY passed
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "habit_channel",
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Channel for habit daily reminders");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void selectColor(String selectedColor, MaterialButton clickedBtn) {
        colour = selectedColor;
        for (MaterialButton btn : allColorButtons) {
            btn.setText("");
        }
        clickedBtn.setText("✔");
    }

    private void selectTypeButton(MaterialButton clickedBtn) {
        if (clickedBtn == btnGood) {
            habitType = "Good";
            btnGood.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnGood.setTextColor(Color.WHITE);
            btnBad.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnBad.setTextColor(Color.BLACK);
        } else {
            habitType = "Bad";
            btnBad.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            btnBad.setTextColor(Color.WHITE);
            btnGood.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnGood.setTextColor(Color.BLACK);
        }
    }

    private void saveHabit() {
        String emoji = emojiInput.getText().toString().trim();
        String title = titleInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();

        String unit = "times";
        if (unitSpinner.getSelectedItem() != null) {
            unit = unitSpinner.getSelectedItem().toString();
        }

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
        } else {
            goalInput.setError("Goal is required");
            return;
        }

        StringBuilder daysBuilder = new StringBuilder();
        for (int i = 0; i < selectedDays.length; i++) {
            if (selectedDays[i]) {
                if (daysBuilder.length() > 0) daysBuilder.append(",");
                daysBuilder.append(weekDays[i]);
            }
        }
        String days = daysBuilder.toString();

        Habit habit;
        if ("EDIT".equals(mode) && habitId != null) {
            habit = new Habit(habitId, emoji, title, desc, colour, habitType, goal, unit, days, timeString);
            db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(habit)
                    .addOnSuccessListener(unused -> {
                        if (isTimeSelected) scheduleNotification(title);
                        Toast.makeText(this, "Habit updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            String newHabitId = UUID.randomUUID().toString();
            habit = new Habit(newHabitId, emoji, title, desc, colour, habitType, goal, unit, days, timeString);
            db.collection("users").document(userId).collection("habits").document(newHabitId)
                    .set(habit)
                    .addOnSuccessListener(unused -> {
                        if (isTimeSelected) scheduleNotification(title);
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
}