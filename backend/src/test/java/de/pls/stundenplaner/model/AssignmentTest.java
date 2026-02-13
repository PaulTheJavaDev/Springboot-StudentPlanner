package de.pls.stundenplaner.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public class AssignmentTest {

    private final LocalDate futureDate = LocalDate.now().plusYears(1);
    
    @Test
    void testAssignmentWithValidCredentials() {

        Assignment assignment = new Assignment(
                Subject.MATH,
                futureDate,
                "Complete exercises 1-10"
        );

        assertEquals(Subject.MATH, assignment.getSubject());
        assertEquals(futureDate, assignment.getDueDate());
        assertEquals("Complete exercises 1-10", assignment.getNotes());

    }

    @Test
    void testAssignmentWithEmptyNotes() {

        Assignment assignment = new Assignment(
                Subject.ENGLISH,
                futureDate,
                ""
        );

        assertEquals(Subject.ENGLISH, assignment.getSubject());
        assertEquals(futureDate, assignment.getDueDate());
        assertEquals("", assignment.getNotes());

    }

    @Test
    void testAssignmentWithPastDueDate() {

        Assignment assignment = new Assignment(
                Subject.HISTORY,
                futureDate.minusYears(2),
                "Write a report on World War II"
        );

        assertEquals(Subject.HISTORY, assignment.getSubject());
        assertEquals(futureDate.minusYears(2), assignment.getDueDate());
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
