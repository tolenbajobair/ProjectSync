package com.example.projectsync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // 1. Find the views from the layout
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.progress);
        TextView goToRegister = findViewById(R.id.goToRegister);

        // 2. Button clicks
        loginButton.setOnClickListener(v -> attemptLogin());
        goToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // 3. Validate input
        emailLayout.setError(null);
        passwordLayout.setError(null);
        boolean ok = true;

        if (email.isEmpty()) {
            emailLayout.setError("Enter your email");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            ok = false;
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Enter your password");
            ok = false;
        }
        if (!ok) return;

        // 4. Sign in with Firebase
        setLoading(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> loadRoleAndGoHome(result.getUser().getUid()))
                .addOnFailureListener(e -> showError("Login failed: " + e.getMessage()));
    }

    // 5. Read users to find the role, then open the right home screen
    private void loadRoleAndGoHome(String uid) {
        FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        FirebaseAuth.getInstance().signOut();
                        showError("No profile found for this account");
                        return;
                    }
                    Class<?> home = User.ROLE_COMPANY.equals(user.getRole())
                            ? CompanyHomeActivity.class
                            : InnovatorHomeActivity.class;
                    Intent intent = new Intent(this, home);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> showError("Could not load profile: " + e.getMessage()));
    }

    private void showError(String message) {
        setLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }
}