package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
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

            holder.cardContent.setTranslationX(0);
            holder.buttonContainer.setVisibility(View.GONE);
            holder.buttonContainer.setAlpha(0f);
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);

            setupDragSwipe(holder.cardContent, holder.buttonContainer, holder.btnEdit, holder.btnDelete, task);

            holder.btnDelete.setOnClickListener(v -> deleteTask(task));
            holder.btnEdit.setOnClickListener(v -> editTask(task));
        }

        private void setupDragSwipe(View cardContent, View buttonContainer, ImageButton editButton, ImageButton deleteButton, Tasks task) {
            cardContent.setOnTouchListener(new View.OnTouchListener() {
                float downX;
                float translation = 0;
                float swipeDistance = 0;
                boolean dragging = false;
                final float TOUCH_SLOP = 12f;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (swipeDistance == 0) {
                        swipeDistance = v.getWidth() * 0.35f;
                    }

                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = event.getRawX() - translation;
                            dragging = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float delta = event.getRawX() - downX;
                            if (!dragging && Math.abs(delta) > TOUCH_SLOP) {
                                dragging = true;
                            }
                            if (!dragging) return true;

                            float limited = Math.max(0, Math.min(delta, swipeDistance));
                            float progress = limited / swipeDistance;
                            float resistance = (float) (1 - Math.pow(progress, 2));
                            translation = limited * resistance + limited * (1 - resistance);

                            v.setTranslationX(translation);
                            float alpha = translation / swipeDistance;

                            buttonContainer.setVisibility(View.VISIBLE);
                            editButton.setVisibility(View.VISIBLE);
                            deleteButton.setVisibility(View.VISIBLE);

                            buttonContainer.setAlpha(alpha);
                            editButton.setAlpha(alpha);
                            deleteButton.setAlpha(alpha);
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (!dragging && Math.abs(event.getRawX() - (downX + translation)) < TOUCH_SLOP) {
                                editTask(task);
                                v.performClick();
                                return true;
                            }

                            float velocity = event.getRawX() - downX;
                            boolean open = translation > swipeDistance * 0.25f || velocity > 1200;

                            if (open) {
                                if (openedCard != null && openedCard != v) {
                                    closeCard(openedCard, openedEdit != null ? (View) openedEdit.getParent() : null, openedEdit, openedDelete);
                                }
                                v.animate().translationX(swipeDistance).setDuration(140).start();
                                buttonContainer.animate().alpha(1f).setDuration(100).start();
                                editButton.setAlpha(1f);
                                deleteButton.setAlpha(1f);

                                openedCard = v;
                                openedEdit = editButton;
                                openedDelete = deleteButton;
                                translation = swipeDistance;
                            } else {
                                closeCard(v, buttonContainer, editButton, deleteButton);
                                translation = 0;
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvTime;
            ImageButton btnDelete, btnEdit;
            View cardContent, buttonContainer;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnDelete = itemView.findViewById(R.id.btnDeleteTask);
                btnEdit = itemView.findViewById(R.id.btnEditTask);
                cardContent = itemView.findViewById(R.id.cardContent);
                buttonContainer = itemView.findViewById(R.id.buttonContainer);
            }
        }
    }

    private void closeCard(View card, View container, ImageButton edit, ImageButton delete) {
        if (card != null) {
            card.animate().translationX(0).setDuration(140).start();
        }
        if (container != null) {
            container.animate().alpha(0f).setDuration(100).withEndAction(() -> container.setVisibility(View.GONE)).start();
        }
        if (edit != null) {
            edit.animate().alpha(0f).setDuration(100).withEndAction(() -> edit.setVisibility(View.GONE)).start();
        }
        if (delete != null) {
            delete.animate().alpha(0f).setDuration(100).withEndAction(() -> delete.setVisibility(View.GONE)).start();
        }
        if (card == openedCard) {
            openedCard = null;
            openedEdit = null;
            openedDelete = null;
        }
    }

    private void deleteTask(Tasks task) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Do you really want to remove this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users").document(currentUserId)
                            .collection("tasks").document(task.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                cancelAlarm(task.getId());
                                Toast.makeText(getContext(), "Task removed", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
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

    private void cancelAlarm(String taskId) {
        if (taskId == null) return;
        Intent intent = new Intent(getContext(), TaskAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), taskId.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
    }
}