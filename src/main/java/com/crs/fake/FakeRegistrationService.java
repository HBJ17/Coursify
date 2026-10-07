package com.crs.fake;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationSubject;
import com.crs.service.RegistrationService;
import java.util.List;
import java.util.Optional;

/**
 * Simplified version: only checks "already registered" and "course full".
 * No prerequisite or time-clash checks (that's Member 2's real job). No waitlist promotion.
 */
public class FakeRegistrationService implements RegistrationService {
    private final CourseDAO courses;
    private final RegistrationDAO registrations;
    private final RegistrationSubject subject;

    public FakeRegistrationService(CourseDAO courses, RegistrationDAO registrations, RegistrationSubject subject) {
        this.courses = courses;
        this.registrations = registrations;
        this.subject = subject;
    }

    @Override
    public Registration register(String studentId, String code) throws CourseFullException, DuplicateRegistrationException {
        boolean already = registrations.findActiveByStudent(studentId).stream().anyMatch(r -> r.getCourseCode().equals(code));
        if (already) throw new DuplicateRegistrationException(code);
        Course c = courses.findByCode(code).orElseThrow(() -> new IllegalArgumentException("No such course: " + code));
        if (c.isFull()) throw new CourseFullException(code);

        Registration r = new Registration(studentId, code);
        registrations.registerAtomic(r);
        subject.publish(new RegistrationEvent(RegistrationEvent.Type.REGISTERED, studentId, code));
        return r;
    }

    @Override
    public void cancel(String studentId, String code) {
        registrations.cancelAtomic(studentId, code);
        subject.publish(new RegistrationEvent(RegistrationEvent.Type.CANCELLED, studentId, code));
    }

    @Override public List<Course> getMyCourses(String studentId) {
        return registrations.findActiveByStudent(studentId).stream()
                .map(r -> courses.findByCode(r.getCourseCode())).flatMap(Optional::stream).toList();
    }

    @Override public List<Registration> getAllActiveRegistrations() { return registrations.findAllActive(); }
}
