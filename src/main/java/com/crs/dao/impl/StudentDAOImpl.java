package com.crs.dao.impl;

import com.crs.dao.StudentDAO;
import com.crs.model.Student;
import java.util.List;
import java.util.Optional;

/** STUB. Member 1 replaces the method bodies with Oracle JDBC code (keep the class name and no-arg constructor). */
public class StudentDAOImpl implements StudentDAO {
    public StudentDAOImpl() { }

    @Override public Optional<Student> findById(String studentId) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Student> findAll() { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void save(Student student) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<String> findCompletedCourses(String studentId) { throw new UnsupportedOperationException("TODO Member 1"); }
}
