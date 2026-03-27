package grig.yeganyan.trackit.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String emoji;


    public User() {}


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.emoji = "👱";
    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}