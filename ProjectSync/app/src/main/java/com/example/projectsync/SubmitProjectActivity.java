package com.example.projectsync;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/** Story 3 – an innovator submits a new project (title, field, description) to Firestore. */
public class SubmitProjectActivity extends AppCompatActivity {

    private TextInputLayout titleLayout, fieldLayout, descLayout;
    private TextInputEditText titleInput, descInput;
    private MaterialAutoCompleteTextView fieldInput;
    private Button submitButton;
    private ProgressBar progress;
    private String[] fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_submit_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 1. Find the views
        titleLayout = findViewById(R.id.titleLayout);
        fieldLayout = findViewById(R.id.fieldLayout);
        descLayout = findViewById(R.id.descLayout);
        titleInput = findViewById(R.id.titleInput);
        fieldInput = findViewById(R.id.fieldInput);
        descInput = findViewById(R.id.descInput);
        submitButton = findViewById(R.id.submitButton);
        progress = findViewById(R.id.progress);

        // 2. Fill the Field dropdown
        fields = getResources().getStringArray(R.array.project_fields);
        fieldInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, fields));
        fieldInput.setOnItemClickListener((parent, view, position, id) -> fieldLayout.setError(null));

        // 3. Button click
        submitButton.setOnClickListener(v -> attemptSubmit());
    }

    private void attemptSubmit() {
        String title = text(titleInput);
        String desc = text(descInput);
        String field = fieldInput.getText() == null ? "" : fieldInput.getText().toString().trim();

        // 4. Validate input (rules live in ProjectValidator so they are unit tested)
        String titleError = ProjectValidator.validateTitle(title);
        String fieldError = ProjectValidator.validateField(field, fields);
        String descError = ProjectValidator.validateDescription(desc);
        titleLayout.setError(titleError);
        fieldLayout.setError(fieldError);
        descLayout.setError(descError);
        if (titleError != null || fieldError != null || descError != null) return;

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            showError(getString(R.string.please_log_in_again));
            return;
        }

        // 5. Look up the author's name (saved in users/{uid} at registration), then save
        setLoading(true);
        FirebaseDatabase.getInstance().getReference("users").child(current.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    String authorName = (user != null && user.getName() != null)
                            ? user.getName() : current.getEmail();
                    saveProject(title, desc, field, current.getUid(), authorName);
                })
                .addOnFailureListener(e ->
                        saveProject(title, desc, field, current.getUid(), current.getEmail()));
    }

    // 6. Store the project in Firestore; the feed listener shows it straight away
    private void saveProject(String title, String desc, String field, String uid, String authorName) {
        Project project = new Project(title, desc, field, uid, authorName,
                System.currentTimeMillis(), false);
        FirebaseFirestore.getInstance().collection(Project.COLLECTION)
                .add(project)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, R.string.project_submitted, Toast.LENGTH_SHORT).show();
                    finish(); // back to the feed
                })
                .addOnFailureListener(e ->
                        showError(getString(R.string.could_not_submit, e.getMessage())));
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void showError(String message) {
        setLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading);
    }
}
