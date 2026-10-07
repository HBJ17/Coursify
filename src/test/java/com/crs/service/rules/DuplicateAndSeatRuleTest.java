package com.crs.service.rules;

import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryRegistrationDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateAndSeatRuleTest {

    private FakeDataStore db;
    private InMemoryStudentDAO studentDAO;
    private InMemoryCourseDAO courseDAO;
    private InMemoryRegistrationDAO registrationDAO;

    @BeforeEach
    void setUp() {
        db = FakeDataStore.withSampleData();
        studentDAO = new InMemoryStudentDAO(db);
        courseDAO = new InMemoryCourseDAO(db);
        registrationDAO = new InMemoryRegistrationDAO(db);
    }

    @Test
    void testDuplicateRule_ThrowsExceptionWhenAlreadyRegistered() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course c1 = courseDAO.findByCode("CS101").orElseThrow();

        DuplicateRule rule = new DuplicateRule(registrationDAO);

        // Register s1 for CS101 first
        registrationDAO.registerAtomic(new Registration("S001", "CS101"));

        // Validating again for CS101 should throw DuplicateRegistrationException
        DuplicateRegistrationException ex = assertThrows(DuplicateRegistrationException.class,
                () -> rule.validate(s1, c1));
        assertTrue(ex.getMessage().contains("CS101"));
    }

    @Test
    void testDuplicateRule_PassesWhenNotRegistered() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course c1 = courseDAO.findByCode("CS101").orElseThrow();

        DuplicateRule rule = new DuplicateRule(registrationDAO);

        assertDoesNotThrow(() -> rule.validate(s1, c1));
    }

    @Test
    void testSeatAvailabilityRule_ThrowsExceptionWhenCourseFull() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course c202 = courseDAO.findByCode("CS202").orElseThrow(); // Capacity 1

        SeatAvailabilityRule rule = new SeatAvailabilityRule();

        // Fill CS202
        registrationDAO.registerAtomic(new Registration("S002", "CS202"));

        // Now seatsLeft is 0, so CS202 is full
        CourseFullException ex = assertThrows(CourseFullException.class,
                () -> rule.validate(s1, c202));
        assertEquals("CS202", ex.getCourseCode());
    }

    @Test
    void testSeatAvailabilityRule_PassesWhenSeatAvailable() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course c101 = courseDAO.findByCode("CS101").orElseThrow(); // Capacity 3

        SeatAvailabilityRule rule = new SeatAvailabilityRule();

        assertDoesNotThrow(() -> rule.validate(s1, c101));
    }
}
