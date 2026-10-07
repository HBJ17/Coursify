package com.crs.service.catalog;

import com.crs.dao.CourseDAO;
import com.crs.model.Course;
import com.crs.service.CourseService;
import java.util.List;
import java.util.Optional;

/** STUB. Member 1 replaces the method bodies (keep the constructor signature). HashMap + TreeMap inside. */
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;

    public CourseServiceImpl(CourseDAO courseDAO) { this.courseDAO = courseDAO; }

    @Override public List<Course> getAllSorted() { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public Optional<Course> findByCode(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Course> search(String query) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void addCourse(Course course) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void removeCourse(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
}
