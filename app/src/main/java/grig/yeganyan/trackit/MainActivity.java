package grig.yeganyan.trackit;


import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton addBtn = findViewById(R.id.addHabitButton);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new MainFragment())
                .commit();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddHabit.class);
                startActivity(i);
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new MainFragment();
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
            } else if (id == R.id.nav_habit) {
                selected = new HabitFragment();
            }
            else if (id == R.id.nav_todo) {
                selected = new ToDoList();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selected)
                        .commit();
            }
            return true;
        });


    }
}
