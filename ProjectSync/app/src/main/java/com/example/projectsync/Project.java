package com.example.projectsync;

import com.google.firebase.firestore.DocumentId;

/** One project, stored as a document in the Firestore collection "projects". */
public class Project {

    public static final String COLLECTION = "projects";

    @DocumentId
    private String id;          // filled in by Firestore from the document id, not saved as a field
    private String title;
    private String desc;
    private String field;       // category, e.g. "Health"
    private String authorId;
    private String authorName;
    private long createdAt;     // milliseconds
    private boolean adopted;
    private String adoptedById;   // uid of the company that adopted it (null if not adopted)
    private String adoptedByName; // that company's name, shown on the details screen

    public Project() {
        // Firestore needs an empty constructor
    }

    public Project(String title, String desc, String field, String authorId,
                   String authorName, long createdAt, boolean adopted) {
        this.title = title;
        this.desc = desc;
        this.field = field;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.adopted = adopted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public String getField() { return field; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public long getCreatedAt() { return createdAt; }
    public boolean isAdopted() { return adopted; }
    public String getAdoptedById() { return adoptedById; }
    public String getAdoptedByName() { return adoptedByName; }

    public void setTitle(String title) { this.title = title; }
    public void setDesc(String desc) { this.desc = desc; }
    public void setField(String field) { this.field = field; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setAdopted(boolean adopted) { this.adopted = adopted; }
    public void setAdoptedById(String adoptedById) { this.adoptedById = adoptedById; }
    public void setAdoptedByName(String adoptedByName) { this.adoptedByName = adoptedByName; }
}
