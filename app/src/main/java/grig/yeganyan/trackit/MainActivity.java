package grig.yeganyan.trackit;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_CURRENT_FRAGMENT = "currentFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, false);

        // Apply theme before setContentView
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }



        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Restore last fragment if exists
        String lastFragment = prefs.getString(KEY_CURRENT_FRAGMENT, "");
        Fragment initialFragment;

        switch (lastFragment) {
            case "Profile":
                initialFragment = new ProfileFragment();
                bottomNav.setSelectedItemId(R.id.nav_profile);
                break;
            case "Habit":
                initialFragment = new HabitFragment();
                bottomNav.setSelectedItemId(R.id.nav_habit);
                break;
            case "ToDo":
                initialFragment = new ToDoList();
                bottomNav.setSelectedItemId(R.id.nav_todo);
                break;
            case "Home":
            default:
                initialFragment = new MainFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);
                break;
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, initialFragment)
                .commit();

        // Bottom nav listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            String fragmentName = "Home";

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new MainFragment();
                fragmentName = "Home";
            } else if (id == R.id.nav_profile) {
                selected = new ProfileFragment();
                fragmentName = "Profile";
            } else if (id == R.id.nav_habit) {
                selected = new HabitFragment();
                fragmentName = "Habit";
            } else if (id == R.id.nav_chat) {
                selected = new ChatFragment();
                fragmentName = "Chat";
            }
            else if (id == R.id.nav_todo) {
                selected = new ToDoList();
                fragmentName = "ToDo";
            }

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragmentContainer, selected)
                        .commit();

                // Save current fragment
                prefs.edit().putString(KEY_CURRENT_FRAGMENT, fragmentName).apply();
            }

            return true;
        });
    }
}