package de.pls.stundenplaner.assignments;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import de.pls.stundenplaner.subjects.Subject;

@SuppressWarnings("unused")
public class AssignmentTest {

    private final LocalDate validDate = LocalDate.now().plusYears(1);
    private final LocalDate invalidDate = LocalDate.now().minusYears(1);

    // -- Valid Assignment Credentials -- //

    @Test
    void testAssignmentWithValidCredentials() {

        Assignment assignment = new Assignment(
                Subject.MATH,
                validDate,
                "Complete exercises 1-10"
        );

        assertEquals(Subject.MATH, assignment.getSubject());
        assertEquals(validDate, assignment.getDueDate());
        assertEquals("Complete exercises 1-10", assignment.getNotes());

    }

    // -- Create Assignment with unique credentials -- //

    @Test
    void testAssignmentWithEmptyNotes() {

        Assignment assignment = new Assignment(
                Subject.ENGLISH,
                validDate,
                ""
        );

        assertEquals(Subject.ENGLISH, assignment.getSubject());
        assertEquals(validDate, assignment.getDueDate());
        assertEquals("", assignment.getNotes());

    }

    // -- Create Assignment with invalid credentials -- //

    @Test
    void testAssignmentWithPastDueDate() {

        Assignment assignment = new Assignment(
                Subject.HISTORY,
                invalidDate,
                "Write a report on World War II"
        );

        assertEquals(Subject.HISTORY, assignment.getSubject());
        assertEquals(invalidDate, assignment.getDueDate());
        assertEquals("Write a report on World War II", assignment.getNotes());

    }

    @Test
    void testAssignmentWithANullNotes() {

        try {
            
            Assignment assignment = new Assignment(
                Subject.SCIENCE,
                LocalDate.of(2024, 10, 15),
                null
            );

        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

    @Test
    void testAssignmentWithANullSubject() {

        try {
            
            Assignment assignment = new Assignment(
                null,
                LocalDate.of(2024, 10, 15),
                "Hello World!"
            );

        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

    @Test
    void testAssignmentWithANullDueDate() {

        try {
            
            Assignment assignment = new Assignment(
                Subject.SCIENCE,
                null,
                "Hello World!"
            );

        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

}
