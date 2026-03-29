package grig.yeganyan.trackit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import grig.yeganyan.trackit.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> messages;
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    public ChatAdapter(List<ChatMessage> messages) { this.messages = messages; }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getRole().equals(ChatMessage.ROLE_USER) ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_USER) ? R.layout.item_chat_user : R.layout.item_chat_ai;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ChatViewHolder) holder).chatText.setText(messages.get(position).getText());
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatText;
        ChatViewHolder(View v) { super(v); chatText = v.findViewById(R.id.chatText); }
    }
}