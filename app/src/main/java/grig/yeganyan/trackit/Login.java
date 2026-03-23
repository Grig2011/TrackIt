package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.model.User;

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if(firebaseUser != null) {
                            if(firebaseUser.isEmailVerified()) {

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(firebaseUser.getUid())
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            if(doc.exists()) {
                                                String username = doc.getString("username");

                                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                prefs.edit().putBoolean("registered", true).apply();
                                                prefs.edit().putString("userId", firebaseUser.getUid()).apply();
                                                prefs.edit().putString("username", username).apply();

                                                Toast.makeText(this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void Guest() {

        auth.signInAnonymously()
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        if(user != null){

                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("registered", false).apply();
                            prefs.edit().putString("userId", user.getUid()).apply();

                            Toast.makeText(this, "Guest login success", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        }

                    } else {

                        Exception e = task.getException();
                        Log.e("AUTH_ERROR", e.getMessage());
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}