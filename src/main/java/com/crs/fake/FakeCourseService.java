package com.crs.fake;

import com.crs.dao.CourseDAO;
import com.crs.model.Course;
import com.crs.service.CourseService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakeCourseService implements CourseService {
    private final CourseDAO courses;
    public FakeCourseService(CourseDAO courses) { this.courses = courses; }

    @Override public List<Course> getAllSorted() {
        return courses.findAll().stream().sorted(Comparator.comparing(Course::getCode)).toList();
    }
    @Override public Optional<Course> findByCode(String code) { return courses.findByCode(code); }
    @Override public List<Course> search(String q) {
        String query = q == null ? "" : q.toLowerCase();
        return getAllSorted().stream().filter(c -> c.getCode().toLowerCase().contains(query)
                || c.getTitle().toLowerCase().contains(query)).toList();
    }
    @Override public void addCourse(Course c) { courses.save(c); }
    @Override public void removeCourse(String code) { courses.delete(code); }
}
