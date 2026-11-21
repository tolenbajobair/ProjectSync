package com.example.projectsync;

import android.os.Bundle;
import android.text.format.DateFormat;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;

/**
 * Story 3 – shows the full details of one project.
 * The innovator who submitted it can delete it; a company can adopt it (and undo its own adoption).
 */
public class ProjectDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "projectId";

    private DocumentReference docRef;
    private ListenerRegistration registration;

    private View content;
    private ProgressBar progress;
    private TextView titleText, fieldText, authorText, dateText, statusText, descText, errorText;
    private Button deleteButton, adoptButton;

    private Project project;          // the project currently shown
    private User currentUser;         // logged-in user's profile (name + role), loaded once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_project_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        content = findViewById(R.id.detailsContent);
        progress = findViewById(R.id.detailsProgress);
        titleText = findViewById(R.id.detailsTitle);
        fieldText = findViewById(R.id.detailsField);
        authorText = findViewById(R.id.detailsAuthor);
        dateText = findViewById(R.id.detailsDate);
        statusText = findViewById(R.id.detailsStatus);
        descText = findViewById(R.id.detailsDesc);
        errorText = findViewById(R.id.detailsError);
        deleteButton = findViewById(R.id.deleteButton);
        adoptButton = findViewById(R.id.adoptButton);

        String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            showMessage(getString(R.string.project_not_found));
            return;
        }
        docRef = FirebaseFirestore.getInstance().collection(Project.COLLECTION).document(projectId);
        deleteButton.setOnClickListener(v -> confirmDelete());
        adoptButton.setOnClickListener(v -> toggleAdopted());

        // Load the logged-in user's role so we know whether to show the Adopt button
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            FirebaseDatabase.getInstance().getReference("users").child(current.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        currentUser = snapshot.getValue(User.class);
                        updateButtons();
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (docRef == null) return;
        // Live listener: if the project changes (e.g. gets adopted later) the screen updates
        registration = docRef.addSnapshotListener((snapshot, error) -> {
            progress.setVisibility(View.GONE);
            if (error != null) {
                showMessage(getString(R.string.could_not_load_project, error.getMessage()));
                return;
            }
            if (snapshot == null || !snapshot.exists()) {
                showMessage(getString(R.string.project_not_found));
                return;
            }
            Project p = snapshot.toObject(Project.class);
            if (p != null) bind(p);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void bind(Project p) {
        errorText.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);

        titleText.setText(p.getTitle());
        fieldText.setText(p.getField());
        fieldText.setVisibility(p.getField() == null || p.getField().isEmpty() ? View.GONE : View.VISIBLE);
        authorText.setText(getString(R.string.by_author, p.getAuthorName()));
        dateText.setText(getString(R.string.submitted_on,
                DateFormat.getMediumDateFormat(this).format(new Date(p.getCreatedAt()))));
        if (p.isAdopted() && p.getAdoptedByName() != null) {
            statusText.setText(getString(R.string.status_adopted_by, p.getAdoptedByName()));
        } else {
            statusText.setText(p.isAdopted() ? R.string.status_adopted : R.string.status_not_adopted);
        }
        statusText.setTextColor(p.isAdopted() ? 0xFF2E7D32 : 0xFF757575);
        descText.setText(p.getDesc());

        project = p;
        updateButtons();
    }

    /** Shows Delete to the project's owner and Adopt / Undo adoption to companies. */
    private void updateButtons() {
        if (project == null) return;
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current == null ? null : current.getUid();

        // Only the innovator who submitted the project can delete it
        boolean isOwner = uid != null && uid.equals(project.getAuthorId());
        deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        // Only companies can adopt; once adopted, only the company that adopted it can undo
        boolean isCompany = currentUser != null && User.ROLE_COMPANY.equals(currentUser.getRole());
        boolean adoptedByMe = project.isAdopted() && uid != null && uid.equals(project.getAdoptedById());
        if (isCompany && !project.isAdopted()) {
            adoptButton.setText(R.string.mark_as_adopted);
            adoptButton.setVisibility(View.VISIBLE);
        } else if (isCompany && adoptedByMe) {
            adoptButton.setText(R.string.mark_as_not_adopted);
            adoptButton.setVisibility(View.VISIBLE);
        } else {
            adoptButton.setVisibility(View.GONE);
        }
        adoptButton.setEnabled(true);
    }

    /** Company adopts the project, or undoes its own adoption. */
    private void toggleAdopted() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null || currentUser == null || project == null) return;
        adoptButton.setEnabled(false);

        boolean adopt = !project.isAdopted();
        docRef.update(
                        "adopted", adopt,
                        "adoptedById", adopt ? current.getUid() : null,
                        "adoptedByName", adopt ? currentUser.getName() : null)
                .addOnSuccessListener(unused -> Toast.makeText(this,
                        adopt ? R.string.project_adopted : R.string.adoption_removed,
                        Toast.LENGTH_SHORT).show())
                // the live listener refreshes the screen; on failure just re-enable the button
                .addOnFailureListener(e -> {
                    adoptButton.setEnabled(true);
                    Toast.makeText(this, getString(R.string.could_not_update, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_project)
                .setMessage(R.string.delete_project_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> deleteProject())
                .show();
    }

    private void deleteProject() {
        deleteButton.setEnabled(false);
        if (registration != null) {
            registration.remove(); // stop listening so we don't show "not found" after deleting
            registration = null;
        }
        docRef.delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    deleteButton.setEnabled(true);
                    Toast.makeText(this, getString(R.string.could_not_delete, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showMessage(String message) {
        progress.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
