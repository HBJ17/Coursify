package com.crs.dao.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.crs.exception.DatabaseOperationException;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.TimeSlot;
import com.crs.model.WaitlistEntry;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Talks to the real Oracle database, using the sample data from database/schema.sql.
 * Skipped automatically when src/main/resources/db.properties does not exist.
 * Every test works on its own temporary course TST99, deleted afterwards (ON DELETE CASCADE removes its rows).
 */
class OracleDaoTest {
    private static final String TEMP = "TST99";

    private final CourseDAOImpl courseDAO = new CourseDAOImpl();
    private final RegistrationDAOImpl registrationDAO = new RegistrationDAOImpl();
    private final WaitlistDAOImpl waitlistDAO = new WaitlistDAOImpl();
    private final StudentDAOImpl studentDAO = new StudentDAOImpl();

    @BeforeEach
    void setUp() {
        assumeTrue(getClass().getResource("/db.properties") != null, "db.properties missing: skipping Oracle tests");
        courseDAO.delete(TEMP); // in case an earlier run crashed
    }

    @AfterEach
    void cleanUp() {
        if (getClass().getResource("/db.properties") != null) courseDAO.delete(TEMP);
    }

    private Course tempCourse(int capacity) {
        return Course.builder(TEMP, "Temporary Test Course").credits(2).capacity(capacity).build();
    }

    @Test
    void registerAndCancelChangeSeatCount() {
        courseDAO.save(tempCourse(2));
        Registration reg = new Registration("S001", TEMP);

        registrationDAO.registerAtomic(reg);
        assertTrue(reg.getRegId() > 0, "Oracle should give the registration an ID");
        assertEquals(1, courseDAO.findByCode(TEMP).orElseThrow().getSeatsLeft());
        assertEquals(1, registrationDAO.findActiveByCourse(TEMP).size());
        assertEquals(1, registrationDAO.findActiveByStudent("S001").stream()
                .filter(r -> r.getCourseCode().equals(TEMP)).count());

        registrationDAO.cancelAtomic("S001", TEMP);
        assertEquals(2, courseDAO.findByCode(TEMP).orElseThrow().getSeatsLeft());
        assertTrue(registrationDAO.findActiveByCourse(TEMP).isEmpty());
    }

    @Test
    void registeringInAFullCourseFailsAndKeepsSeatsAtZero() {
        courseDAO.save(tempCourse(1));
        registrationDAO.registerAtomic(new Registration("S001", TEMP));

        DatabaseOperationException e = assertThrows(DatabaseOperationException.class,
                () -> registrationDAO.registerAtomic(new Registration("S002", TEMP)));
        assertTrue(e.getMessage().contains("No seats left"));
        assertEquals(0, courseDAO.findByCode(TEMP).orElseThrow().getSeatsLeft());
        assertEquals(1, registrationDAO.findActiveByCourse(TEMP).size()); // the failed one was rolled back
    }

    @Test
    void cancellingWithoutAnActiveRegistrationFails() {
        courseDAO.save(tempCourse(2));
        assertThrows(DatabaseOperationException.class, () -> registrationDAO.cancelAtomic("S001", TEMP));
        assertEquals(2, courseDAO.findByCode(TEMP).orElseThrow().getSeatsLeft()); // seat count not touched
    }

    @Test
    void prerequisiteGraphContainsCs301Chain() {
        var graph = courseDAO.loadPrerequisiteGraph();
        assertEquals(List.of("CS102", "CS201"), graph.get("CS301"));
        assertEquals(List.of("CS101"), graph.get("CS201"));
        assertTrue(graph.get("CS101").isEmpty());
    }

    @Test
    void courseReadMapsTimeSlotAndPrerequisites() {
        Course cs301 = courseDAO.findByCode("CS301").orElseThrow();
        assertEquals(new TimeSlot(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)), cs301.getTimeSlot());
        assertEquals(List.of("CS102", "CS201"), cs301.getPrerequisites());
        assertTrue(courseDAO.findByCode("NOPE").isEmpty());
        assertTrue(courseDAO.findAll().size() >= 5);
    }

    @Test
    void courseSaveUpdateDeleteRoundTrip() {
        Course c = Course.builder(TEMP, "Temp").credits(2).capacity(5)
                .timeSlot(new TimeSlot(DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
                .prerequisites(List.of("CS101")).build();
        courseDAO.save(c);
        Course loaded = courseDAO.findByCode(TEMP).orElseThrow();
        assertEquals(List.of("CS101"), loaded.getPrerequisites());
        assertEquals(DayOfWeek.FRIDAY, loaded.getTimeSlot().getDay());

        loaded.setTitle("Renamed");
        loaded.setTimeSlot(null);                       // no fixed slot: stored as NULL
        loaded.setPrerequisites(List.of("CS102"));
        courseDAO.update(loaded);
        Course updated = courseDAO.findByCode(TEMP).orElseThrow();
        assertEquals("Renamed", updated.getTitle());
        assertNull(updated.getTimeSlot());
        assertEquals(List.of("CS102"), updated.getPrerequisites());

        courseDAO.delete(TEMP);
        assertTrue(courseDAO.findByCode(TEMP).isEmpty());
    }

    @Test
    void waitlistEntriesCarryCgpaFromStudents() {
        courseDAO.save(tempCourse(1));
        WaitlistEntry entry = new WaitlistEntry("S002", TEMP, 0); // cgpa here is ignored: it comes from students
        waitlistDAO.add(entry);
        assertTrue(entry.getWaitlistId() > 0);

        List<WaitlistEntry> byCourse = waitlistDAO.findByCourse(TEMP);
        assertEquals(1, byCourse.size());
        assertEquals(9.2, byCourse.get(0).getCgpa(), 0.001);
        assertEquals(1, waitlistDAO.findByStudent("S002").stream()
                .filter(w -> w.getCourseCode().equals(TEMP)).count());

        waitlistDAO.remove("S002", TEMP);
        assertTrue(waitlistDAO.findByCourse(TEMP).isEmpty());
    }

    @Test
    void studentReadsAndCompletedCourses() {
        assertEquals("Arun Kumar", studentDAO.findById("S001").orElseThrow().getName());
        assertTrue(studentDAO.findById("S999").isEmpty());
        assertTrue(studentDAO.findAll().size() >= 3);
        assertEquals(List.of("CS101"), studentDAO.findCompletedCourses("S002"));
        assertTrue(studentDAO.findCompletedCourses("S001").isEmpty());
        assertTrue(new AdminDAOImpl().findById("A001").isPresent());
    }
}
