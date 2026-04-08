package grig.yeganyan.trackit.Services;

import java.util.List;

import grig.yeganyan.trackit.model.Habit;



public interface HabitCallback {
    void onCallback(List<Habit> habitList);
}