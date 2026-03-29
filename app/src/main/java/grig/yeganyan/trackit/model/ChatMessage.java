package grig.yeganyan.trackit.model;

public class ChatMessage {
    // Constants to avoid typos later
    public static final String ROLE_USER = "user";
    public static final String ROLE_AI = "model";

    private String text;
    private String role; // Stores either "user" or "model"
    private long timestamp;

    // Required empty constructor for Firebase (if you decide to save chats later)
    public ChatMessage() {
    }

    public ChatMessage(String text, String role) {
        this.text = text;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }

    // --- GETTERS ---
    // These are used by the Adapter to display the data

    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // --- SETTERS ---

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