package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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

import java.util.Calendar;

import grig.yeganyan.trackit.model.Tasks;

public class AddTask extends DialogFragment {

    private EditText etTitle, etDescription, etTime;
    private Button btnAddTask;
    private FirebaseFirestore db;
    private String currentUserId;
    private String taskId;
    private boolean isEditMode = false;
    private Calendar calendar = Calendar.getInstance();

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

        if (getArguments() != null) {
            currentUserId = getArguments().getString("userId");
            taskId = getArguments().getString("taskId");

            if (taskId != null) {
                isEditMode = true;
                etTitle.setText(getArguments().getString("title"));
                etDescription.setText(getArguments().getString("description"));
                String time = getArguments().getString("time");
                etTime.setText(time);

                if (time != null && time.contains(":")) {
                    String[] parts = time.split(":");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    calendar.set(Calendar.SECOND, 0);
                }
                btnAddTask.setText("Update Task");
            }
        }

        etTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute1) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute1);
                calendar.set(Calendar.SECOND, 0);
                etTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
            }, hour, minute, true).show();
        });

        btnAddTask.setOnClickListener(v -> {
            if (isEditMode) updateTask();
            else addTask();
        });

        return view;
    }

    private void addTask() {
        if (currentUserId == null) {
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
                    String newId = docRef.getId();
                    docRef.update("id", newId);
                    if (!time.isEmpty()) scheduleNotification(title, newId);
                    Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
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
                .update("title", title, "description", description, "time", time)
                .addOnSuccessListener(aVoid -> {
                    if (!time.isEmpty()) scheduleNotification(title, taskId);
                    Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void scheduleNotification(String title, String id) {
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), TaskAlarmReceiver.class);
        intent.putExtra("TASK_TITLE", title);
        intent.putExtra("TASK_ID", id); // ADD THIS LINE
        int requestCode = id.hashCode();
        intent.putExtra("REQUEST_CODE", requestCode);

        PendingIntent pi = PendingIntent.getBroadcast(getContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            calendar.add(Calendar.DATE, 1);
        }

        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}