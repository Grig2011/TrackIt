package grig.yeganyan.trackit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import grig.yeganyan.trackit.model.Habit;

public class AddHabit extends AppCompatActivity {

    TextInputEditText emojiInput, titleInput, descInput, goalInput;
    Spinner typeSpinner, unitSpinner, daysSpinner;
    Button saveHabitBtn;

    DatabaseReference habitsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);
        Button back = findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddHabit.this,MainActivity.class);
                startActivity(i);
            }
        });

        // Views
        emojiInput = findViewById(R.id.emojiInput);
        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        goalInput = findViewById(R.id.goalInput);

        typeSpinner = findViewById(R.id.typeSpinner);
        unitSpinner = findViewById(R.id.unitSpinner);
        daysSpinner = findViewById(R.id.daysSpinner);

        saveHabitBtn = findViewById(R.id.saveHabitBtn);

        // Firebase reference
        habitsRef = FirebaseDatabase.getInstance()
                .getReference("habits");

        saveHabitBtn.setOnClickListener(v -> saveHabit());
    }

    private void saveHabit() {

        String emoji = emojiInput.getText().toString().trim();
        String title = titleInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String unit = unitSpinner.getSelectedItem().toString();
        String days = daysSpinner.getSelectedItem().toString();

        if (title.isEmpty()) {
            titleInput.setError("Title required");
            return;
        }

        double goal = 0;
        if (!goalInput.getText().toString().isEmpty()) {
            goal = Double.parseDouble(goalInput.getText().toString());
        }

        String habitId = habitsRef.push().getKey();

        Habit habit = new Habit(
                habitId,
                emoji,
                title,
                desc,
                "#6200EE",
                type,
                goal,
                unit,
                days
        );

        habitsRef.child(habitId).setValue(habit)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show();
                    finish(); // go back to MainActivity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
