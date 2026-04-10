package grig.yeganyan.trackit.model;

public class ChatMessage {

    public static final String ROLE_USER = "user";
    public static final String ROLE_AI = "model";

    private String text;
    private String role;
    private long timestamp;


    public ChatMessage() {
    }

    public ChatMessage(String text, String role) {
        this.text = text;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }



    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public long getTimestamp() {
        return timestamp;
    }



    public void setText(String text) {
        this.text = text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}