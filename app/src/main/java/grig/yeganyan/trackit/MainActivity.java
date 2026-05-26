package grig.yeganyan.trackit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import grig.yeganyan.trackit.Services.HabitCallback;
import grig.yeganyan.trackit.Services.HabitService;
import grig.yeganyan.trackit.model.Habit;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_CURRENT_FRAGMENT = "currentFragment";
    private boolean isAppReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        startLoadingData();

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check if the data is loaded
                        if (isAppReady) {
                            // Data is ready, remove listener and proceed to app
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            // Data is still loading, keep holding the splash screen
                            return false;
                        }
                    }
                }
        );

        // 4. Customize the +1 Exit Animation
        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            View view = splashScreenView.getView();
            View iconView = splashScreenView.getIconView();

            // Create a fade-out animation for the entire background
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
            fadeOut.setDuration(400L);

            // Create an elegant shrink/zoom-out animation for your center logo
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0.6f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0.6f);
            scaleX.setDuration(400L);
            scaleY.setDuration(400L);

            // Group them to play at the same time with a premium Material bounce-back interpolator
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new AnticipateInterpolator());
            animatorSet.playTogether(fadeOut, scaleX, scaleY);

            // Remove the view safely when the animation finishes
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenView.remove();
                }
            });

            // Start the show
            animatorSet.start();
        });


        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(KEY_DARK_MODE, false);


        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }





        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);


        String lastFragment = prefs.getString(KEY_CURRENT_FRAGMENT, "");
        Fragment initialFragment;

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MainFragment())
                    .commit();


            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        switch (lastFragment) {
            case "Home":
                initialFragment = new MainFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);
                break;
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

            default:
                initialFragment = new MainFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);
                break;
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, initialFragment)
                .commit();


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

                prefs.edit().putString(KEY_CURRENT_FRAGMENT, fragmentName).apply();
            }

            return true;
        });
        Fragment selected = null;
        if(getIntent()!=null){

            boolean a = getIntent().getBooleanExtra("FromProfile",false);

            if(a){

                selected = new MainFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);
                loadFragment(selected);

            }

        }


    }
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    private void startLoadingData() {
        // Simulating an asynchronous database fetch or user session check (1.5 seconds)
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Switch flag on the main thread so the UI can update safely
            runOnUiThread(() -> isAppReady = true);
        }).start();
    }
}