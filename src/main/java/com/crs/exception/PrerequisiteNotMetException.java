package com.crs.exception;

import java.util.List;

public class PrerequisiteNotMetException extends RegistrationException {
    private final List<String> missing;

    public PrerequisiteNotMetException(String courseCode, List<String> missing) {
        super("Cannot register for " + courseCode + ". Missing prerequisites: " + String.join(", ", missing));
        this.missing = List.copyOf(missing);
    }

    public List<String> getMissing() { return missing; }
}
