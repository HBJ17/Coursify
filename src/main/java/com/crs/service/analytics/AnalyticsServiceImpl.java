package com.crs.service.analytics;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.dao.WaitlistDAO;
import com.crs.model.CourseDemand;
import com.crs.service.AnalyticsService;
import java.util.List;

/** STUB. Member 3 replaces the method body (keep the constructor signature). Max-heap top-k. */
public class AnalyticsServiceImpl implements AnalyticsService {
    private final CourseDAO courseDAO;
    private final RegistrationDAO registrationDAO;
    private final WaitlistDAO waitlistDAO;

    public AnalyticsServiceImpl(CourseDAO courseDAO, RegistrationDAO registrationDAO, WaitlistDAO waitlistDAO) {
        this.courseDAO = courseDAO;
        this.registrationDAO = registrationDAO;
        this.waitlistDAO = waitlistDAO;
    }

    @Override public List<CourseDemand> topInDemand(int k) { throw new UnsupportedOperationException("TODO Member 3"); }
}
