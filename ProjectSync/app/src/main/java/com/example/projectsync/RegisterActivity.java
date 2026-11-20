package com.example.projectsync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmLayout;
    private TextInputEditText nameInput, emailInput, passwordInput, confirmInput;
    private RadioGroup roleGroup;
    private TextView roleError;
    private Button registerButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // 1. Find the views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmLayout = findViewById(R.id.confirmLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        roleGroup = findViewById(R.id.roleGroup);
        roleError = findViewById(R.id.roleError);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.progress);

        // 2. Button clicks
        registerButton.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.goToLogin).setOnClickListener(v -> finish()); // back to login
    }

    private void attemptRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmInput.getText().toString().trim();

        String role = null;
        if (roleGroup.getCheckedRadioButtonId() == R.id.roleInnovator) role = User.ROLE_INNOVATOR;
        if (roleGroup.getCheckedRadioButtonId() == R.id.roleCompany) role = User.ROLE_COMPANY;

        // 3. Validate input
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);
        roleError.setVisibility(View.GONE);
        boolean ok = true;

        if (name.isEmpty()) {
            nameLayout.setError("Enter your name");
            ok = false;
        }
        if (email.isEmpty()) {
            emailLayout.setError("Enter your email");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            ok = false;
        }
        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            ok = false;
        }
        if (!confirm.equals(password)) {
            confirmLayout.setError("Passwords don't match");
            ok = false;
        }
        if (role == null) {
            roleError.setVisibility(View.VISIBLE);
            ok = false;
        }
        if (!ok) return;

        // 4. Create the account in Firebase Authentication
        setLoading(true);
        final String chosenRole = role;
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result ->
                        saveProfile(result.getUser(), name, email, chosenRole))
                .addOnFailureListener(e -> showError("Registration failed: " + e.getMessage()));
    }

    // 5. Save name + role to users, because Auth only stores email/password
    private void saveProfile(FirebaseUser firebaseUser, String name, String email, String role) {
        User user = new User(firebaseUser.getUid(), name, email, role);
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid())
                .setValue(user)
                .addOnSuccessListener(unused -> goHome(role))
                .addOnFailureListener(e -> {
                    firebaseUser.delete(); // don't leave an account with no profile
                    showError("Could not save profile: " + e.getMessage());
                });
    }

    // 6. Firebase logs the user in automatically after registering, so go straight home
    private void goHome(String role) {
        Class<?> home = User.ROLE_COMPANY.equals(role)
                ? CompanyHomeActivity.class
                : InnovatorHomeActivity.class;
        Intent intent = new Intent(this, home);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        setLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }
}