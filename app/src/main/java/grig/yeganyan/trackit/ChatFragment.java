package grig.yeganyan.trackit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import grig.yeganyan.trackit.BuildConfig;

import grig.yeganyan.trackit.model.ChatMessage;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private MaterialButton sendBtn;
    private ProgressBar progressBar;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private GenerativeModelFutures model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);


        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY

        );
        model = GenerativeModelFutures.from(gm);

        recyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendBtn = view.findViewById(R.id.sendBtn);
        progressBar = view.findViewById(R.id.chatProgressBar);

        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            // Check if the user pressed "Send" on the keyboard
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage(); // Calls your existing send function
                return true;
            }
            return false;
        });

        ChipGroup suggestionGroup = view.findViewById(R.id.suggestionChipGroup);

        for (int i = 0; i < suggestionGroup.getChildCount(); i++) {
            View child = suggestionGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {

                    String prompt = chip.getText().toString();

                    messageInput.setText(prompt);


                    sendMessage();

                });
            }
        }


        sendBtn.setOnClickListener(v -> sendMessage());
        return view;
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        addMessageToUI(text, ChatMessage.ROLE_USER);
        messageInput.setText("");
        progressBar.setVisibility(View.VISIBLE);

        Content content = new Content.Builder()
                .addText("You are a habit coach. Answer this: " + text)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            String aiResponse = result.getText();
                            if (aiResponse != null && !aiResponse.isEmpty()) {
                                addMessageToUI(aiResponse, ChatMessage.ROLE_AI);
                            } else {
                                addMessageToUI("You are the official AI Habit Coach for 'TrackIt'. " +
                                        "Encourage users to use our app features to break bad habits!"+"Speak prity and short speach", ChatMessage.ROLE_AI);
                            }
                        } catch (Exception e) {
                            addMessageToUI("Error: " + e.getMessage(), ChatMessage.ROLE_AI);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        // 404 FIX: If 404 persists, the API key might be locked to a different model series.
                        addMessageToUI("Error " + t.getMessage(), ChatMessage.ROLE_AI);
                    });
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void addMessageToUI(String text, String role) {
        messageList.add(new ChatMessage(text, role));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
}
