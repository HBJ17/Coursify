package com.crs.service.rules;

import com.crs.dao.RegistrationDAO;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.model.Course;
import com.crs.model.Student;

import java.util.HashSet;
import java.util.Set;

/**
 * Prevents a student from registering for a course they are already actively registered in.
 * Uses a HashSet of active course codes for O(1) lookup.
 */
public class DuplicateRule extends ValidationRule {
    private final RegistrationDAO registrationDAO;

    public DuplicateRule(RegistrationDAO registrationDAO) {
        this.registrationDAO = registrationDAO;
    }

    @Override
    public void validate(Student student, Course course) throws DuplicateRegistrationException {
        // Collect all active course codes into a HashSet for fast lookup
        Set<String> activeCodes = new HashSet<>();
        registrationDAO.findActiveByStudent(student.getId())
                .forEach(r -> activeCodes.add(r.getCourseCode()));

        if (activeCodes.contains(course.getCode())) {
            throw new DuplicateRegistrationException(course.getCode());
        }
    }
}
