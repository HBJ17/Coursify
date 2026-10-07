package com.crs.dao;

import com.crs.model.Course;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CONTRACT. Real version: Member 1 (dao.impl.CourseDAOImpl). */
public interface CourseDAO {
    Optional<Course> findByCode(String courseCode);
    List<Course> findAll();
    /** Insert a new course (and its prerequisites). */
    void save(Course course);
    /** Update an existing course's details. */
    void update(Course course);
    void delete(String courseCode);
    /** Whole prerequisite graph as an adjacency list: course -> list of its prerequisites. */
    Map<String, List<String>> loadPrerequisiteGraph();
}
