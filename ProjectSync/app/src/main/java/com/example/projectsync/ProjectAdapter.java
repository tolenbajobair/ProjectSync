package com.example.projectsync;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projects = new ArrayList<>();

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project p = projects.get(position);

        holder.title.setText(p.getTitle());
        holder.desc.setText(p.getDesc());

        // "Health • by Sara • 5 minutes ago • Not adopted"
        String time = DateUtils.getRelativeTimeSpanString(
                p.getCreatedAt(), System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS).toString();
        String status = p.isAdopted() ? "Adopted" : "Not adopted";

        StringBuilder meta = new StringBuilder();
        if (p.getField() != null && !p.getField().isEmpty()) {
            meta.append(p.getField()).append(" • ");
        }
        meta.append("by ").append(p.getAuthorName())
                .append(" • ").append(time)
                .append(" • ").append(status);
        holder.meta.setText(meta.toString());
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, desc, meta;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.projectTitle);
            desc = itemView.findViewById(R.id.projectDesc);
            meta = itemView.findViewById(R.id.projectMeta);
        }
    }
}