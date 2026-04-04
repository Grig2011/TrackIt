package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class HabitNotificationActionReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String habitId = intent.getStringExtra("HABIT_ID"); // Changed from taskId to habitId
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId);
        }

        if ("ACTION_YES".equals(action)) {
            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("userId", null);

            if (userId != null && habitId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("habits")
                        .document(habitId)
                        .update("streak", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> {
                            android.util.Log.d("HABIT_UPDATE", "Success! Streak increased for: " + habitId);
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("HABIT_UPDATE", "Failed: " + e.getMessage());
                        });
            }
        } else if ("ACTION_NO".equals(action)) {
            String title = intent.getStringExtra("HABIT_TITLE");
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent nextIntent = new Intent(context, HabitAlarmReceiver.class);
            nextIntent.putExtra("HABIT_NAME", title);
            nextIntent.putExtra("HABIT_ID", habitId);

            PendingIntent pi = PendingIntent.getBroadcast(context, notificationId, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            long tenMinutesInMs = 10 * 60 * 1000;
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + tenMinutesInMs, pi);
            }
        }
    }
}