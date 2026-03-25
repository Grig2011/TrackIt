package grig.yeganyan.trackit;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.model.Tasks;

public class AddTask extends DialogFragment {

    private EditText etTitle, etDescription, etTime;
    private Button btnAddTask;

    private FirebaseFirestore db;
    private String currentUserId;

    private String taskId;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etTime = view.findViewById(R.id.etTime);
        btnAddTask = view.findViewById(R.id.btnAddTask);

        db = FirebaseFirestore.getInstance();

        // Get arguments from ToDoList
        if (getArguments() != null) {

            currentUserId = getArguments().getString("userId");
            taskId = getArguments().getString("taskId");

            // If taskId exists → edit mode
            if (taskId != null) {

                isEditMode = true;

                String title = getArguments().getString("title");
                String description = getArguments().getString("description");
                String time = getArguments().getString("time");

                etTitle.setText(title);
                etDescription.setText(description);
                etTime.setText(time);

                btnAddTask.setText("Update Task");
            }
        }
        etTime.setOnClickListener(v -> {

            int hour = 12;
            int minute = 0;

            // If a time is already selected, prefill it
            String currentTime = etTime.getText().toString();
            if (!currentTime.isEmpty()) {
                String[] parts = currentTime.split(":");
                if (parts.length == 2) {
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                }
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (timePicker, hourOfDay, minute1) -> {

                        // Format time as HH:mm
                        String timeFormatted = String.format("%02d:%02d", hourOfDay, minute1);
                        etTime.setText(timeFormatted);

                    }, hour, minute, true);

            timePickerDialog.show();
        });


        btnAddTask.setOnClickListener(v -> {
            if (isEditMode) {
                updateTask();
            } else {
                addTask();
            }
        });

        return view;
    }

    private void addTask() {

        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        Tasks task = new Tasks("", time, title, description);

        db.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .add(task)
                .addOnSuccessListener(docRef -> {

                    // Save document id inside task
                    docRef.update("id", docRef.getId());

                    Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();

                    etTitle.setText("");
                    etDescription.setText("");
                    etTime.setText("");

                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateTask() {

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(taskId)
                .update(
                        "title", title,
                        "description", description,
                        "time", time
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = getDialog().getWindow().getAttributes().height;

            getDialog().getWindow().setLayout(width, height);
        }
    }
}