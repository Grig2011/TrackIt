package grig.yeganyan.trackit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import grig.yeganyan.trackit.model.Habit;

public class MainFragment extends Fragment {
    private View openedCard = null;
    private ImageButton openedEdit = null;
    private ImageButton openedDelete = null;
    private LinearLayout habitsContainer;
    private String userId;
    private EditText searchInput;

    private String currentEditingHabitId;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private double initialSteps = -1;

    private final ActivityResultLauncher<Intent> habitLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double updatedValue = data.getDoubleExtra("updated_value", 0);
                    int streakCount = data.getIntExtra("streak_count", 0);
                    int progress = data.getIntExtra("progress", 0);
                    String lastDate = data.getStringExtra("last_completed_date");

                    if (currentEditingHabitId != null) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("habits")
                                .document(currentEditingHabitId)
                                .update(
                                        "streak", streakCount,
                                        "progress", progress,
                                        "currentValue", updatedValue,
                                        "lastCompletedDate", lastDate
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Habit Updated!", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
    );
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        habitsContainer = view.findViewById(R.id.habits_container);
        FloatingActionButton addBtn = view.findViewById(R.id.addHabitButton);

        addBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddHabit.class)));

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(getContext(), "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            return view;
        }

        searchInput = view.findViewById(R.id.searchInput);
        setupSearchInput();

        loadHabits();
        return view;
    }

    private void setupSearchInput() {
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }
                searchInput.clearFocus();
                return true;
            }
            return false;
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterHabits(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHabits() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener((value, error) -> {
                    // SAFETY CHECK: If the fragment is detached, stop immediately
                    if (error != null || !isAdded() || getContext() == null) return;

                    habitsContainer.removeAllViews();

                    if (value != null) {
                        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.add(java.util.Calendar.DATE, -1);
                        String yesterday = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());

                        // Use getContext() instead of requireContext() for safety inside the loop
                        android.content.SharedPreferences prefs = getContext().getSharedPreferences("HabitResets", android.content.Context.MODE_PRIVATE);

                        for (QueryDocumentSnapshot doc : value) {
                            Habit habit = doc.toObject(Habit.class);
                            habit.setId(doc.getId());

                            // --- 1. PROGRESS RESET LOGIC ---
                            String lastResetDate = prefs.getString("reset_" + habit.getId(), "");
                            if (!lastResetDate.equals(today)) {
                                habit.setCurrentValue(0);
                                habit.setProgress(0);

                                db.collection("users")
                                        .document(userId)
                                        .collection("habits")
                                        .document(habit.getId())
                                        .update("currentValue", 0, "progress", 0);

                                prefs.edit().putString("reset_" + habit.getId(), today).apply();
                            }

                            // --- 2. STREAK RESET LOGIC ---
                            String lastDone = habit.getLastCompletedDate();
                            if (lastDone != null && !lastDone.isEmpty()) {
                                if (!lastDone.equals(today) && !lastDone.equals(yesterday)) {
                                    habit.setStreak(0);
                                    db.collection("users")
                                            .document(userId)
                                            .collection("habits")
                                            .document(habit.getId())
                                            .update("streak", 0);
                                }
                            }

                            addHabitCard(habit);
                        }
                    }
                });
    }
    private void addHabitCard(Habit habit) {
        if (getContext() == null || habitsContainer == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View card = inflater.inflate(R.layout.item_habit, habitsContainer, false);

        TextView title = card.findViewById(R.id.habitTitle);
        TextView desc = card.findViewById(R.id.habitDesc);
        TextView goal = card.findViewById(R.id.habitGoal);
        TextView streak = card.findViewById(R.id.Strak);
        LinearProgressIndicator progress = card.findViewById(R.id.habitProgress);
        View colorBar = card.findViewById(R.id.colorBar);
        ImageButton deleteButton = card.findViewById(R.id.deleteButton);
        ImageButton editButton = card.findViewById(R.id.editButton);
        LinearLayout cardContent = card.findViewById(R.id.cardContent);

        title.setText((habit.getEmoji() != null ? habit.getEmoji() + " " : "") + habit.getTitle());
        desc.setText(habit.getDescription());
        goal.setText(habit.getGoal() > 0 ? "Goal: " + habit.getGoal() + " " + habit.getUnit() : "");
        streak.setText(habit.getStreak() + "🔥");

        try {
            int color = Color.parseColor(habit.getColor());
            title.setTextColor(color);
            desc.setTextColor(color);
            goal.setTextColor(color);
            colorBar.setBackgroundColor(color);
            progress.setIndicatorColor(color);
            progress.setTrackColor(Color.parseColor("#E0E0E0"));

            double brightness = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color));
            editButton.setColorFilter(brightness > 186 ? Color.BLACK : Color.WHITE);
            GradientDrawable bg = (GradientDrawable) editButton.getBackground().mutate();
            bg.setColor(color);
        } catch (Exception e) {
            title.setTextColor(Color.parseColor("#7E57C2"));
            desc.setTextColor(Color.parseColor("#7E57C2"));
            goal.setTextColor(Color.parseColor("#7E57C2"));
            colorBar.setBackgroundColor(Color.parseColor("#7E57C2"));
            progress.setIndicatorColor(Color.parseColor("#7E57C2"));
            progress.setTrackColor(Color.parseColor("#D1C4E9"));
        }

        double current = habit.getCurrentValue();
        double goalVal = habit.getGoal();

        int prog = goalVal > 0 ? (int) Math.min((current / goalVal) * 100, 100) : 0;
        progress.setProgress(prog);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        editButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);


        setupDragSwipe(cardContent, editButton, deleteButton, habit);


        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habit.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                habitsContainer.removeView(card);
                                Toast.makeText(getContext(), "Habit deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete habit", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show()
        );

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddHabit.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("title", habit.getTitle());
            intent.putExtra("description", habit.getDescription());
            intent.putExtra("emoji", habit.getEmoji());
            intent.putExtra("goal", String.valueOf(habit.getGoal()));
            intent.putExtra("color", habit.getColor());
            intent.putExtra("type", habit.getType());
            intent.putExtra("unit", habit.getUnit());
            intent.putExtra("days", habit.getDays());
            intent.putExtra("time",habit.getTime());
            intent.putExtra("currentStreak", habit.getStreak());
            v.getContext().startActivity(intent);
        });
        card.setOnClickListener(v -> {
            currentEditingHabitId = habit.getId();
            Intent intent = new Intent(getActivity(), HabitdetailActivity.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("habit_name", habit.getTitle());
            intent.putExtra("habit_emoji", habit.getEmoji());
            intent.putExtra("habit_goal", habit.getGoal());
            intent.putExtra("habit_unit", habit.getUnit());
            intent.putExtra("habit_type", habit.getType());
            intent.putExtra("current_value", habit.getCurrentValue());
            intent.putExtra("streak_count", habit.getStreak());
            intent.putExtra("last_completed_date", habit.getLastCompletedDate());

            habitLauncher.launch(intent);
        });

        habitsContainer.addView(card);
    }

    private void setupDragSwipe(View cardContent, ImageButton editButton, ImageButton deleteButton, Habit habit) {

        cardContent.setOnTouchListener(new View.OnTouchListener() {

            float downX;
            float translation = 0;
            float swipeDistance = 0;
            boolean dragging = false;

            final float TOUCH_SLOP = 12f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (swipeDistance == 0)
                    swipeDistance = v.getWidth() * 0.35f;

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX() - translation;
                        dragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float delta = event.getRawX() - downX;

                        if (!dragging && Math.abs(delta) > TOUCH_SLOP) {
                            dragging = true;
                        }

                        if (!dragging) return true;

                        float limited = Math.max(0, Math.min(delta, swipeDistance));
                        float progress = limited / swipeDistance;

                        float resistance = (float) (1 - Math.pow(progress, 2));
                        translation = limited * resistance + limited * (1 - resistance);

                        v.setTranslationX(translation);

                        float alpha = translation / swipeDistance;

                        editButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);

                        editButton.setAlpha(alpha);
                        deleteButton.setAlpha(alpha);

                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:

                        if (!dragging && Math.abs(event.getRawX() - (downX + translation)) < TOUCH_SLOP) {
                            currentEditingHabitId = habit.getId();
                            Intent intent = new Intent(getActivity(), HabitdetailActivity.class);
                            intent.putExtra("habit_name", habit.getTitle());
                            intent.putExtra("habit_emoji", habit.getEmoji());
                            intent.putExtra("habit_goal", habit.getGoal());
                            intent.putExtra("habit_unit", habit.getUnit());
                            intent.putExtra("current_value", habit.getCurrentValue());
                            intent.putExtra("streak_count", habit.getStreak());
                            intent.putExtra("last_completed_date", habit.getLastCompletedDate());
                            intent.putExtra("habit_color", habit.getColor());
                            intent.putExtra("habit_type", habit.getType());
                            habitLauncher.launch(intent);
                            v.performClick();
                            return true;
                        }

                        float velocity = event.getRawX() - downX;

                        boolean open =
                                translation > swipeDistance * 0.25f ||
                                        velocity > 1200;

                        if (open) {

                            if (openedCard != null && openedCard != v) {
                                closeCard(openedCard, openedEdit, openedDelete);
                            }

                            v.animate()
                                    .translationX(swipeDistance)
                                    .setDuration(140)
                                    .start();

                            editButton.setAlpha(1f);
                            deleteButton.setAlpha(1f);

                            openedCard = v;
                            openedEdit = editButton;
                            openedDelete = deleteButton;

                            translation = swipeDistance;

                        } else {

                            closeCard(v, editButton, deleteButton);
                            translation = 0;
                        }

                        return true;
                }

                return false;
            }
        });
    }

    private void filterHabits(String text) {
        for (int i = 0; i < habitsContainer.getChildCount(); i++) {
            View card = habitsContainer.getChildAt(i);
            TextView title = card.findViewById(R.id.habitTitle);
            TextView desc = card.findViewById(R.id.habitDesc);

            String titleText = title.getText().toString().toLowerCase();
            String descText = desc.getText().toString().toLowerCase();

            card.setVisibility(titleText.contains(text.toLowerCase()) || descText.contains(text.toLowerCase())
                    ? View.VISIBLE : View.GONE);
        }
    }
    private void closeCard(View card, ImageButton edit, ImageButton delete) {

        card.animate()
                .translationX(0)
                .setDuration(140)
                .start();

        edit.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> edit.setVisibility(View.GONE))
                .start();

        delete.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> delete.setVisibility(View.GONE))
                .start();

        if (card == openedCard) {
            openedCard = null;
            openedEdit = null;
            openedDelete = null;
        }
    }
}