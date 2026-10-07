package com.crs.service.registration;

import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.exception.TimeConflictException;
import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryRegistrationDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.fake.NoOpWaitlistService;
import com.crs.fake.SimpleRegistrationSubject;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.WaitlistEntry;
import com.crs.observer.RegistrationEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceImplTest {

    private FakeDataStore db;
    private InMemoryStudentDAO studentDAO;
    private InMemoryCourseDAO courseDAO;
    private InMemoryRegistrationDAO registrationDAO;
    private SimpleRegistrationSubject subject;
    private List<RegistrationEvent> publishedEvents;
    private RecordingWaitlistService waitlistService;
    private RegistrationServiceImpl service;

    /**
     * Inner recording WaitlistService to test waitlist promotion on cancel.
     */
    private static class RecordingWaitlistService extends NoOpWaitlistService {
        private final List<String> promotedCourses = new ArrayList<>();

        @Override
        public Optional<WaitlistEntry> promoteNext(String courseCode) {
            promotedCourses.add(courseCode);
            return Optional.empty();
        }

        public List<String> getPromotedCourses() {
            return promotedCourses;
        }
    }

    @BeforeEach
    void setUp() {
        db = FakeDataStore.withSampleData();
        studentDAO = new InMemoryStudentDAO(db);
        courseDAO = new InMemoryCourseDAO(db);
        registrationDAO = new InMemoryRegistrationDAO(db);
        subject = new SimpleRegistrationSubject();
        publishedEvents = new ArrayList<>();
        subject.addListener(publishedEvents::add);

        waitlistService = new RecordingWaitlistService();
        service = new RegistrationServiceImpl(studentDAO, courseDAO, registrationDAO, waitlistService, subject);
    }

    @Test
    void testRegister_SuccessLowersSeatsAndPublishesEvent() throws Exception {
        Course cs101 = courseDAO.findByCode("CS101").orElseThrow();
        int initialSeats = cs101.getSeatsLeft();

        Registration reg = service.register("S001", "CS101");

        assertNotNull(reg);
        assertEquals("S001", reg.getStudentId());
        assertEquals("CS101", reg.getCourseCode());
        assertEquals(initialSeats - 1, cs101.getSeatsLeft());

        // Verify event published
        assertEquals(1, publishedEvents.size());
        RegistrationEvent event = publishedEvents.get(0);
        assertEquals(RegistrationEvent.Type.REGISTERED, event.getType());
        assertEquals("S001", event.getStudentId());
        assertEquals("CS101", event.getCourseCode());
    }

    @Test
    void testRegister_DuplicateThrowsException() throws Exception {
        service.register("S001", "CS101");

        assertThrows(DuplicateRegistrationException.class,
                () -> service.register("S001", "CS101"));
    }

    @Test
    void testRegister_PrerequisiteNotMetThrowsException() {
        // S001 has not completed CS101, so registering for CS201 fails
        assertThrows(PrerequisiteNotMetException.class,
                () -> service.register("S001", "CS201"));
    }

    @Test
    void testRegister_TimeConflictThrowsException() throws Exception {
        service.register("S001", "CS101"); // Mon 9:00-10:00

        // CS102 is Mon 9:30-10:30, which clashes
        assertThrows(TimeConflictException.class,
                () -> service.register("S001", "CS102"));
    }

    @Test
    void testRegister_FullCourseThrowsCourseFullException() throws Exception {
        // CS202 capacity is 1
        service.register("S001", "CS202");

        // Next student trying to register gets CourseFullException
        assertThrows(CourseFullException.class,
                () -> service.register("S003", "CS202"));
    }

    @Test
    void testCancel_RestoresSeatPublishesEventAndPromotesNext() throws Exception {
        Course cs101 = courseDAO.findByCode("CS101").orElseThrow();
        int initialSeats = cs101.getSeatsLeft();

        service.register("S001", "CS101");
        assertEquals(initialSeats - 1, cs101.getSeatsLeft());

        publishedEvents.clear();

        service.cancel("S001", "CS101");

        // Seats restored
        assertEquals(initialSeats, cs101.getSeatsLeft());

        // Event published
        assertEquals(1, publishedEvents.size());
        RegistrationEvent event = publishedEvents.get(0);
        assertEquals(RegistrationEvent.Type.CANCELLED, event.getType());
        assertEquals("S001", event.getStudentId());

        // Waitlist promoteNext called
        assertTrue(waitlistService.getPromotedCourses().contains("CS101"));
    }

    @Test
    void testGetMyCoursesAndGetAllActiveRegistrations() throws Exception {
        service.register("S001", "CS101");

        List<Course> myCourses = service.getMyCourses("S001");
        assertEquals(1, myCourses.size());
        assertEquals("CS101", myCourses.get(0).getCode());

        List<Registration> allActive = service.getAllActiveRegistrations();
        assertEquals(1, allActive.size());
    }
}
