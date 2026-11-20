package com.example.projectsync;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectFeed {

    private final ProjectAdapter adapter = new ProjectAdapter();
    private final ProgressBar progress;
    private final TextView empty;
    private final Query query = FirebaseDatabase.getInstance()
            .getReference("projects")
            .orderByChild("createdAt");
    private ValueEventListener listener;

    public ProjectFeed(Activity activity) {
        RecyclerView list = activity.findViewById(R.id.feedList);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setAdapter(adapter);
        progress = activity.findViewById(R.id.feedProgress);
        empty = activity.findViewById(R.id.feedEmpty);
    }

    /** Starts listening: the list updates by itself whenever a project is added. */
    public void start() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Project> projects = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Project p = child.getValue(Project.class);
                    if (p != null) {
                        p.setId(child.getKey());
                        projects.add(p);
                    }
                }
                Collections.reverse(projects); // newest first
                adapter.setProjects(projects);
                progress.setVisibility(View.GONE);
                empty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.setVisibility(View.GONE);
                empty.setText("Could not load projects: " + error.getMessage());
                empty.setVisibility(View.VISIBLE);
            }
        };
        query.addValueEventListener(listener);
    }

    /** Stops listening (call when the screen closes). */
    public void stop() {
        if (listener != null) query.removeEventListener(listener);
    }


}