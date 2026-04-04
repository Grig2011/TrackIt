package grig.yeganyan.trackit;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import grig.yeganyan.trackit.AddHabit;


public class NotificationHelper {

    private static final String CHANNEL_ID = "habit_notify_channel";
    private static final String CHANNEL_NAME = "Habit Reminders";
    private static final String CHANNEL_DESC = "Daily habit notifications";

    public static void showNotification(Context context, String title, String message, int reqCode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "habit_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // For Android 7.1 and lower
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // TRiggers default sound/vibrate
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(reqCode, builder.build());
        }
    }

}