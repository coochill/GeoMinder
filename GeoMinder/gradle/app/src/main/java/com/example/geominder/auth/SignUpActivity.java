package com.example.geominder.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geominder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import java.util.HashMap;
import java.util.Map;




public class SignUpActivity extends AppCompatActivity {
    private EditText emailField, usernameField, passwordField;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);  // Set layout
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Initialize views
        emailField = findViewById(R.id.emailField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        signUpButton = findViewById(R.id.signUpButton);
        TextView loginText = findViewById(R.id.loginText);
        SpannableString spannableString = new SpannableString("Already have an account? Login");
        // Find the position of the "Login" text and apply the blue color
        int loginStart = spannableString.toString().indexOf("Login");
        int loginEnd = loginStart + "Login".length();


        // Apply the color to the "Login" part of the string
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.white)),
                loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the styled text to the TextView
        loginText.setText(spannableString);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to validate input fields and sign up user
                signUpUser();
            }
        });




        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }




    private void signUpUser() {
        // Get input values
        String email = emailField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$";




        // Validate inputs
        if (email.isEmpty()) {
            emailField.setError("Email is required");
            return;
        }




        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Enter a valid email address");
            return;
        }




        if (username.isEmpty()) {
            usernameField.setError("Username is required");
            return;
        }




        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            return;
        }




        if (!password.matches(passwordPattern)) {
            passwordField.setError("Password must be at least 6 characters and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            return;
        }




        db.collection("users")
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Username already exists in Firestore
                                usernameField.setError("Username is already taken");
                            } else {
                                // Username is available, check email
                                checkIfEmailExists(email, username, password);
                            }
                        } else {
                            // Handle Firestore query error
                            Toast.makeText(SignUpActivity.this,
                                    "Error checking username: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void checkIfEmailExists(String email, String username, String password) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            // Check if there are any sign-in methods for the email
                            boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                            if (emailExists) {
                                emailField.setError("Email is already in use");
                            } else {
                                // Email does not exist, proceed to create a new user
                                createNewUser(email, username, password);
                            }
                        } else {
                            // Handle failure
                            Toast.makeText(SignUpActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void createNewUser(String email, String username, String password) {
        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                saveUserToFirestore(username, email);
                            }
                        } else {
                            // Failed to create user
                            Toast.makeText(SignUpActivity.this, "Authentication Failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void saveUserToFirestore(String username, String email) {
        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);




        // Save user data to Firestore with the username as the document ID
        db.collection("users")
                .document(username)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() { // Specify <Void> here
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Navigate to the Login page after successful registration
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);  // Replace LoginActivity.class with your actual login activity
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this,
                                    "Firestore Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}





