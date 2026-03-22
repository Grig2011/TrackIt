package grig.yeganyan.trackit;

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
        }

        btnAddTask.setOnClickListener(v -> addTask());

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
                    docRef.update("id", docRef.getId());
                    Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etDescription.setText("");
                    etTime.setText("");
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}