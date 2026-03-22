package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.model.User;

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView GuestLink = findViewById(R.id.Guest);
        TextView link = findViewById(R.id.RegisterLink);
        link.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
        GuestLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Guest();
            }
        });

        btnLogin.setOnClickListener(v -> loginUser());

    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if(snapshot.isEmpty()) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        User loggedUser = snapshot.getDocuments().get(0).toObject(User.class);
                        String userId = snapshot.getDocuments().get(0).getId(); // <-- get Firestore doc ID

                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("registered", true).apply();
                        prefs.edit().putString("userId", userId).apply(); // <-- save userId

                        Toast.makeText(this, "Welcome " + loggedUser.getUsername(), Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    private void Guest(){
        String email = "gyeganyan11@gmail.com";
        String password = "Grig2011";
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if(snapshot.isEmpty()) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        User loggedUser = snapshot.getDocuments().get(0).toObject(User.class);
                        String userId = snapshot.getDocuments().get(0).getId();

                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("registered", true).apply();
                        prefs.edit().putString("userId", userId).apply();

                        Toast.makeText(this, "Welcome " + "Dear Guest", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}