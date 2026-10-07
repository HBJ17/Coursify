package com.crs.service.rules;

import com.crs.exception.TimeConflictException;
import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryRegistrationDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.Student;
import com.crs.model.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeConflictRuleTest {

    private FakeDataStore db;
    private InMemoryStudentDAO studentDAO;
    private InMemoryCourseDAO courseDAO;
    private InMemoryRegistrationDAO registrationDAO;
    private TimeConflictRule rule;

    @BeforeEach
    void setUp() {
        db = FakeDataStore.withSampleData();
        studentDAO = new InMemoryStudentDAO(db);
        courseDAO = new InMemoryCourseDAO(db);
        registrationDAO = new InMemoryRegistrationDAO(db);
        rule = new TimeConflictRule(registrationDAO, courseDAO);
    }

    @Test
    void testTimeConflict_CS101AndCS102Clash() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course cs102 = courseDAO.findByCode("CS102").orElseThrow(); // Mon 9:30-10:30

        // Student s1 registers for CS101 (Mon 9:00-10:00)
        registrationDAO.registerAtomic(new Registration("S001", "CS101"));

        // Registering for CS102 should clash with CS101
        TimeConflictException ex = assertThrows(TimeConflictException.class,
                () -> rule.validate(s1, cs102));
        assertTrue(ex.getMessage().contains("CS102") && ex.getMessage().contains("CS101"));
    }

    @Test
    void testTimeConflict_BackToBackSlotsDoNotClash() {
        Student s1 = studentDAO.findById("S001").orElseThrow();

        // Create course A: Mon 10:00 - 11:00
        Course courseA = Course.builder("TEST101", "Course A")
                .timeSlot(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)))
                .build();
        courseDAO.save(courseA);

        // Create course B: Mon 11:00 - 12:00
        Course courseB = Course.builder("TEST102", "Course B")
                .timeSlot(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(12, 0)))
                .build();
        courseDAO.save(courseB);

        // Student registers for course A
        registrationDAO.registerAtomic(new Registration("S001", "TEST101"));

        // Validating course B should pass because 11:00 is not before 11:00
        assertDoesNotThrow(() -> rule.validate(s1, courseB));
    }

    @Test
    void testTimeConflict_NullTimeSlotDoesNotClash() {
        Student s1 = studentDAO.findById("S001").orElseThrow();

        // Course with null TimeSlot
        Course onlineCourse = Course.builder("ONLINE1", "Online Course")
                .timeSlot(null)
                .build();
        courseDAO.save(onlineCourse);

        // Register for CS101 first
        registrationDAO.registerAtomic(new Registration("S001", "CS101"));

        // Validating online course should pass
        assertDoesNotThrow(() -> rule.validate(s1, onlineCourse));
    }

    @Test
    void testTimeConflict_DifferentDaysDoNotClash() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course cs201 = courseDAO.findByCode("CS201").orElseThrow(); // Tue 11:00-12:00

        // Register for CS101 (Mon 9:00-10:00)
        registrationDAO.registerAtomic(new Registration("S001", "CS101"));

        // Validating CS201 (Tuesday) should pass
        assertDoesNotThrow(() -> rule.validate(s1, cs201));
    }
}
