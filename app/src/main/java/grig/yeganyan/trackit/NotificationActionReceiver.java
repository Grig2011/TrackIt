package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String taskId = intent.getStringExtra("TASK_ID");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        if ("ACTION_YES".equals(action)) {

            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("userId", null);
            if (userId != null && taskId != null) {
                FirebaseFirestore.getInstance().collection("users").document(userId)
                        .collection("tasks").document(taskId).delete();
            }
        } else if ("ACTION_NO".equals(action)) {

            String title = intent.getStringExtra("TASK_TITLE");
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent nextIntent = new Intent(context, TaskAlarmReceiver.class);
            nextIntent.putExtra("TASK_TITLE", title);
            nextIntent.putExtra("TASK_ID", taskId);
            nextIntent.putExtra("REQUEST_CODE", notificationId);

            PendingIntent pi = PendingIntent.getBroadcast(context, notificationId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            long tenMinutesInMs = 10 * 60 * 1000;
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + tenMinutesInMs, pi);
            }
        }
    }
}