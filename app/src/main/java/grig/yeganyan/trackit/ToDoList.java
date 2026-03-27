package grig.yeganyan.trackit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

    private View openedCard = null;
    private ImageButton openedEdit = null;
    private ImageButton openedDelete = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

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
                            task.setId(doc.getId());
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

    class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private List<Tasks> tasks;

        TaskAdapter(List<Tasks> tasks) {
            this.tasks = tasks;
        }

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
            holder.tvTime.setText(task.getTime());

            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);

            setupDragSwipe(holder.itemView.findViewById(R.id.cardContent), holder, task);

            holder.btnDelete.setOnClickListener(v -> deleteTask(task));
            holder.btnEdit.setOnClickListener(v -> editTask(task));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvTime;
            ImageButton btnDelete, btnEdit;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnDelete = itemView.findViewById(R.id.btnDeleteTask);
                btnEdit = itemView.findViewById(R.id.btnEditTask);
            }
        }

        private void setupDragSwipe(View cardContent, TaskViewHolder holder, Tasks task) {

            cardContent.setOnTouchListener(new View.OnTouchListener() {

                float startX;
                float translation = 0;
                float swipeDistance = 0;
                final float TOUCH_SLOP = 12f;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (swipeDistance == 0)
                        swipeDistance = v.getWidth() * 0.35f;

                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX() - translation;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float delta = event.getRawX() - startX;
                            translation = Math.max(0, Math.min(delta, swipeDistance));
                            v.setTranslationX(translation);

                            float alpha = translation / swipeDistance;
                            holder.btnEdit.setVisibility(View.VISIBLE);
                            holder.btnDelete.setVisibility(View.VISIBLE);
                            holder.btnEdit.setAlpha(alpha);
                            holder.btnDelete.setAlpha(alpha);
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:

                            if (translation > swipeDistance * 0.25f) {
                                // Open buttons
                                if (openedCard != null && openedCard != v) closeCard(openedCard, openedEdit, openedDelete);
                                v.animate().translationX(swipeDistance).setDuration(180).start();
                                holder.btnEdit.setAlpha(1f);
                                holder.btnDelete.setAlpha(1f);
                                openedCard = v;
                                openedEdit = holder.btnEdit;
                                openedDelete = holder.btnDelete;
                                translation = swipeDistance;
                            } else {
                                closeCard(v, holder.btnEdit, holder.btnDelete);
                                translation = 0;
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    private void closeCard(View card, ImageButton edit, ImageButton delete) {
        card.animate().translationX(0).setDuration(140).start();
        edit.animate().alpha(0f).setDuration(120).withEndAction(() -> edit.setVisibility(View.GONE)).start();
        delete.animate().alpha(0f).setDuration(120).withEndAction(() -> delete.setVisibility(View.GONE)).start();
        if (card == openedCard) {
            openedCard = null;
            openedEdit = null;
            openedDelete = null;
        }
    }

    private void deleteTask(Tasks task) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Do you really want to remove this task? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    taskList.remove(task);
                    adapter.notifyDataSetChanged();
                    db.collection("users").document(currentUserId)
                            .collection("tasks").document(task.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Task removed", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Could not delete task", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void editTask(Tasks task) {
        AddTask addTaskFragment = new AddTask();
        Bundle args = new Bundle();
        args.putString("userId", currentUserId);
        args.putString("taskId", task.getId());
        args.putString("title", task.getTitle());
        args.putString("description", task.getDescription());
        args.putString("time", task.getTime());
        addTaskFragment.setArguments(args);
        addTaskFragment.show(getParentFragmentManager(), "edit_task");
    }
}