package com.example.projectsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for Story 3 – Project Submission input validation (written test-first). */
public class ProjectValidatorTest {

    private static final String[] FIELDS = {"Technology", "Health", "Education"};
    private static final String GOOD_DESC = "An app that matches students with mentors.";

    // ---- Title ----
    @Test public void emptyTitle_isRejected() {
        assertEquals(ProjectValidator.TITLE_REQUIRED, ProjectValidator.validateTitle(""));
    }

    @Test public void blankTitle_isRejected() {
        assertEquals(ProjectValidator.TITLE_REQUIRED, ProjectValidator.validateTitle("   "));
    }

    @Test public void nullTitle_isRejected() {
        assertEquals(ProjectValidator.TITLE_REQUIRED, ProjectValidator.validateTitle(null));
    }

    @Test public void tooLongTitle_isRejected() {
        String longTitle = new String(new char[ProjectValidator.TITLE_MAX + 1]).replace('\0', 'a');
        assertEquals(ProjectValidator.TITLE_TOO_LONG, ProjectValidator.validateTitle(longTitle));
    }

    @Test public void normalTitle_isAccepted() {
        assertNull(ProjectValidator.validateTitle("Smart Irrigation Sensor"));
    }

    // ---- Description ----
    @Test public void emptyDescription_isRejected() {
        assertEquals(ProjectValidator.DESC_REQUIRED, ProjectValidator.validateDescription(""));
    }

    @Test public void shortDescription_isRejected() {
        assertEquals(ProjectValidator.DESC_TOO_SHORT, ProjectValidator.validateDescription("Too short"));
    }

    @Test public void tooLongDescription_isRejected() {
        String longDesc = new String(new char[ProjectValidator.DESC_MAX + 1]).replace('\0', 'a');
        assertEquals(ProjectValidator.DESC_TOO_LONG, ProjectValidator.validateDescription(longDesc));
    }

    @Test public void normalDescription_isAccepted() {
        assertNull(ProjectValidator.validateDescription(GOOD_DESC));
    }

    // ---- Field ----
    @Test public void missingField_isRejected() {
        assertEquals(ProjectValidator.FIELD_REQUIRED, ProjectValidator.validateField("", FIELDS));
    }

    @Test public void unknownField_isRejected() {
        assertEquals(ProjectValidator.FIELD_REQUIRED, ProjectValidator.validateField("Cooking", FIELDS));
    }

    @Test public void listedField_isAccepted() {
        assertNull(ProjectValidator.validateField("Health", FIELDS));
    }

    // ---- Whole form ----
    @Test public void allValid_isValid() {
        assertTrue(ProjectValidator.isValid("Project Sync", GOOD_DESC, "Technology", FIELDS));
    }

    @Test public void allEmpty_isNotValid() {
        assertFalse(ProjectValidator.isValid("", "", "", FIELDS));
    }
}
