package com.crs.service.registration;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.exception.TimeConflictException;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.observer.RegistrationSubject;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import java.util.List;

/** STUB. Member 2 replaces the method bodies (keep the constructor signature). Runs the ValidationRule chain. */
public class RegistrationServiceImpl implements RegistrationService {
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final RegistrationDAO registrationDAO;
    private final WaitlistService waitlistService;
    private final RegistrationSubject subject;

    public RegistrationServiceImpl(StudentDAO studentDAO, CourseDAO courseDAO, RegistrationDAO registrationDAO,
                                   WaitlistService waitlistService, RegistrationSubject subject) {
        this.studentDAO = studentDAO;
        this.courseDAO = courseDAO;
        this.registrationDAO = registrationDAO;
        this.waitlistService = waitlistService;
        this.subject = subject;
    }

    @Override
    public Registration register(String studentId, String courseCode)
            throws CourseFullException, DuplicateRegistrationException,
                   PrerequisiteNotMetException, TimeConflictException {
        throw new UnsupportedOperationException("TODO Member 2");
    }

    @Override public void cancel(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 2"); }
    @Override public List<Course> getMyCourses(String studentId) { throw new UnsupportedOperationException("TODO Member 2"); }
    @Override public List<Registration> getAllActiveRegistrations() { throw new UnsupportedOperationException("TODO Member 2"); }
}
