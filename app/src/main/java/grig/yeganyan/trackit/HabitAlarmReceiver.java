package grig.yeganyan.trackit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import grig.yeganyan.trackit.NotificationHelper;

public class HabitAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String habitName = intent.getStringExtra("HABIT_NAME");
        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);

        if (habitName == null) habitName = "Habit";

        // 1. Show the Notification (Corrected style: Just the name)
        // We pass empty string for message to keep it clean
        NotificationHelper.showNotification(context, habitName, "", requestCode);

        // 2. Reschedule for tomorrow at the EXACT same time
        scheduleNextDay(context, habitName, requestCode);
    }

    private void scheduleNextDay(Context context, String habitName, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(context, HabitAlarmReceiver.class);
        newIntent.putExtra("HABIT_NAME", habitName);
        newIntent.putExtra("REQUEST_CODE", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Move to exactly 24 hours from now
        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DAY_OF_YEAR, 1);

        if (alarmManager != null) {
            // setExactAndAllowWhileIdle is required for daily reliability on Oreo+
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextDay.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}