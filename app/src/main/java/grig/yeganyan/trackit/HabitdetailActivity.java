package grig.yeganyan.trackit;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import grig.yeganyan.trackit.CircularProgressView;
import grig.yeganyan.trackit.R;

public class HabitdetailActivity extends AppCompatActivity {


    private TextView tvHabitName, tvHabitEmoji, tvCurrentValue, tvGoalLabel, tvStreakCount;
    private CircularProgressView circularProgress;
    private TextInputEditText etProgressInput;
    private MaterialButton btnAdd, btnDone, btnPlus, btnMinus;
    private RadioGroup rgStepSize;
    private LinearLayout layoutStreak;
    private RecyclerView rvEntries;


    private double currentValue = 0;
    private double goal = 1000;
    private String unit = "steps";
    private String habitName = "Walk";
    private String habitEmoji = "🌲";
    private int stepSize = 10;
    private int streakCount = 0;

    private final List<ProgressEntry> entries = new ArrayList<>();
    private ProgressEntryAdapter adapter;


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

    // ── Intent extras ─────────────────────────────────────────────────────
    private void readIntentExtras() {
        if (getIntent() != null) {
            habitName   = getIntent().getStringExtra("habit_name")  != null
                    ? getIntent().getStringExtra("habit_name") : habitName;
            habitEmoji  = getIntent().getStringExtra("habit_emoji") != null
                    ? getIntent().getStringExtra("habit_emoji") : habitEmoji;
            goal        = getIntent().getDoubleExtra("habit_goal", 1000);
            unit        = getIntent().getStringExtra("habit_unit") != null
                    ? getIntent().getStringExtra("habit_unit") : unit;
            currentValue = getIntent().getDoubleExtra("current_value", 0);
            streakCount = getIntent().getIntExtra("streak_count", 0);
        }
    }

    // ── View binding ──────────────────────────────────────────────────────
    private void bindViews() {
        tvHabitName    = findViewById(R.id.tvHabitName);
        tvHabitEmoji   = findViewById(R.id.tvHabitEmoji);
        tvCurrentValue = findViewById(R.id.tvCurrentValue);
        tvGoalLabel    = findViewById(R.id.tvGoalLabel);
        tvStreakCount  = findViewById(R.id.tvStreakCount);

        // Fix: Assign to the correct field and cast to your custom class
        circularProgress = findViewById(R.id.circularProgress);

        etProgressInput  = findViewById(R.id.etProgressInput);
        btnAdd   = findViewById(R.id.btnAdd);
        btnDone  = findViewById(R.id.btnDone);
        btnPlus  = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        rgStepSize   = findViewById(R.id.rgStepSize);
        layoutStreak = findViewById(R.id.layoutStreak);
        rvEntries    = findViewById(R.id.rvEntries);
    }

    // ── Initial UI state ──────────────────────────────────────────────────
    private void setupInitialState() {
        tvHabitName.setText(habitEmoji + " " + habitName);
        tvHabitEmoji.setText(habitEmoji);
        updateDisplay(false);
        if (streakCount > 0) showStreak(false);
    }

    // ── Listeners ─────────────────────────────────────────────────────────
    private void setupListeners() {

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Step size radio
        rgStepSize.setOnCheckedChangeListener((group, checkedId) -> {
            if      (checkedId == R.id.rb1)   stepSize = 1;
            else if (checkedId == R.id.rb10)  stepSize = 10;
            else if (checkedId == R.id.rb100) stepSize = 100;
        });

        // Plus / Minus
        btnPlus.setOnClickListener(v -> adjustValue(stepSize));
        btnMinus.setOnClickListener(v -> adjustValue(-stepSize));

        // Long-press for rapid change
        btnPlus.setOnLongClickListener(v -> {
            adjustValue(stepSize * 5);
            return true;
        });
        btnMinus.setOnLongClickListener(v -> {
            adjustValue(-stepSize * 5);
            return true;
        });

        // Add button — uses typed value if present, else uses stepSize
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

        // Done — marks habit complete for today, returns result
        btnDone.setOnClickListener(v -> {
            // Persist changes here (Room / SharedPrefs / your storage)
            setResult(RESULT_OK, getIntent()
                    .putExtra("updated_value", currentValue)
                    .putExtra("streak_count", streakCount));
            finish();
        });
    }

    // ── RecyclerView ──────────────────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new ProgressEntryAdapter(entries, unit);
        rvEntries.setLayoutManager(new LinearLayoutManager(this));
        rvEntries.setAdapter(adapter);
    }

    // ── Core logic ────────────────────────────────────────────────────────

    /** Called by +/- buttons directly */
    private void adjustValue(double delta) {
        applyDelta(delta);
    }

    /**
     * Central method that:
     * 1. Adds delta to currentValue (clamps to 0)
     * 2. Records the entry
     * 3. Checks goal and awards streak
     * 4. Updates the UI
     */
    private void applyDelta(double delta) {
        double before = currentValue;
        currentValue = Math.max(0, currentValue + delta);

        // Record entry
        entries.add(0, new ProgressEntry(delta, currentValue, new Date()));
        adapter.notifyItemInserted(0);
        rvEntries.scrollToPosition(0);

        // Check goal crossing (ascending only — don't award streak on decrease)
        boolean crossedGoal = before < goal && currentValue >= goal;
        if (crossedGoal) {
            streakCount++;
            showStreak(true);
            // Optionally: Toast or celebration animation
            Toast.makeText(this, "🎉 Goal reached! Streak: " + streakCount, Toast.LENGTH_SHORT).show();
        }

        updateDisplay(true);
    }

    // ── UI helpers ────────────────────────────────────────────────────────

    private void updateDisplay(boolean animate) {
        // Number text
        String formatted = currentValue == (long) currentValue
                ? String.valueOf((long) currentValue)
                : String.valueOf(currentValue);
        tvCurrentValue.setText(formatted);

        // Goal label
        String goalFormatted = goal == (long) goal
                ? String.valueOf((long) goal)
                : String.valueOf(goal);
        tvGoalLabel.setText("/" + goalFormatted + " " + unit);

        // Circular progress (0.0 – 1.0, capped at 1)
        float progress = (float) Math.min(currentValue / goal, 1.0);
        if (animate) {
            circularProgress.animateProgress(progress);
        } else {
            circularProgress.setProgress(progress);
        }

        // Animate number on change
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
}
