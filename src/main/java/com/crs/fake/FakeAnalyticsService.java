package com.crs.fake;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.dao.WaitlistDAO;
import com.crs.model.Course;
import com.crs.model.CourseDemand;
import com.crs.service.AnalyticsService;
import java.util.Comparator;
import java.util.List;

/** Simplified: sorts the whole list. Member 3's real version uses a max-heap. */
public class FakeAnalyticsService implements AnalyticsService {
    private final CourseDAO courses;
    private final RegistrationDAO registrations;
    private final WaitlistDAO waitlist;

    public FakeAnalyticsService(CourseDAO courses, RegistrationDAO registrations, WaitlistDAO waitlist) {
        this.courses = courses;
        this.registrations = registrations;
        this.waitlist = waitlist;
    }

    @Override
    public List<CourseDemand> topInDemand(int k) {
        return courses.findAll().stream()
                .map(this::toDemand)
                .sorted(Comparator.comparingDouble(CourseDemand::getDemandScore).reversed())
                .limit(k).toList();
    }

    private CourseDemand toDemand(Course c) {
        return new CourseDemand(c.getCode(), c.getTitle(), c.getCapacity(),
                registrations.findActiveByCourse(c.getCode()).size(),
                waitlist.findByCourse(c.getCode()).size());
    }
}
