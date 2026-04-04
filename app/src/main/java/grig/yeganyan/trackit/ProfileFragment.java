package grig.yeganyan.trackit;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.Login;
import grig.yeganyan.trackit.model.User;

public class ProfileFragment extends Fragment {

    TextView profileName, profileEmail;
    Button logoutButton;
    SwitchMaterial themeSwitch;
    SharedPreferences prefs;
    TextView profileAvatar;
    MaterialButton btnCoachTone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        Button deleteButton = view.findViewById(R.id.btndelete);
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {


            profileEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        profileName.setText(username);
                    }
                });

        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String avatar = document.getString("avatar");
                            if (avatar != null) {
                                profileAvatar.setText(avatar);
                            }
                        }
                    });
        }

        btnCoachTone = view.findViewById(R.id.btnCoachTone);
        themeSwitch = view.findViewById(R.id.themeSwitch);

        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        boolean darkMode = prefs.getBoolean("darkMode", false);
        themeSwitch.setChecked(darkMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("darkMode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });



        profileAvatar = view.findViewById(R.id.profileAvatar);
        profileAvatar.setOnClickListener(v -> openEmojiSelectorDynamic()


        );

        MaterialButton btnCoachTone = view.findViewById(R.id.btnCoachTone);

        String savedToneName = getSavedCoachTone();
        CoachTone currentTone = CoachTone.valueOf(savedToneName);
        btnCoachTone.setText("Coach Personality: " + currentTone.displayName);

        // Set the click listener
        btnCoachTone.setOnClickListener(v -> showCoachToneDialog());

        logoutButton = view.findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("registered", false).apply(); // reset registration status
        prefs.edit().putString("userId", "").apply(); // clear saved userId

        Intent intent = new Intent(getActivity(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("⚠️ Warning! Deleting your account is permanent.\n\n" +
                        "All your habits, tasks, and personal data will be lost forever.\n" +
                        "You will not be able to recover your account.\n\n" +
                        "Are you sure you want to continue?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) return;

        String userId = user.getUid();


        db.collection("users").document(userId)
                .delete()
                .addOnCompleteListener(task -> {
                    // 2. Delete Firebase Authentication account
                    user.delete()
                            .addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "Account deleted permanently", Toast.LENGTH_LONG).show();
                                    // Redirect to login screen or finish activity
                                    startActivity(new Intent(getContext(), Login.class));
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + deleteTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error deleting data: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void openEmojiSelectorDynamic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(6);
        grid.setRowCount(GridLayout.UNDEFINED);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setUseDefaultMargins(true);
        grid.setPadding(16, 16, 16, 16);

        String[] emojis = {
                // Smileys & People
                "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕",

                // Animals & Nature
                "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐽","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧","🐦","🐤","🐣","🐥","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🐛","🦋","🐌","🐞","🐜","🪲","🪳","🦟","🦗","🕷️","🦂","🐢","🐍","🦎","🐙","🦑","🦐","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈","🐊","🐅","🐆","🦓","🦍","🦧","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🦬","🐃","🐂","🐄","🐎","🐖","🐏","🐑","🐐","🦌","🐕","🐩","🦮","🐕‍🦺","🐈","🐓","🦃","🕊️","🦢","🦜","🦚","🦩","🐇","🦝","🦨","🦡","🐁","🐀","🐿️","🦔",

                // Food & Drink
                "🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑","🌽","🥕","🫛","🥔","🍠","🥐","🥯","🍞","🥖","🥨","🥞","🧇","🧀","🍖","🍗","🥩","🥓","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🥙","🧆","🥚","🍳","🥘","🍲","🥣","🥗","🍿","🧈","🧂","🥫","🍱","🍣","🍛","🍚","🍙","🍘","🥠","🥟","🦪","🍤","🍙","🍢","🍡","🍧","🍨","🍦","🥧","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪","🥛","🍼","☕","🍵","🍶","🍺","🍻","🥂","🍷","🥃","🍸","🍹","🧉","🧃","🧊","🥤","🧋",

                // Activities & Objects
                "⚽","🏀","🏈","⚾","🥎","🏐","🏉","🎾","🥏","🎳","🏏","🏑","🏒","🥍","🏓","🏸","🥊","🥋","🥅","⛳","🪁","🏹","🎣","🤿","🥌","🎿","⛷️","🏂","🪂","🏋️","🤼","🤸","⛹️","🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚴","🚵","🛹","🛷","🥇","🥈","🥉","🏆","🎖️","🏅","🎗️","🎫","🎟️","🎪","🤹","🎭","🎨","🖌️","🎬","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🪗","🎸","🪕","🎻","📯","🎙️","🎚️","🎛️","🎲","🧩","🧸","🪀","🪁"
        };

        // Scrollable container
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(grid);
        builder.setView(scrollView);

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        // Create dialog first
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true); // allow tap outside to dismiss
        dialog.show();

        // Add emojis AFTER dialog creation
        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(42); // balanced, readable size
            tv.setGravity(Gravity.CENTER);

            int size = (int) (64 * getResources().getDisplayMetrics().density); // 64dp
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);

            tv.setOnClickListener(v -> {
                profileAvatar.setText(emoji); // update UI

                // Save selected emoji to Firebase
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(user.getUid())
                            .update("avatar", emoji) // "avatar" field in Firestore
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show();
                            });
                }

                dialog.dismiss(); // close emoji selector
            });

            grid.addView(tv);
        }
    }

    private void showCoachToneDialog() {
        // 1. Prepare the options for the dialog
        CoachTone[] tones = CoachTone.values();
        String[] toneNames = new String[tones.length];
        for (int i = 0; i < tones.length; i++) {
            toneNames[i] = tones[i].displayName;
        }

        // 2. Build the Material Dialog
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle("Choose your Coach")
                .setItems(toneNames, (dialog, which) -> {

                    CoachTone selectedTone = tones[which];


                    saveCoachPreference(selectedTone.name());


                    btnCoachTone.setText("Coach Personality: " + selectedTone.displayName);


                    Toast.makeText(getContext(), "Coach set to " + selectedTone.displayName, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void saveCoachPreference(String toneName) {
        SharedPreferences prefs = getActivity().getSharedPreferences("TrackItPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("COACH_TONE", toneName).apply();
    }

    private String getSavedCoachTone() {
        SharedPreferences prefs = getActivity().getSharedPreferences("TrackItPrefs", Context.MODE_PRIVATE);
        return prefs.getString("COACH_TONE", "DISCIPLINED"); // Default to Disciplined
    }
}