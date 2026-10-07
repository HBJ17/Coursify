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

    @Override public List<CourseDemand> topInDemand(int k) {
        if (k <= 0) return List.of();
        
        List<com.crs.model.Course> allCourses = courseDAO.findAll();
        
        java.util.PriorityQueue<CourseDemand> heap = new java.util.PriorityQueue<>(
            java.util.Comparator.comparingDouble(CourseDemand::getDemandScore).reversed()
            .thenComparing(CourseDemand::getCourseCode)
        );
        
        for (com.crs.model.Course c : allCourses) {
            int registered = registrationDAO.findActiveByCourse(c.getCode()).size();
            int waitlisted = waitlistDAO.findByCourse(c.getCode()).size();
            heap.add(new CourseDemand(c.getCode(), c.getTitle(), c.getCapacity(), registered, waitlisted));
        }
        
        List<CourseDemand> result = new java.util.ArrayList<>();
        int count = Math.min(k, heap.size());
        for (int i = 0; i < count; i++) {
            result.add(heap.poll());
        }
        
        return result;
    }
}
