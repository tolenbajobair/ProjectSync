package com.example.projectsync;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        // Not logged in -> login screen
        if (current == null) {
            open(LoginActivity.class);
            return;
        }

        // Logged in -> read their role and open the right home screen
        FirebaseDatabase.getInstance().getReference("users").child(current.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        FirebaseAuth.getInstance().signOut();
                        open(LoginActivity.class);
                    } else if (User.ROLE_COMPANY.equals(user.getRole())) {
                        open(CompanyHomeActivity.class);
                    } else {
                        open(InnovatorHomeActivity.class);
                    }
                })
                .addOnFailureListener(e -> open(LoginActivity.class));
    }

    private void open(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}