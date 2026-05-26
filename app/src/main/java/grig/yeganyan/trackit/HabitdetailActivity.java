package grig.yeganyan.trackit;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import grig.yeganyan.trackit.CircularProgressView;
import grig.yeganyan.trackit.R;
import grig.yeganyan.trackit.Services.HabitService;
import grig.yeganyan.trackit.model.Habit;

public class HabitdetailActivity extends AppCompatActivity {

    private View rootLayout;
    private TextView tvHabitName, tvHabitEmoji, tvCurrentValue, tvGoalLabel, tvStreakCount;
    private CircularProgressView circularProgress;
    private TextInputEditText etProgressInput;
    private MaterialButton btnAdd, btnDone, btnPlus, btnMinus;
    private RadioGroup rgStepSize;
    private LinearLayout layoutStreak;
    private RecyclerView rvEntries;

    private boolean isStreakIncreasedThisSession = false;


    private double currentValue = 0;
    private double goal = 1000;
    private String unit = "steps";
    private String habitName = "Walk";
    private String habitEmoji = "🌲";
    private int stepSize = 10;
    private int streakCount = 0;
    public String lastCompletedDate;
    private Habit habit;


    private final List<ProgressEntry> entries = new ArrayList<>();
    private ProgressEntryAdapter adapter;

    private String habitType = "Good";
    private String habitId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habitdetail);

        readIntentExtras();
        bindViews();
        setupInitialState();
        setupListeners();
        setupRecyclerView();

    }


    private void readIntentExtras() {
        if (getIntent() != null) {
            habitId = getIntent().getStringExtra("habit_id");
            habitName = getIntent().getStringExtra("habit_name");
            habitEmoji = getIntent().getStringExtra("habit_emoji");
            goal = getIntent().getDoubleExtra("habit_goal", 1000);
            unit = getIntent().getStringExtra("habit_unit");
            habitType = getIntent().getStringExtra("habit_type");
            streakCount = getIntent().getIntExtra("streak_count", 0);
            lastCompletedDate = getIntent().getStringExtra("last_completed_date");
            habit = (Habit) getIntent().getSerializableExtra("habit");

            String today = getTodayDateString();
            String yesterday = getYesterdayDateString();


            android.content.SharedPreferences prefs = getSharedPreferences("TrackItPrefs", MODE_PRIVATE);
            String lastSeenDate = prefs.getString("last_seen_" + habitId, "");

            if (!lastSeenDate.equals(today)) {
                currentValue = 0;
            } else {
                currentValue = getIntent().getDoubleExtra("current_value", 0);
            }


            if (lastCompletedDate != null && !lastCompletedDate.isEmpty()) {
                if (!lastCompletedDate.equals(today) && !lastCompletedDate.equals(yesterday)) {
                    streakCount = 0;
                }
            }
        }
    }

    private void bindViews() {

        tvHabitName = findViewById(R.id.tvHabitName);
        tvHabitEmoji = findViewById(R.id.tvHabitEmoji);
        tvCurrentValue = findViewById(R.id.tvCurrentValue);
        tvGoalLabel = findViewById(R.id.tvGoalLabel);
        tvStreakCount = findViewById(R.id.tvStreakCount);


        circularProgress = findViewById(R.id.circularProgress);

        etProgressInput = findViewById(R.id.etProgressInput);
        btnAdd = findViewById(R.id.btnAdd);
        btnDone = findViewById(R.id.btnDone);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        rgStepSize = findViewById(R.id.rgStepSize);
        layoutStreak = findViewById(R.id.layoutStreak);
        rvEntries = findViewById(R.id.rvEntries);
        rootLayout = findViewById(R.id.root_layout);
    }


    private void setupInitialState() {
        tvHabitName.setText(habitEmoji + " " + habitName);
        tvHabitEmoji.setText(habitEmoji);
        String habitColor = getIntent().getStringExtra("habit_color");
        if (habitColor != null) {
            applyThemeColor(habitColor);
        }
        updateDisplay(false);
        if (streakCount > 0) showStreak(false);
    }


    private void setupListeners() {


        findViewById(R.id.btnBack).setOnClickListener(v -> finish());


        rgStepSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb1) stepSize = 1;
            else if (checkedId == R.id.rb10) stepSize = 10;
            else if (checkedId == R.id.rb100) stepSize = 100;
        });


        btnPlus.setOnClickListener(v -> adjustValue(stepSize));
        btnMinus.setOnClickListener(v -> adjustValue(-stepSize));


        btnPlus.setOnLongClickListener(v -> {
            adjustValue(stepSize * 5);
            return true;
        });
        btnMinus.setOnLongClickListener(v -> {
            adjustValue(-stepSize * 5);
            return true;
        });


        btnAdd.setOnClickListener(v -> {
            String input = etProgressInput.getText() != null
                    ? etProgressInput.getText().toString().trim() : "";
            double delta;
            if (!TextUtils.isEmpty(input)) {
                try {
                    delta = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                delta = stepSize;
            }
            applyDelta(delta);
            etProgressInput.setText("");
        });


        btnDone.setOnClickListener(v -> {
            String today = getTodayDateString();

            android.content.SharedPreferences prefs = getSharedPreferences("TrackItPrefs", MODE_PRIVATE);
            prefs.edit().putString("last_seen_" + habitId, today).apply();

            int progressPercentage = (goal > 0) ? (int) Math.min((currentValue / goal) * 100, 100) : 0;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_value", currentValue);
            resultIntent.putExtra("streak_count", streakCount);
            resultIntent.putExtra("last_completed_date", lastCompletedDate);
            resultIntent.putExtra("progress", progressPercentage);
            resultIntent.putExtra("last_updated_date", today);
            resultIntent.putExtra("streak_increased", isStreakIncreasedThisSession);

            setResult(RESULT_OK, resultIntent);
            onHabitCompleted();
            finish();
        });
    }


    private void setupRecyclerView() {
        adapter = new ProgressEntryAdapter(entries, unit);
        rvEntries.setLayoutManager(new LinearLayoutManager(this));
        rvEntries.setAdapter(adapter);
    }


    private void adjustValue(double delta) {
        applyDelta(delta);
    }


    private void applyDelta(double delta) {

        double before = currentValue;
        currentValue = Math.max(0, currentValue + delta);


        entries.add(0, new ProgressEntry(delta, currentValue, new java.util.Date()));
        adapter.notifyItemInserted(0);
        rvEntries.scrollToPosition(0);


        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());


        boolean crossedGoalUp = (before < goal) && (currentValue >= goal);


        boolean crossedGoalDown = (before >= goal) && (currentValue < goal);
        if ("Good".equalsIgnoreCase(habitType)) {
            if (crossedGoalUp) {

                if (lastCompletedDate == null || !lastCompletedDate.equals(today)) {
                    streakCount++;
                    lastCompletedDate = today;
                    showStreak(true);
                    isStreakIncreasedThisSession = true;
                    Toast.makeText(this, "🎉 Goal reached! Streak: " + streakCount, Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, "Goal reached again!", Toast.LENGTH_SHORT).show();
                }
            } else if (crossedGoalDown) {

                if (today.equals(lastCompletedDate)) {
                    streakCount = Math.max(0, streakCount - 1);
                    lastCompletedDate = "";

                    tvStreakCount.setText(streakCount + " day streak!");

                    if (streakCount == 0) {
                        layoutStreak.setVisibility(View.GONE);
                    }
                    isStreakIncreasedThisSession = false;
                    Toast.makeText(this, "Goal unreached. Streak: " + streakCount, Toast.LENGTH_SHORT).show();
                }
            }
        }else if("Bad".equals(habitType)) {

            if (currentValue > goal) {
                streakCount = 0;
                lastCompletedDate = "";
                layoutStreak.setVisibility(View.GONE);
                isStreakIncreasedThisSession = false;
                Toast.makeText(this, "Limit reached! Streak reset.", Toast.LENGTH_SHORT).show();
            } else {
                if (lastCompletedDate == null || !lastCompletedDate.equals(today)) {
                    streakCount++;
                    lastCompletedDate = today;
                    isStreakIncreasedThisSession = true;
                    showStreak(true);
                }
            }
        }else{
            Toast.makeText(this,"Problem!!!!!"+habitType,Toast.LENGTH_SHORT).show();
        }

        updateDisplay(true);
    }







    private void updateDisplay(boolean animate) {

        String formatted = currentValue == (long) currentValue
                ? String.valueOf((long) currentValue)
                : String.valueOf(currentValue);
        tvCurrentValue.setText(formatted);


        String goalFormatted = goal == (long) goal
                ? String.valueOf((long) goal)
                : String.valueOf(goal);
        tvGoalLabel.setText("/" + goalFormatted + " " + unit);


        float progress = (float) Math.min(currentValue / goal, 1.0);
        if (animate) {
            circularProgress.animateProgress(progress);
        } else {
            circularProgress.setProgress(progress);
        }


        if (animate) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvCurrentValue, "scaleX", 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvCurrentValue, "scaleY", 1.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(200);
            set.setInterpolator(new OvershootInterpolator());
            set.start();
        }
    }

    private void showStreak(boolean animate) {
        tvStreakCount.setText(streakCount + " day streak!");
        layoutStreak.setVisibility(View.VISIBLE);
        if (animate) {
            layoutStreak.setAlpha(0f);
            layoutStreak.setScaleX(0.5f);
            layoutStreak.setScaleY(0.5f);
            layoutStreak.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .start();
        }

    }

    private String getTodayDateString() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }


    private void applyThemeColor(String colorStr) {
        try {
            int color = Color.parseColor(colorStr);


            circularProgress.setIndicatorColor(color);


            btnAdd.setBackgroundColor(color);
            btnDone.setBackgroundColor(color);


            btnPlus.setStrokeColor(android.content.res.ColorStateList.valueOf(color));
            btnPlus.setTextColor(color);
            btnMinus.setStrokeColor(android.content.res.ColorStateList.valueOf(color));
            btnMinus.setTextColor(color);


            tvHabitName.setTextColor(color);
            tvCurrentValue.setTextColor(color);

        } catch (Exception e) {
            e.printStackTrace();
        }
        int color = Color.parseColor(habit.getColor());
        double brightness = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color));
        btnDone.setIconTint(ColorStateList.valueOf(brightness > 186 ? Color.BLACK : Color.WHITE));
    }

    private String getYesterdayDateString() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, -1);
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
    }

    public void onHabitCompleted() {
        HabitService habitService = new HabitService();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        habitService.getUsersAllHabits(uid, habitList -> {
            habitService.syncBestStreak(uid, habitList);
        });
    }

}
