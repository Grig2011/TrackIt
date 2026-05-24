package grig.yeganyan.trackit;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import grig.yeganyan.trackit.Login;
import grig.yeganyan.trackit.model.User;

public class ProfileFragment extends Fragment {

    TextView profileName, profileEmail;
    Button logoutButton,btnSettings;
    SwitchMaterial themeSwitch;
    SharedPreferences prefs;
    TextView profileAvatar;
    MaterialButton btnCoachTone;
    Button btnTop50;

    TextView profileBio;
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

        TextView profileBio = view.findViewById(R.id.profileBio);
        setDailyMotivation(profileBio);

        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        Button deleteButton = view.findViewById(R.id.btndelete);
        deleteButton.setOnClickListener(v -> showPasswordConfirmationDialog());

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

        btnTop50 = view.findViewById(R.id.Top50Link);
        btnTop50.setOnClickListener(v->{
            Intent i = new Intent(getActivity(),LeaderboardActivity.class);
            startActivity(i);

        });
        MaterialButton btnInstructionGuide = view.findViewById(R.id.btnInstructionGuide);
        btnInstructionGuide.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), instructionActivity.class);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            }
        });

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

        String prefix = getString(R.string.coach_prefix);


        String toneName = getString(currentTone.nameResId);


        btnCoachTone.setText(prefix + toneName);


        btnCoachTone.setOnClickListener(v -> showCoachToneDialog());

        logoutButton = view.findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> ShowPasswordForSettings());




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
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_acc_title))
                .setMessage(getString(R.string.delete_acc_msg))


                .setNegativeButton(getString(R.string.delete_acc_cancel), (dialog, which) -> {
                    dialog.dismiss();
                })

                .setPositiveButton(getString(R.string.delete_acc_confirm), (dialog, which) -> {
                    deleteUserAccount();
                })
                .show();
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))

                .setNegativeButton(getString(R.string.logout_cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.logout_confirm), (dialog, which) -> {
                    logoutUser();
                })
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
            toneNames[i] = getString(tones[i].nameResId);
        }


        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle(getString(R.string.coach_dialog_title))
                .setItems(toneNames, (dialog, which) -> {

                    CoachTone selectedTone = tones[which];


                    saveCoachPreference(selectedTone.name());


                    btnCoachTone.setText(getString(R.string.coach_prefix) + getString(selectedTone.nameResId));


                    Toast.makeText(getContext(), "Coach set to " + getString(selectedTone.nameResId), Toast.LENGTH_SHORT).show();
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
        View overlay = requireActivity().findViewById(R.id.loadingOverlay);
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);


        overlay.animate().alpha(1f).setDuration(300).withEndAction(() -> {


            SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
            prefs.edit().putString("My_Lang", langCode).apply();


            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
            AppCompatDelegate.setApplicationLocales(appLocale);


            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }).start();
    }
    private void setDailyMotivation(TextView textView) {
        String[] quotes = getResources().getStringArray(R.array.daily_quotes);


        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);


        int quoteIndex = dayOfYear % quotes.length;

        textView.setText(quotes[quoteIndex]);
    }

    private void showPasswordConfirmationDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Error finding user email.", Toast.LENGTH_SHORT).show();
            return;
        }


        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");


        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(60, 0, 60, 0);
        passwordInput.setLayoutParams(params);
        container.addView(passwordInput);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Verify Identity")
                .setMessage("Please enter your password to continue.")
                .setView(container)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Verify", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        reauthenticateUser(user, password);
                    }
                })
                .show();
    }

    private void reauthenticateUser(FirebaseUser user, String password) {

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        showDeleteConfirmation();
                    } else {

                        Toast.makeText(getContext(), "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void ShowPasswordForSettings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Error finding user email.", Toast.LENGTH_SHORT).show();
            return;
        }


        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");


        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(60, 0, 60, 0);
        passwordInput.setLayoutParams(params);
        container.addView(passwordInput);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Verify Identity")
                .setMessage("Please enter your password to continue.")
                .setView(container)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Verify", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        reauthenticateUserForSettings(user, password);
                    }
                })
                .show();
    }

    private void reauthenticateUserForSettings(FirebaseUser user, String password) {

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSettingsDialog();
                    } else {

                        Toast.makeText(getContext(), "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void showSettingsDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.title_settings));


        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 20);


        final EditText inputUsername = new EditText(requireContext());
        inputUsername.setHint("Username");

        inputUsername.setText(user.getDisplayName());
        layout.addView(inputUsername);


        final EditText inputEmail = new EditText(requireContext());
        inputEmail.setHint("Email");

        inputEmail.setText(user.getEmail());
        layout.addView(inputEmail);


        final EditText inputPassword = new EditText(requireContext());
        inputPassword.setHint("New Password (leave blank to keep current)");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = inputUsername.getText().toString().trim();
            String newEmail = inputEmail.getText().toString().trim();
            String newPass = inputPassword.getText().toString().trim();

            processProfileUpdate(user, newName, newEmail, newPass);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void processProfileUpdate(FirebaseUser user, String name, String email, String password) {
        Context context = requireContext();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates).addOnSuccessListener(aVoid -> {

            db.collection("users")
                    .document(user.getUid())
                    .update("username", name)
                    .addOnSuccessListener(unused -> {
                        profileName.setText(name);
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show();
                    });
        });


        if (!email.equals(user.getEmail())) {
            user.updateEmail(email).addOnSuccessListener(aVoid -> {
                profileEmail.setText(email);
            });
        }


        if (!password.isEmpty()) {
            user.updatePassword(password).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show();
            });
        }
    }


}