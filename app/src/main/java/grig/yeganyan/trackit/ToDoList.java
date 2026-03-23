package grig.yeganyan.trackit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import grig.yeganyan.trackit.model.Tasks;

public class ToDoList extends Fragment {

    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private List<Tasks> taskList;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get userId from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Setup FAB
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> openAddTaskDialog());

        db = FirebaseFirestore.getInstance();
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);
        rvTasks.setAdapter(adapter);

        loadTasks();

        return view;
    }

    private void loadTasks() {
        listenerRegistration = db.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .orderBy("time")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    taskList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Tasks task = doc.toObject(Tasks.class);
                            taskList.add(task);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void openAddTaskDialog() {
        AddTask addTaskFragment = new AddTask();
        Bundle args = new Bundle();
        args.putString("userId", currentUserId);
        addTaskFragment.setArguments(args);
        addTaskFragment.show(getParentFragmentManager(), "add_task");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    // --- Adapter ---
    class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private List<Tasks> tasks;
        TaskAdapter(List<Tasks> tasks) { this.tasks = tasks; }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Tasks task = tasks.get(position);
            holder.tvTitle.setText(task.getTitle());
            holder.tvDescription.setText(task.getDescription());
            holder.tvTime.setText(task.getTime().toString());

            holder.btnDelete.setOnClickListener(v -> deleteTask(position));
        }

        @Override
        public int getItemCount() { return tasks.size(); }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvTime;
            ImageButton btnDelete;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnDelete = itemView.findViewById(R.id.btnDeleteTask);
            }
        }
    }

    private void deleteTask(int position) {
        String taskId = taskList.get(position).getId();
        db.collection("users")
                .document(currentUserId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}