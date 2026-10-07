package com.crs.dao.impl;

import com.crs.dao.CourseDAO;
import com.crs.model.Course;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** STUB. Member 1 replaces the method bodies with Oracle JDBC code (keep the class name and no-arg constructor). */
public class CourseDAOImpl implements CourseDAO {
    public CourseDAOImpl() { }

    @Override public Optional<Course> findByCode(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Course> findAll() { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void save(Course course) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void update(Course course) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void delete(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public Map<String, List<String>> loadPrerequisiteGraph() { throw new UnsupportedOperationException("TODO Member 1"); }
}
