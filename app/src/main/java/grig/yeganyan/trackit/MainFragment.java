package grig.yeganyan.trackit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    private LinearLayout habits_container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        habits_container = view.findViewById(R.id.habits_container);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("habits")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }


                    habits_container.removeAllViews();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Habit habit = doc.toObject(Habit.class);
                            habit.id = doc.getId();
                            addHabitCard(habit);
                        }
                    }
                });

        return view;
    }

    private void addHabitCard(Habit habit) {

        if (getContext() == null || habits_container == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View card = inflater.inflate(R.layout.item_habit, habits_container, false);

        TextView title = card.findViewById(R.id.habitTitle);
        TextView desc = card.findViewById(R.id.habitDesc);
        TextView goal = card.findViewById(R.id.habitGoal);
        TextView streak = card.findViewById(R.id.Strak);
        title.setTextColor(Color.parseColor(habit.color));
        goal.setTextColor(Color.parseColor(habit.color));
        desc.setTextColor(Color.parseColor(habit.color));
        LinearProgressIndicator progress = card.findViewById(R.id.habitProgress);
        View colorBar = card.findViewById(R.id.colorBar);
        ImageButton deleteButton = card.findViewById(R.id.deleteButton);
        title.setText((habit.emoji != null ? habit.emoji + " " : "") + habit.title);
        desc.setText(habit.description);
        goal.setText(habit.goal > 0 ? "Goal: " + habit.goal + " " + habit.unit : "");
        streak.setText(String.valueOf(habit.Streak+"🔥"));


        int prog = (int) Math.min(Math.max(habit.goal, 0), 100);
        progress.setProgress(prog);
        progress.setTrackColor(Color.parseColor(habit.color));

        try {
            int color = Color.parseColor(habit.color);
            colorBar.setBackgroundColor(color);
            progress.setIndicatorColor(color);
            progress.setTrackColor(Color.parseColor("#E0E0E0"));
        } catch (Exception e) {
            colorBar.setBackgroundColor(Color.parseColor("#7E57C2"));
            progress.setIndicatorColor(Color.parseColor("#7E57C2"));
            progress.setTrackColor(Color.parseColor("#D1C4E9"));
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            db.collection("habits").document(habit.id).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        habits_container.removeView(card); // remove from UI
                                        Toast.makeText(getContext(), "Habit deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to delete habit", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // User pressed Cancel
                            dialog.dismiss();
                        }
                    })
                    .show();

        });


        habits_container.addView(card);
    }
}