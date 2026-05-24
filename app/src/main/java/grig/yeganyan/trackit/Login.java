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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private static final String GUEST_EMAIL = "innovationcampus26@gmail.com";
    private static final String GUEST_PASSWORD = "Samsung2026";
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
        TextView link = findViewById(R.id.RegisterLink);

        link.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });

        findViewById(R.id.Guest).setOnClickListener(v -> Guest());

        btnLogin.setOnClickListener(v -> loginUser());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> signInGoogle());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (firebaseUser.isEmailVerified()) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(firebaseUser.getUid())
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            if (doc.exists()) {
                                                String username = doc.getString("username");

                                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                prefs.edit().putBoolean("registered", true).apply();
                                                prefs.edit().putString("userId", firebaseUser.getUid()).apply();
                                                prefs.edit().putString("username", username).apply();

                                                Toast.makeText(this, getString(R.string.welcome_user, username), Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(this, getString(R.string.please_verify_email), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            String errorCode = ((FirebaseAuthInvalidUserException) exception).getErrorCode();

                            if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                                Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_LONG).show();
                            } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                                Toast.makeText(this, getString(R.string.error_account_disabled), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                            }
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            String errorCode = ((FirebaseAuthInvalidCredentialsException) exception).getErrorCode();


                            if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_LONG).show();
                            } else if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                                Toast.makeText(this, getString(R.string.error_wrong_password), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, getString(R.string.error_wrong_password), Toast.LENGTH_LONG).show();
                            }
                        } else {


                            Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void Guest() {
        auth.signInWithEmailAndPassword(GUEST_EMAIL, GUEST_PASSWORD)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("registered", false)
                                    .putString("userId", user.getUid())
                                    .putString("username", "Guest")
                                    .apply();

                            Toast.makeText(this, getString(R.string.welcome_user, "Guest"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Exception e = task.getException();

                        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {

                            Log.d("GUEST_AUTH", "Static profile missing or credential mismatched. Forcing auto-registration reset...");

                            auth.createUserWithEmailAndPassword(GUEST_EMAIL, GUEST_PASSWORD)
                                    .addOnCompleteListener(this, createWithEmailTask -> {
                                        if (createWithEmailTask.isSuccessful()) {
                                            FirebaseUser newUser = auth.getCurrentUser();
                                            if (newUser != null) {
                                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                prefs.edit().putBoolean("registered", false)
                                                        .putString("userId", newUser.getUid())
                                                        .putString("username", "Guest")
                                                        .apply();

                                                Toast.makeText(this, getString(R.string.welcome_user, "Guest"), Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                                finish();
                                            }
                                        } else {
                                            Exception err = createWithEmailTask.getException();
                                            String msg = (err != null && err.getMessage() != null) ? err.getMessage() : "Registration barrier";
                                            Log.e("GUEST_AUTH_ERROR", msg);
                                            Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Authentication failed";
                            Log.e("GUEST_AUTH_ERROR", msg);
                            Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed, code: " + e.getStatusCode(), e);
                Toast.makeText(this, getString(R.string.error_login_failed) + " (Code: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Google User";
                            String uid = user.getUid();


                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (!documentSnapshot.exists()) {

                                            java.util.Map<String, Object> userData = new java.util.HashMap<>();
                                            userData.put("username", displayName);
                                            userData.put("email", user.getEmail());

                                            db.collection("users").document(uid).set(userData)
                                                    .addOnSuccessListener(aVoid -> savePrefsAndProceed(uid, displayName))
                                                    .addOnFailureListener(e -> Toast.makeText(Login.this, "Firestore init failed", Toast.LENGTH_SHORT).show());
                                        } else {

                                            String storedUsername = documentSnapshot.getString("username");
                                            savePrefsAndProceed(uid, storedUsername != null ? storedUsername : displayName);
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(Login.this, "Database connection failed", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void savePrefsAndProceed(String uid, String username) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("registered", true)
                .putString("userId", uid)
                .putString("username", username)
                .apply();

        Toast.makeText(this, getString(R.string.welcome_user, username), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Login.this, MainActivity.class));
        finish();
    }
}