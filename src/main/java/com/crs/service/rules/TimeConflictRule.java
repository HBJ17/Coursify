package com.crs.service.rules;

import com.crs.dao.RegistrationDAO;
import com.crs.exception.TimeConflictException;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.Student;
import com.crs.model.TimeSlot;
import com.crs.dao.CourseDAO;

import java.util.List;

/**
 * Checks that the new course's time slot does not overlap with any
 * course the student is already registered for.
 * Overlap = same day AND a.start < b.end AND b.start < a.end.
 * A null TimeSlot never clashes (the course has no fixed meeting time).
 */
public class TimeConflictRule extends ValidationRule {
    private final RegistrationDAO registrationDAO;
    private final CourseDAO courseDAO;

    public TimeConflictRule(RegistrationDAO registrationDAO, CourseDAO courseDAO) {
        this.registrationDAO = registrationDAO;
        this.courseDAO = courseDAO;
    }

    @Override
    public void validate(Student student, Course course) throws TimeConflictException {
        TimeSlot newSlot = course.getTimeSlot();
        // A null time slot never clashes
        if (newSlot == null) return;

        List<Registration> active = registrationDAO.findActiveByStudent(student.getId());
        for (Registration reg : active) {
            Course existing = courseDAO.findByCode(reg.getCourseCode()).orElse(null);
            if (existing == null) continue;

            TimeSlot existingSlot = existing.getTimeSlot();
            if (existingSlot == null) continue;

            // Overlap: same day AND a.start < b.end AND b.start < a.end
            if (newSlot.getDay() == existingSlot.getDay()
                    && newSlot.getStart().isBefore(existingSlot.getEnd())
                    && existingSlot.getStart().isBefore(newSlot.getEnd())) {
                throw new TimeConflictException(course.getCode(), existing.getCode());
            }
        }
    }
}
