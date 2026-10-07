package com.crs.service.analytics;

import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.fake.InMemoryRegistrationDAO;
import com.crs.fake.InMemoryWaitlistDAO;
import com.crs.model.CourseDemand;
import com.crs.model.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalyticsServiceImplTest {
    private InMemoryCourseDAO courseDAO;
    private InMemoryRegistrationDAO registrationDAO;
    private InMemoryWaitlistDAO waitlistDAO;
    private AnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        FakeDataStore db = FakeDataStore.withSampleData();
        courseDAO = new InMemoryCourseDAO(db);
        registrationDAO = new InMemoryRegistrationDAO(db);
        waitlistDAO = new InMemoryWaitlistDAO(db);
        service = new AnalyticsServiceImpl(courseDAO, registrationDAO, waitlistDAO);
    }

    @Test
    void testTopInDemand() {
        registrationDAO.registerAtomic(new Registration("S001", "CS202"));
        registrationDAO.registerAtomic(new Registration("S001", "CS101"));
        registrationDAO.registerAtomic(new Registration("S002", "CS101"));

        List<CourseDemand> top = service.topInDemand(2);
        assertEquals(2, top.size());
        assertEquals("CS202", top.get(0).getCourseCode());
        assertEquals("CS101", top.get(1).getCourseCode());
    }

    @Test
    void testTopInDemandInvalidK() {
        assertTrue(service.topInDemand(0).isEmpty());
        assertTrue(service.topInDemand(-1).isEmpty());
    }
}
