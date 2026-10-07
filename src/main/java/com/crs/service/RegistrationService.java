package com.crs.service;

import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.exception.TimeConflictException;
import com.crs.model.Course;
import com.crs.model.Registration;
import java.util.List;

/** CONTRACT. Real version: Member 2 (service.registration.RegistrationServiceImpl). */
public interface RegistrationService {
    /**
     * Runs all validation rules, then saves the registration and publishes a REGISTERED event.
     * If the course is full, throws CourseFullException. The UI can then offer WaitlistService.join().
     */
    Registration register(String studentId, String courseCode)
            throws CourseFullException, DuplicateRegistrationException,
                   PrerequisiteNotMetException, TimeConflictException;

    /**
     * Cancels the registration, publishes a CANCELLED event,
     * then calls WaitlistService.promoteNext(courseCode) so the next student gets the seat.
     */
    void cancel(String studentId, String courseCode);

    List<Course> getMyCourses(String studentId);
    List<Registration> getAllActiveRegistrations(); // for the admin screen
}
