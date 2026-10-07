package com.crs.fake;

import com.crs.dao.StudentDAO;
import com.crs.model.Student;
import java.util.*;

public class InMemoryStudentDAO implements StudentDAO {
    private final FakeDataStore db;
    public InMemoryStudentDAO(FakeDataStore db) { this.db = db; }

    @Override public Optional<Student> findById(String id) { return Optional.ofNullable(db.students.get(id)); }
    @Override public List<Student> findAll() { return new ArrayList<>(db.students.values()); }
    @Override public void save(Student s) { db.students.put(s.getId(), s); }
    @Override public List<String> findCompletedCourses(String id) {
        return new ArrayList<>(db.completedCourses.getOrDefault(id, List.of()));
    }
}
