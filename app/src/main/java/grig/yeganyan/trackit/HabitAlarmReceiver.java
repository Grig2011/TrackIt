package grig.yeganyan.trackit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class HabitAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String habitName = intent.getStringExtra("HABIT_NAME");
        String habitId = intent.getStringExtra("HABIT_ID");
        int requestCode = intent.getIntExtra("REQUEST_CODE", (int) System.currentTimeMillis());

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "habit_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Habit Reminders", NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) manager.createNotificationChannel(channel);
        }


        Intent yesIntent = new Intent(context, HabitNotificationActionReciver.class);
        yesIntent.setAction("ACTION_YES");
        yesIntent.putExtra("HABIT_ID", habitId);
        yesIntent.putExtra("NOTIFICATION_ID", requestCode);
        PendingIntent yesPi = PendingIntent.getBroadcast(context, requestCode + 100, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Intent noIntent = new Intent(context, HabitNotificationActionReciver.class);
        noIntent.setAction("ACTION_NO");
        noIntent.putExtra("HABIT_TITLE", habitName);
        noIntent.putExtra("HABIT_ID", habitId);
        noIntent.putExtra("NOTIFICATION_ID", requestCode);
        PendingIntent noPi = PendingIntent.getBroadcast(context, requestCode + 200, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // Or your habit icon
                .setContentTitle("Habit Reminder")
                .setContentText("Did you: " + habitName + "?")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0, "Yes", yesPi)
                .addAction(0, "No", noPi);

        if (manager != null) {
            manager.notify(requestCode, builder.build());
        }
    }
}