package de.pls.stundenplaner.subjects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles Web Requests for the Subjects
 */
@RestController
@RequestMapping("/subjects")
public class SubjectController {

    /**
     * Gets all possible Subjects
     * @return A List of Subjects as Strings, since its easier for the Frontend to work with Strings.
     */
    @GetMapping
    public List<String> getAllSubjects() {
        return Subject.getAllSubjects();
    }

}