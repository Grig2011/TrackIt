package grig.yeganyan.trackit.model;

public class Habit {

    public String id;
    public String emoji;
    public String title;
    public String description;
    public String color;
    public String type;
    public double goal;
    public String unit;
    public String days;
    public int Streak = 0;

    public Habit() {}

    public Habit(String id, String emoji, String title, String description,
                 String color, String type, double goal, String unit, String days,int streak) {
        this.id = id;
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.color = color;
        this.type = type;
        this.goal = goal;
        this.unit = unit;
        this.days = days;
        this.Streak = streak;
    }
}
