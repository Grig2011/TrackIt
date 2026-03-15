package grig.yeganyan.trackit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import grig.yeganyan.trackit.model.Habit;

public class MainFragment extends Fragment {

    private LinearLayout habitsContainer;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        habitsContainer = view.findViewById(R.id.habits_container);

        // Get current userId
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(getContext(), "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadHabits();

        return view;
    }

    private void loadHabits() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    habitsContainer.removeAllViews();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Habit habit = doc.toObject(Habit.class);
                            habit.id = doc.getId();
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

        title.setText((habit.emoji != null ? habit.emoji + " " : "") + habit.title);
        desc.setText(habit.description);
        goal.setText(habit.goal > 0 ? "Goal: " + habit.goal + " " + habit.unit : "");
        streak.setText(habit.Streak + "🔥");

        try {
            int color = Color.parseColor(habit.color);
            title.setTextColor(color);
            desc.setTextColor(color);
            goal.setTextColor(color);
            colorBar.setBackgroundColor(color);
            progress.setIndicatorColor(color);
            progress.setTrackColor(Color.parseColor("#E0E0E0"));
        } catch (Exception e) {
            title.setTextColor(Color.parseColor("#7E57C2"));
            desc.setTextColor(Color.parseColor("#7E57C2"));
            goal.setTextColor(Color.parseColor("#7E57C2"));
            colorBar.setBackgroundColor(Color.parseColor("#7E57C2"));
            progress.setIndicatorColor(Color.parseColor("#7E57C2"));
            progress.setTrackColor(Color.parseColor("#D1C4E9"));
        }

        int prog = habit.goal > 0 ? (int) Math.min((habit.Streak / habit.goal) * 100, 100) : 0;
        progress.setProgress(prog);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habit.id)
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

        habitsContainer.addView(card);
    }
}