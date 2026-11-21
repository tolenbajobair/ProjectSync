package com.example.projectsync;

/**
 * Input rules for the Submit Project screen.
 * Plain Java (no Android classes) so it can be unit tested with JUnit (test-first).
 * Each method returns an error message, or null when the input is valid.
 */
public final class ProjectValidator {

    public static final int TITLE_MAX = 80;
    public static final int DESC_MIN = 20;
    public static final int DESC_MAX = 1000;

    public static final String TITLE_REQUIRED = "Title is required";
    public static final String TITLE_TOO_LONG = "Title must be at most " + TITLE_MAX + " characters";
    public static final String DESC_REQUIRED = "Description is required";
    public static final String DESC_TOO_SHORT = "Description must be at least " + DESC_MIN + " characters";
    public static final String DESC_TOO_LONG = "Description must be at most " + DESC_MAX + " characters";
    public static final String FIELD_REQUIRED = "Please choose a field";

    private ProjectValidator() {
    }

    public static String validateTitle(String title) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) return TITLE_REQUIRED;
        if (t.length() > TITLE_MAX) return TITLE_TOO_LONG;
        return null;
    }

    public static String validateDescription(String desc) {
        String d = desc == null ? "" : desc.trim();
        if (d.isEmpty()) return DESC_REQUIRED;
        if (d.length() < DESC_MIN) return DESC_TOO_SHORT;
        if (d.length() > DESC_MAX) return DESC_TOO_LONG;
        return null;
    }

    /** The field must be one of the allowed options (the dropdown list). */
    public static String validateField(String field, String[] allowedFields) {
        if (field == null || field.trim().isEmpty()) return FIELD_REQUIRED;
        for (String allowed : allowedFields) {
            if (allowed.equals(field.trim())) return null;
        }
        return FIELD_REQUIRED;
    }

    /** True when every input is valid. */
    public static boolean isValid(String title, String desc, String field, String[] allowedFields) {
        return validateTitle(title) == null
                && validateDescription(desc) == null
                && validateField(field, allowedFields) == null;
    }
}
