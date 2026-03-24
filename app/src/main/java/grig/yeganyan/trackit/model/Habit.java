package grig.yeganyan.trackit.model;

public class Habit {

    private String id;
    private String emoji;
    private String title;
    private String description;
    private String color;
    private String type;
    private double goal;
    private String unit;
    private String days;
    private int streak = 0;

    public Habit() {}

    public Habit(String id, String emoji, String title, String description,
                 String color, String type, double goal, String unit, String days, int streak) {
        this.id = id;
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.color = color;
        this.type = type;
        this.goal = goal;
        this.unit = unit;
        this.days = days;
        this.streak = streak;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }
}