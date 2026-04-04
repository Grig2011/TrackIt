package grig.yeganyan.trackit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


        SharedPreferences prefs = requireActivity().getSharedPreferences("TrackItPrefs", Context.MODE_PRIVATE);
        String savedToneName = prefs.getString("COACH_TONE", "DISCIPLINED");

        String personalityInstruction;
        try {
            personalityInstruction = CoachTone.valueOf(savedToneName).systemInstruction;
        } catch (Exception e) {
            personalityInstruction = CoachTone.DISCIPLINED.systemInstruction;
        }


        String finalSystemPrompt = "You are the official TrackIt app coach. " + personalityInstruction +
                " Keep responses brief (2-3 sentences) and always end with a single follow-up question.";


        Content systemInstruction = new Content("system",
                Collections.singletonList(new TextPart(finalSystemPrompt)));


        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY,
                new GenerationConfig.Builder().build(),
                null,
                new RequestOptions(),
                null,
                null,
                systemInstruction
        );
        model = GenerativeModelFutures.from(gm);


        recyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendBtn = view.findViewById(R.id.sendBtn);
        progressBar = view.findViewById(R.id.chatProgressBar);

        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        ChipGroup suggestionGroup = view.findViewById(R.id.suggestionChipGroup);
        if (suggestionGroup != null) {
            for (int i = 0; i < suggestionGroup.getChildCount(); i++) {
                View child = suggestionGroup.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setOnClickListener(v -> {
                        messageInput.setText(((Chip) v).getText().toString());
                        sendMessage();
                    });
                }
            }
        }

        sendBtn.setOnClickListener(v -> sendMessage());
        return view;
    }

    private void sendMessage() {
        String userText = messageInput.getText().toString().trim();
        if (userText.isEmpty()) return;

        addMessageToUI(userText, ChatMessage.ROLE_USER);
        messageInput.setText("");
        progressBar.setVisibility(View.VISIBLE);


        Content userContent = new Content("user",
                Collections.singletonList(new TextPart(userText)));

        ListenableFuture<GenerateContentResponse> response = model.generateContent(userContent);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        String aiResponse = result.getText();
                        if (aiResponse != null && !aiResponse.isEmpty()) {
                            addMessageToUI(aiResponse, ChatMessage.ROLE_AI);
                        } else {
                            addMessageToUI("I'm here to help you stay on track!", ChatMessage.ROLE_AI);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        addMessageToUI("Error: " + t.getMessage(), ChatMessage.ROLE_AI);
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