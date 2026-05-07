package grig.yeganyan.trackit;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import grig.yeganyan.trackit.model.User;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<User> userList;

    public LeaderboardAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);


        int rank = position + 1;
        holder.rankText.setText(String.valueOf(rank));


        if (position == 0) {
            holder.rankText.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_streak)));
            holder.rankText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.habit_bg_color));
        } else if (position < 3) {
            holder.rankText.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_color)));
            holder.rankText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.habit_bg_color));
        } else {
            holder.rankText.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.track_gray)));
            holder.rankText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }


        holder.userName.setText(user.getUsername() != null ? user.getUsername() : "Anonymous");


        String emoji = user.getAvatar();
        holder.emojiAvatar.setText(emoji != null && !emoji.isEmpty() ? emoji : "👤");


        String habitName = user.getBestStreakHabitName();
        holder.habitNameText.setText(habitName != null ? habitName : "New Member");
        holder.streakValue.setText(String.valueOf(user.getBestStreak()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, userName, habitNameText, streakValue, emojiAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            userName = itemView.findViewById(R.id.userName);
            habitNameText = itemView.findViewById(R.id.habitNameText);
            streakValue = itemView.findViewById(R.id.streakValue);
            emojiAvatar = itemView.findViewById(R.id.emojiAvatar);
        }
    }
}