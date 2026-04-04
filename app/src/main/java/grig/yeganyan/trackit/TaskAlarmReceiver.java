package grig.yeganyan.trackit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class TaskAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("TASK_TITLE");
        String taskId = intent.getStringExtra("TASK_ID");
        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }


        Intent yesIntent = new Intent(context, NotificationActionReceiver.class);
        yesIntent.setAction("ACTION_YES");
        yesIntent.putExtra("TASK_ID", taskId);
        yesIntent.putExtra("NOTIFICATION_ID", requestCode);
        PendingIntent yesPi = PendingIntent.getBroadcast(context, requestCode + 1, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Intent noIntent = new Intent(context, NotificationActionReceiver.class);
        noIntent.setAction("ACTION_NO");
        noIntent.putExtra("TASK_TITLE", taskTitle);
        noIntent.putExtra("TASK_ID", taskId);
        noIntent.putExtra("NOTIFICATION_ID", requestCode);
        PendingIntent noPi = PendingIntent.getBroadcast(context, requestCode + 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle(taskTitle)
                .setContentText("Is this finished?")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0, "Yes", yesPi)
                .addAction(0, "No", noPi);

        manager.notify(requestCode, builder.build());
    }
}