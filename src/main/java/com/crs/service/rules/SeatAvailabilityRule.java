package com.crs.service.rules;

import com.crs.exception.CourseFullException;
import com.crs.model.Course;
import com.crs.model.Student;

/**
 * Checks that the course still has at least one seat available.
 * Throws CourseFullException if seats are exhausted; the UI can then offer the waitlist.
 */
public class SeatAvailabilityRule extends ValidationRule {

    @Override
    public void validate(Student student, Course course) throws CourseFullException {
        if (course.isFull()) {
            throw new CourseFullException(course.getCode());
        }
    }
}
