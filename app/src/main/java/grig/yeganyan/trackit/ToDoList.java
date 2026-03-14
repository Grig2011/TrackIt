package grig.yeganyan.trackit;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ToDoList extends Fragment {

    ListView listView;
    Button btnAdd;

    ArrayList<String> tasks;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;

    public ToDoList() {

    }

    public static ToDoList newInstance(String param1, String param2) {
        ToDoList fragment = new ToDoList();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        listView = view.findViewById(R.id.listView);
        btnAdd = view.findViewById(R.id.btnAdd);

        db = FirebaseFirestore.getInstance();

        tasks = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                tasks
        );

        listView.setAdapter(adapter);

        loadTasks();

        btnAdd.setOnClickListener(v -> showAddDialog());



        return view;
    }

    private void showAddDialog(){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.fragment_add_task, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        EditText editDescription = view.findViewById(R.id.editDescription);
        EditText editTime = view.findViewById(R.id.editTime);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Task")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {

                    String title = editTitle.getText().toString();
                    String description = editDescription.getText().toString();
                    String time = editTime.getText().toString();

                    if(!title.isEmpty()){

                        Map<String,Object> task = new HashMap<>();
                        task.put("title", title);
                        task.put("description", description);
                        task.put("time", time);

                        db.collection("tasks")
                                .add(task)
                                .addOnSuccessListener(documentReference -> {

                                    tasks.add(title + " - " + time);
                                    adapter.notifyDataSetChanged();

                                });
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadTasks(){

        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    tasks.clear();

                    for(DocumentSnapshot doc : queryDocumentSnapshots){

                        String title = doc.getString("title");
                        String time = doc.getString("time");

                        if(title != null){
                            tasks.add(title + " - " + time);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
