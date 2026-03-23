package grig.yeganyan.trackit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.model.User;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Input validation
        if (username.isEmpty()) {
            etUsername.setError("Username required");
            etUsername.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        // Create user with FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser != null) {
                            // Send verification email
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Verification email sent to " + email,
                                                    Toast.LENGTH_LONG).show();
                                            Log.d("FIREBASE_VERIFY", "Email sent to " + email);
                                        } else {
                                            Log.e("FIREBASE_VERIFY", "Failed to send email",
                                                    verifyTask.getException());
                                            Toast.makeText(this,
                                                    "Failed to send verification email",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            // Save extra info in Firestore (without password)
                            User user = new User(username, email, null);
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                "Registered Successfully! Please verify your email before login.",
                                                Toast.LENGTH_LONG).show();

                                        // Save login state locally
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        prefs.edit().putBoolean("registered", true).apply();
                                        prefs.edit().putString("userId", firebaseUser.getUid()).apply();

                                        // Redirect to Login screen
                                        startActivity(new Intent(Register.this, Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIRESTORE", "Error saving user", e);
                                        Toast.makeText(this,
                                                "Error saving user info: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("REGISTER", "Registration failed", task.getException());
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}