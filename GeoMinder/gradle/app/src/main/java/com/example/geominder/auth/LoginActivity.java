package com.example.geominder.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geominder.ui.MainActivity;
import com.example.geominder.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;



public class LoginActivity extends AppCompatActivity {
    private EditText emailOrUsernameField, passwordField, usernameField;
    private Button loginButton, googleLoginButton, saveButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private Map<String, Integer> loginAttempts = new HashMap<>();



    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                @Override
                public void onActivityResult(androidx.activity.result.ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            if (task.isSuccessful()) {
                                GoogleSignInAccount account = task.getResult();
                                firebaseAuthWithGoogle(account, account.getEmail());
                            } else {
                                // Log the exception
                                Log.e("GoogleSignIn", "Sign-In failed: " + task.getException());
                                Toast.makeText(LoginActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Log if the data is null
                            Log.e("GoogleSignIn", "Google Sign-In data is null");
                        }
                    } else {
                        Log.e("GoogleSignIn", "Google Sign-In result code not OK");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        emailOrUsernameField = findViewById(R.id.emailOrUsernameField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        usernameField = findViewById(R.id.usernameField); // Used for Google Sign-In username
        saveButton = findViewById(R.id.saveButton);
        TextView signUpText = findViewById(R.id.signUpText);


        SpannableString spannableString = new SpannableString("Don't have an account? Sign Up");
        int signUpStart = spannableString.toString().indexOf("Sign Up");
        int signUpEnd = signUpStart + "Sign Up".length();
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.white)), signUpStart, signUpEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpText.setText(spannableString);
        usernameField.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(v -> loginUser());
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.forgotPasswordText).setOnClickListener(v -> handleForgotPassword());
    }

    private void handleForgotPassword() {
        String emailOrUsername = emailOrUsernameField.getText().toString().trim();


        if (emailOrUsername.isEmpty()) {
            emailOrUsernameField.setError("Please enter the email associated with your account");
            return;
        }
        if (isValidEmail(emailOrUsername)) {
            // If it's a valid email, check Firebase Authentication for the email
            checkIfEmailExistsForgotPassword(emailOrUsername);
        } else {
            // Check if it's a username (this assumes you are storing users in Firestore)
            emailOrUsernameField.setError("Please enter the email associated with your account");
        }
    }


    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loginUser() {
        String emailOrUsername = emailOrUsernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();


        if (emailOrUsername.isEmpty()) {
            emailOrUsernameField.setError("Email or Username is required");
            return;
        }
        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            return;
        }
        handleLogin(emailOrUsername, password);
    }


    private void handleLogin(String emailOrUsername, String password) {
        if (isValidEmail(emailOrUsername)) {
            checkIfEmailExists(emailOrUsername, password);
        } else {
            checkIfDocumentIdExists(emailOrUsername, password);
        }
    }

    private void checkIfEmailExists(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        loginWithEmail(email, password);
                    } else {
                        // If no document with this email exists
                        Toast.makeText(LoginActivity.this, "Email not registered.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(LoginActivity.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfEmailExistsForgotPassword(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        sendPasswordResetEmail(email);
                    } else {
                        // If no document with this email exists
                        Toast.makeText(LoginActivity.this, "Email not registered.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(LoginActivity.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailPattern);
    }

    private void checkIfDocumentIdExists(String documentId, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(documentId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String email = document.getString("email");
                    if (email != null) {
                        loginWithEmail(email, password);
                    } else {
                        Toast.makeText(LoginActivity.this, "Email not found for this user.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Username is not registered.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Failed to retrieve user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Reset login attempts on success
                        loginAttempts.put(email, 0);
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        proceedToHome();
                    } else {
                        // Increment login attempts
                        int attempts = loginAttempts.getOrDefault(email, 0) + 1;
                        loginAttempts.put(email, attempts);
                        if (attempts >= MAX_LOGIN_ATTEMPTS) {
                            // Send password reset email and notify user
                            sendPasswordResetEmail(email);
                            Toast.makeText(LoginActivity.this, "Too many failed attempts. Password reset email sent.", Toast.LENGTH_LONG).show();
                        } else {
                            int remaining = MAX_LOGIN_ATTEMPTS - attempts;
                            Toast.makeText(LoginActivity.this, "Invalid Credentials. " + remaining + " attempts remaining.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void signInWithGoogle() {
        // Sign out to force account picker
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Now initiate Google Sign-In, which will show the account picker
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkForUsername(user, email);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkForUsername(FirebaseUser user, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email) // Search by the email field
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // If the email is found in Firestore
                        DocumentSnapshot document = task.getResult().getDocuments().get(0); // Get the first document (email should be unique)
                        if (document.exists() && document.getString("username") != null) {
                            // If a username exists, proceed to home
                            proceedToHome();
                        } else {
                            // If no username exists, prompt for a username
                            promptForUsername(user);
                        }
                    } else {
                        promptForUsername(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., network issues)
                    Toast.makeText(LoginActivity.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void promptForUsername(FirebaseUser user) {
        usernameField.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            if (username.isEmpty()) {
                usernameField.setError("Username is required");
                return;
            }


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Username already exists
                            usernameField.setError("Username already exists. Please choose another.");
                        } else {
                            // Username is unique, save user data
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", user.getEmail());
                            userData.put("username", username);

                            db.collection("users")
                                    .document(username)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        proceedToHome();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void proceedToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}



