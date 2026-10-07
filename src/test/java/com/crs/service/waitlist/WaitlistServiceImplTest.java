package com.crs.service.waitlist;

import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryRegistrationDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.fake.InMemoryWaitlistDAO;
import com.crs.model.Registration;
import com.crs.model.WaitlistEntry;
import com.crs.observer.impl.RegistrationSubjectImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WaitlistServiceImplTest {
    private InMemoryWaitlistDAO waitlistDAO;
    private InMemoryStudentDAO studentDAO;
    private InMemoryCourseDAO courseDAO;
    private InMemoryRegistrationDAO registrationDAO;
    private RegistrationSubjectImpl subject;
    private WaitlistServiceImpl service;

    @BeforeEach
    void setUp() {
        FakeDataStore db = FakeDataStore.withSampleData();
        waitlistDAO = new InMemoryWaitlistDAO(db);
        studentDAO = new InMemoryStudentDAO(db);
        courseDAO = new InMemoryCourseDAO(db);
        registrationDAO = new InMemoryRegistrationDAO(db);
        subject = new RegistrationSubjectImpl(false);
        service = new WaitlistServiceImpl(waitlistDAO, studentDAO, courseDAO, registrationDAO, subject, WaitlistComparators.BY_CGPA_THEN_TIME);
    }

    @Test
    void testJoinPositionAndPromotion() {
        registrationDAO.registerAtomic(new Registration("S001", "CS202"));

        service.join("S003", "CS202");
        service.join("S002", "CS202");

        assertEquals(1, service.positionOf("S002", "CS202"));
        assertEquals(2, service.positionOf("S003", "CS202"));

        registrationDAO.cancelAtomic("S001", "CS202");

        Optional<WaitlistEntry> promoted = service.promoteNext("CS202");
        assertTrue(promoted.isPresent());
        assertEquals("S002", promoted.get().getStudentId());

        assertTrue(registrationDAO.findActiveByStudent("S002").stream().anyMatch(r -> r.getCourseCode().equals("CS202")));
    }

    @Test
    void testJoinErrors() {
        assertThrows(IllegalStateException.class, () -> service.join("S001", "CS201"));

        registrationDAO.registerAtomic(new Registration("S001", "CS202"));

        service.join("S002", "CS202");
        assertThrows(IllegalStateException.class, () -> service.join("S002", "CS202"));
    }
}
