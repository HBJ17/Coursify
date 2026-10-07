package com.crs.dao;

import com.crs.model.Student;
import java.util.List;
import java.util.Optional;

/** CONTRACT. Real version: Member 1 (dao.impl.StudentDAOImpl). Do not change without team approval. */
public interface StudentDAO {
    Optional<Student> findById(String studentId);
    List<Student> findAll();
    void save(Student student);
    /** Course codes this student has already passed (used for prerequisite checks). */
    List<String> findCompletedCourses(String studentId);
}
