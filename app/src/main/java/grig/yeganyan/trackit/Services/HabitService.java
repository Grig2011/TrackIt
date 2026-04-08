package grig.yeganyan.trackit.Services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import grig.yeganyan.trackit.model.Habit;

public class HabitService {
    public void getUsersAllHabits(String userId, HabitCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("users")
                .document(userId)
                .collection("habits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Habit> habitList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Habit habit = document.toObject(Habit.class);
                            habitList.add(habit);
                        }

                        callback.onCallback(habitList);
                    } else {
                        Log.e("FirestoreError", "Error getting habits: ", task.getException());
                    }
                });
    }
    public static String formatHabitsToString(List<Habit> habitList) {
        if (habitList == null || habitList.isEmpty()) {
            return "No habits found";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < habitList.size(); i++) {
            Habit h = habitList.get(i);


            sb.append(h.getTitle()).append(",")
                    .append(h.getDescription()).append(",")
                    .append("Streak").append(h.getStreak());


            if (i < habitList.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }


}
