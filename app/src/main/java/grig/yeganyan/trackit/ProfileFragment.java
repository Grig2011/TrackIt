package grig.yeganyan.trackit;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import grig.yeganyan.trackit.Login;

public class ProfileFragment extends Fragment {

    TextView profileName, profileEmail;
    Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            profileEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");
        }


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
}