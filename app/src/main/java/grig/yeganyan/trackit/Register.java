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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import grig.yeganyan.trackit.model.User;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfPassword;
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
        etConfPassword = findViewById(R.id.etConfPassword);
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
        String confPassword = etConfPassword.getText().toString().trim();


        if (username.isEmpty()) {
            etUsername.setError(getString(R.string.error_username_required));
            etUsername.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confPassword)) {
            etConfPassword.setError(getString(R.string.error_password_mismatch));
            etConfPassword.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser != null) {

                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    getString(R.string.verify_email_sent, email),
                                                    Toast.LENGTH_LONG).show();
                                            Log.d("FIREBASE_VERIFY", "Email sent to " + email);
                                        } else {
                                            Log.e("FIREBASE_VERIFY", "Failed to send email", verifyTask.getException());
                                            Toast.makeText(this,
                                                    getString(R.string.verify_email_failed),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });


                            User user = new User(username, email, null);
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                getString(R.string.register_success),
                                                Toast.LENGTH_LONG).show();

                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        prefs.edit().putBoolean("registered", true).apply();
                                        prefs.edit().putString("userId", firebaseUser.getUid()).apply();

                                        startActivity(new Intent(Register.this, Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIRESTORE", "Error saving user", e);
                                        Toast.makeText(this,
                                                getString(R.string.error_firestore_save) + ": " + e.getLocalizedMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {

                        Exception exception = task.getException();
                        Log.e("REGISTER", "Registration failed", exception);

                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, getString(R.string.error_email_exists), Toast.LENGTH_LONG).show();
                        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, getString(R.string.error_weak_password), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, getString(R.string.error_register_failed), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}