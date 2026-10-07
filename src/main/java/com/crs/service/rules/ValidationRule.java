package com.crs.service.rules;

import com.crs.exception.RegistrationException;
import com.crs.model.Course;
import com.crs.model.Student;

/**
 * Base class for registration validation rules (Strategy pattern).
 * Each subclass checks one condition and throws a RegistrationException if it fails.
 * RegistrationServiceImpl runs them in order: Duplicate → Prerequisite → TimeConflict → SeatAvailability.
 */
public abstract class ValidationRule {

    /**
     * Checks whether the student is allowed to register for the course.
     * @param student  the student trying to register
     * @param course   the course they want
     * @throws RegistrationException if the rule is violated
     */
    public abstract void validate(Student student, Course course) throws RegistrationException;
}
