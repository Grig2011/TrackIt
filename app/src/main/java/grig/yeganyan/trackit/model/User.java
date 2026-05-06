package grig.yeganyan.trackit.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String avatar;

    public int bestStreak;
    public String bestStreakHabitName;
    public User() {}


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;

    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBestStreakHabitName(){return bestStreakHabitName; }
    public void setBestStreakHabitName(String bestStreakHabitName){this.bestStreakHabitName=bestStreakHabitName; }

    public int getBestStreak(){return bestStreak; }
    public void setBestStreak(int bestStreak){this.bestStreak = bestStreak;}

}