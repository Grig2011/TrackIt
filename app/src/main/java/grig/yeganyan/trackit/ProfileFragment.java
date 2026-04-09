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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
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

    private boolean isUserAction = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        Spinner spinner = view.findViewById(R.id.langSpinner);


        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "en");


        if (currentLang.equals("hy")) spinner.setSelection(1);
        else if (currentLang.equals("ru")) spinner.setSelection(2);
        else spinner.setSelection(0);

        spinner.setOnTouchListener((v, event) -> {
            isUserAction = true;
            return false;
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserAction) return;

                String selectedLang;
                switch (position) {
                    case 1: selectedLang = "hy"; break;
                    case 2: selectedLang = "ru"; break;
                    default: selectedLang = "en"; break;
                }

                changeLanguage(selectedLang);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        SharedPreferences finalPrefs = prefs;
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            SharedPreferences.Editor editor = finalPrefs.edit();
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


        btnCoachTone.setOnClickListener(v -> showCoachToneDialog());

        logoutButton = view.findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("registered", false).apply();
        prefs.edit().putString("userId", "").apply();

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

                    user.delete()
                            .addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "Account deleted permanently", Toast.LENGTH_LONG).show();

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


        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(grid);
        builder.setView(scrollView);

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());


        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(42);
            tv.setGravity(Gravity.CENTER);

            int size = (int) (64 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);

            tv.setOnClickListener(v -> {
                profileAvatar.setText(emoji);


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(user.getUid())
                            .update("avatar", emoji)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show();
                            });
                }

                dialog.dismiss();
            });

            grid.addView(tv);
        }
    }

    private void showCoachToneDialog() {

        CoachTone[] tones = CoachTone.values();
        String[] toneNames = new String[tones.length];
        for (int i = 0; i < tones.length; i++) {
            toneNames[i] = tones[i].displayName;
        }


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
        return prefs.getString("COACH_TONE", "DISCIPLINED");
    }

    private void changeLanguage(String langCode) {

        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        prefs.edit().putString("My_Lang", langCode).apply();

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}