package grig.yeganyan.trackit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainFragment extends Fragment {
    boolean isDark = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        LinearLayout root = view.findViewById(R.id.root_layout);
        LinearLayout HabbitCard = view.findViewById(R.id.HabitCard);

        // Theme toggle button
        Button themeBtn = view.findViewById(R.id.ThemeButton);

        // Text
        TextView header1 = view.findViewById(R.id.header1);
        TextView header2 = view.findViewById(R.id.header2);
        TextView desc1 = view.findViewById(R.id.habitDesc1);
        TextView streak1 = view.findViewById(R.id.habitStreak1);
        TextView habitTitle1 = view.findViewById(R.id.habitTitle1);
        TextView habitDesc1 = view.findViewById(R.id.habitDesc1);

        // Progress
        LinearProgressIndicator progress1 = view.findViewById(R.id.habitProgress1);

        // Theme toggle logic
        themeBtn.setOnClickListener(v -> {
            if (!isDark) {
                themeBtn.setText("☀️");

                HabbitCard.setBackgroundColor(Color.parseColor("#4f4e4d"));
                habitTitle1.setTextColor(Color.WHITE);
                habitDesc1.setTextColor(Color.WHITE);

                root.setBackgroundColor(Color.parseColor("#121212"));
                header1.setTextColor(Color.WHITE);
                header2.setTextColor(Color.parseColor("#B3B3B3"));
                desc1.setTextColor(Color.parseColor("#B3B3B3"));
                streak1.setTextColor(Color.parseColor("#FF7043"));
                progress1.setIndicatorColor(Color.parseColor("#7C4DFF"));


                isDark = true;
            } else {
                themeBtn.setText("🌙️");

                HabbitCard.setBackgroundColor(Color.parseColor("#E0E0E0"));
                habitTitle1.setTextColor(Color.BLACK);
                habitDesc1.setTextColor(Color.BLACK);

                root.setBackgroundColor(Color.parseColor("#F5F5F5"));
                header1.setTextColor(Color.BLACK);
                header2.setTextColor(Color.parseColor("#444444"));
                desc1.setTextColor(Color.parseColor("#757575"));
                streak1.setTextColor(Color.parseColor("#FF5722"));
                progress1.setIndicatorColor(Color.parseColor("#6200EE"));
                isDark = false;
            }
        });

        return view;
    }
}
