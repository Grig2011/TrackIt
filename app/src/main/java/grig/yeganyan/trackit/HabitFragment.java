package grig.yeganyan.trackit;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HabitFragment extends Fragment {

    public HabitFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_habit, container, false);

        // Find the "add" icons
        TextView habit1 = view.findViewById(R.id.habitAddIcon1);
        TextView habit2 = view.findViewById(R.id.habitAddIcon2);
        TextView habit3 = view.findViewById(R.id.habitAddIcon3);
        TextView habit4 = view.findViewById(R.id.habitAddIcon4);

        // Set click listeners for each popular habit
        habit1.setOnClickListener(v ->
                openAddHabit("🧘‍♂️", "Morning Meditation", "15 min daily mindfulness")
        );

        habit2.setOnClickListener(v ->
                openAddHabit("💧", "Drink Water", "8 glasses daily")
        );

        habit3.setOnClickListener(v ->
                openAddHabit("📖", "Read Book", "20 pages daily")
        );

        habit4.setOnClickListener(v ->
                openAddHabit("🚶‍♂️", "Evening Walk", "30 min walk")
        );

        return view;
    }

    private void openAddHabit(String emoji, String title, String desc){
        Intent intent = new Intent(getActivity(), AddHabit.class);
        intent.putExtra("emoji", emoji);
        intent.putExtra("title", title);
        intent.putExtra("desc", desc);
        startActivity(intent);
    }
}