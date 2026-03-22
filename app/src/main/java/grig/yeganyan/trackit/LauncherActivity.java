package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean registered = prefs.getBoolean("registered", false);

        if (registered) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, Register.class));
        }
        finish();
    }
}