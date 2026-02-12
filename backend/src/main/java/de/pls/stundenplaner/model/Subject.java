package de.pls.stundenplaner.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
@Getter
public enum Subject {

    MATH("Math"),
    GERMAN("German"),
    SCIENCE("Science"),
    POLITICS("Politics"),
    COMPUTER_SCIENCE("Computer Science"),
    ENGLISH("English"),
    HISTORY("History"),
    PHYSICAL_EDUCATION("Physical Education"),
    MUSIC("Music"),
    ART("Art"),
    RELIGION("Religion");

    private final String name;

    Subject(String name) {
        this.name = name;
    }

    public static List<String> getAllSubjects() {
        return Arrays.stream(Subject.values())
                .map(Subject::getName)
                .toList();
    }

}