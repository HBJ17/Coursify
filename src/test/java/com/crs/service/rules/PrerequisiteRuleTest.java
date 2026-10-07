package com.crs.service.rules;

import com.crs.exception.PrerequisiteNotMetException;
import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.model.Course;
import com.crs.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrerequisiteRuleTest {

    private FakeDataStore db;
    private InMemoryStudentDAO studentDAO;
    private InMemoryCourseDAO courseDAO;
    private PrerequisiteRule rule;

    @BeforeEach
    void setUp() {
        db = FakeDataStore.withSampleData();
        studentDAO = new InMemoryStudentDAO(db);
        courseDAO = new InMemoryCourseDAO(db);
        rule = new PrerequisiteRule(courseDAO, studentDAO);
    }

    @Test
    void testPrerequisite_S001BlockedFromCS201() {
        // S001 has no completed courses
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course cs201 = courseDAO.findByCode("CS201").orElseThrow(); // needs CS101

        PrerequisiteNotMetException ex = assertThrows(PrerequisiteNotMetException.class,
                () -> rule.validate(s1, cs201));
        assertEquals(List.of("CS101"), ex.getMissing());
    }

    @Test
    void testPrerequisite_S002AllowedForCS201() {
        // S002 completed CS101 in FakeDataStore
        Student s2 = studentDAO.findById("S002").orElseThrow();
        Course cs201 = courseDAO.findByCode("CS201").orElseThrow(); // needs CS101

        assertDoesNotThrow(() -> rule.validate(s2, cs201));
    }

    @Test
    void testPrerequisite_S002BlockedFromCS301Chain() {
        // S002 completed CS101. CS301 requires CS102 and CS201 (which transitively requires CS101).
        // Since S002 has only completed CS101, missing courses are CS102 and CS201.
        Student s2 = studentDAO.findById("S002").orElseThrow();
        Course cs301 = courseDAO.findByCode("CS301").orElseThrow(); // needs CS102, CS201

        PrerequisiteNotMetException ex = assertThrows(PrerequisiteNotMetException.class,
                () -> rule.validate(s2, cs301));
        assertEquals(List.of("CS102", "CS201"), ex.getMissing());
    }

    @Test
    void testPrerequisite_NoPrerequisitesAllowed() {
        Student s1 = studentDAO.findById("S001").orElseThrow();
        Course cs101 = courseDAO.findByCode("CS101").orElseThrow(); // no prereqs

        assertDoesNotThrow(() -> rule.validate(s1, cs101));
    }
}
