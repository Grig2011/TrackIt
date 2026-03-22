package grig.yeganyan.trackit.model;

public class Tasks {
    private String id;
    private String time;
    private String title;
    private String description;

    public Tasks() {} // required by Firestore

    public Tasks(String id, String time, String title, String description) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}