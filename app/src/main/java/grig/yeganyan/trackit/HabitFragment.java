package grig.yeganyan.trackit;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
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





        CardView card2 = view.findViewById(R.id.habitCard2);
        CardView card3 = view.findViewById(R.id.habitCard3);
        CardView card4 = view.findViewById(R.id.habitCard4);
        CardView card5 = view.findViewById(R.id.habitCard5);
        CardView card6 = view.findViewById(R.id.habitCard6);
        CardView card7 = view.findViewById(R.id.habitCard7);
        CardView card8 = view.findViewById(R.id.habitCard8);
        CardView card9 = view.findViewById(R.id.habitCard9);
        CardView card10 = view.findViewById(R.id.habitCard10);



        card2.setOnClickListener(v -> openAddHabit("💧", "Drink Water", "8 glasses daily"));
        card3.setOnClickListener(v -> openAddHabit("📖", "Read Book", "20 pages daily"));
        card4.setOnClickListener(v -> openAddHabit("🚶‍♂️", "Evening Walk", "30 min walk"));
        card5.setOnClickListener(v -> openAddHabit("🤸‍♂️", "Morning Stretch", "Wake up your body in 10 minutes"));
        card6.setOnClickListener(v -> openAddHabit("📝", "Journaling", "Reflect on your day in 5 minutes"));
        card7.setOnClickListener(v -> openAddHabit("💻", "Daily Coding", "Solve a coding problem every day"));
        card8.setOnClickListener(v -> openAddHabit("🌙", "Sleep Early", "Set a bedtime and stick to it"));
        card9.setOnClickListener(v -> openAddHabit("🥗", "Healthy Snack", "Choose fruits or nuts instead of junk food"));
        card10.setOnClickListener(v -> openAddHabit("🌅", "Evening Reflection", "Spend 5 minutes reviewing your day"));

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