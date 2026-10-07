package com.crs.service.registration;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.exception.RegistrationException;
import com.crs.exception.TimeConflictException;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.Student;
import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationSubject;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import com.crs.service.rules.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RegistrationService (Member 2).
 * Runs the ValidationRule chain in order: Duplicate -> Prerequisite -> TimeConflict -> SeatAvailability.
 */
public class RegistrationServiceImpl implements RegistrationService {
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final RegistrationDAO registrationDAO;
    private final WaitlistService waitlistService;
    private final RegistrationSubject subject;
    private final List<ValidationRule> rules;

    public RegistrationServiceImpl(StudentDAO studentDAO, CourseDAO courseDAO, RegistrationDAO registrationDAO,
                                   WaitlistService waitlistService, RegistrationSubject subject) {
        this.studentDAO = studentDAO;
        this.courseDAO = courseDAO;
        this.registrationDAO = registrationDAO;
        this.waitlistService = waitlistService;
        this.subject = subject;

        // Build validation rule chain in order: Duplicate, Prerequisite, TimeConflict, SeatAvailability
        this.rules = new ArrayList<>();
        this.rules.add(new DuplicateRule(registrationDAO));
        this.rules.add(new PrerequisiteRule(courseDAO, studentDAO));
        this.rules.add(new TimeConflictRule(registrationDAO, courseDAO));
        this.rules.add(new SeatAvailabilityRule());
    }

    @Override
    public Registration register(String studentId, String courseCode)
            throws CourseFullException, DuplicateRegistrationException,
                   PrerequisiteNotMetException, TimeConflictException {

        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown student: " + studentId));
        Course course = courseDAO.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown course: " + courseCode));

        // Execute rule chain
        for (ValidationRule rule : rules) {
            try {
                rule.validate(student, course);
            } catch (CourseFullException | DuplicateRegistrationException |
                     PrerequisiteNotMetException | TimeConflictException e) {
                throw e;
            } catch (RegistrationException e) {
                throw new IllegalStateException("Unexpected registration exception", e);
            }
        }

        Registration reg = new Registration(studentId, courseCode);
        registrationDAO.registerAtomic(reg);
        subject.publish(new RegistrationEvent(RegistrationEvent.Type.REGISTERED, studentId, courseCode));
        return reg;
    }

    @Override
    public void cancel(String studentId, String courseCode) {
        throw new UnsupportedOperationException("TODO Member 2");
    }

    @Override
    public List<Course> getMyCourses(String studentId) {
        throw new UnsupportedOperationException("TODO Member 2");
    }

    @Override
    public List<Registration> getAllActiveRegistrations() {
        throw new UnsupportedOperationException("TODO Member 2");
    }
}
