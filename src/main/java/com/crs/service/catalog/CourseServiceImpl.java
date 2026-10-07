package com.crs.service.catalog;

import com.crs.dao.CourseDAO;
import com.crs.model.Course;
import com.crs.service.CourseService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The course catalog.
 * HashMap  = fast lookup by course code (O(1)).
 * TreeMap  = keeps courses sorted by code automatically (O(log n) insert).
 * Both are rebuilt from the DAO on every read, so seat counts are never stale.
 */
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;

    public CourseServiceImpl(CourseDAO courseDAO) { this.courseDAO = courseDAO; }

    @Override
    public List<Course> getAllSorted() {
        return new ArrayList<>(buildSortedView().values());
    }

    @Override
    public Optional<Course> findByCode(String courseCode) {
        return Optional.ofNullable(buildLookup().get(courseCode));
    }

    /** Matches the code or the title, ignoring case. A blank query returns every course. */
    @Override
    public List<Course> search(String query) {
        if (query == null || query.isBlank()) return getAllSorted();
        String q = query.trim().toLowerCase();
        List<Course> matches = new ArrayList<>();
        for (Course c : buildSortedView().values()) {
            if (c.getCode().toLowerCase().contains(q) || c.getTitle().toLowerCase().contains(q)) matches.add(c);
        }
        return matches;
    }

    @Override
    public void addCourse(Course course) { courseDAO.save(course); }

    @Override
    public void removeCourse(String courseCode) { courseDAO.delete(courseCode); }

    private Map<String, Course> buildLookup() {
        Map<String, Course> byCode = new HashMap<>();
        for (Course c : courseDAO.findAll()) byCode.put(c.getCode(), c);
        return byCode;
    }

    private TreeMap<String, Course> buildSortedView() {
        return new TreeMap<>(buildLookup());
    }
}
