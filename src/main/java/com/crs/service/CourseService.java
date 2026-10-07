package com.crs.service;

import com.crs.model.Course;
import java.util.List;
import java.util.Optional;

/** CONTRACT. Real version: Member 1 (service.catalog.CourseServiceImpl, uses HashMap + TreeMap). */
public interface CourseService {
    /** All courses sorted by course code. */
    List<Course> getAllSorted();
    Optional<Course> findByCode(String courseCode);
    /** Matches code or title, case-insensitive. Empty query returns all courses. */
    List<Course> search(String query);
    /** Admin only. */
    void addCourse(Course course);
    /** Admin only. */
    void removeCourse(String courseCode);
}
