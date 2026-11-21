package com.example.projectsync;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows projects from Firestore, newest first. Shared by the Innovator and Company home screens.
 * The company home can filter it to show only the projects that company adopted.
 */
public class ProjectFeed {

    public enum Filter { ALL, ADOPTED_BY_ME }

    private final Activity activity;
    private final ProjectAdapter adapter;
    private final ProgressBar progress;
    private final TextView empty;
    private final Query query = FirebaseFirestore.getInstance()
            .collection(Project.COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING);
    private ListenerRegistration registration;

    private List<Project> allProjects = new ArrayList<>();
    private Filter filter = Filter.ALL;
    private boolean loaded = false;

    public ProjectFeed(Activity activity) {
        this.activity = activity;

        // Tapping a project opens its details screen
        adapter = new ProjectAdapter(project -> {
            Intent intent = new Intent(activity, ProjectDetailsActivity.class);
            intent.putExtra(ProjectDetailsActivity.EXTRA_PROJECT_ID, project.getId());
            activity.startActivity(intent);
        });

        RecyclerView list = activity.findViewById(R.id.feedList);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setAdapter(adapter);
        progress = activity.findViewById(R.id.feedProgress);
        empty = activity.findViewById(R.id.feedEmpty);
    }

    /** Starts listening: the list updates by itself whenever a project is added or changed. */
    public void start() {
        registration = query.addSnapshotListener((snapshot, error) -> {
            progress.setVisibility(View.GONE);
            if (error != null) {
                adapter.setProjects(new ArrayList<>());
                empty.setText(activity.getString(R.string.could_not_load_projects, error.getMessage()));
                empty.setVisibility(View.VISIBLE);
                return;
            }
            List<Project> projects = new ArrayList<>();
            if (snapshot != null) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Project p = doc.toObject(Project.class);
                    if (p != null) projects.add(p);
                }
            }
            allProjects = projects;
            loaded = true;
            show();
        });
    }

    /** Changes which projects are shown (called when the user switches tabs). */
    public void setFilter(Filter filter) {
        this.filter = filter;
        if (loaded) show();
    }

    private void show() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current == null ? "" : current.getUid();

        List<Project> shown = new ArrayList<>();
        for (Project p : allProjects) {
            if (filter == Filter.ALL
                    || (filter == Filter.ADOPTED_BY_ME && p.isAdopted() && uid.equals(p.getAdoptedById()))) {
                shown.add(p);
            }
        }
        adapter.setProjects(shown);

        empty.setText(filter == Filter.ADOPTED_BY_ME
                ? R.string.no_adopted_projects : R.string.no_projects_yet);
        empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Stops listening (call when the screen closes). */
    public void stop() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
