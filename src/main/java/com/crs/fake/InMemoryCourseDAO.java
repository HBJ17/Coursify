package com.crs.fake;

import com.crs.dao.CourseDAO;
import com.crs.model.Course;
import java.util.*;

public class InMemoryCourseDAO implements CourseDAO {
    private final FakeDataStore db;
    public InMemoryCourseDAO(FakeDataStore db) { this.db = db; }

    @Override public Optional<Course> findByCode(String code) { return Optional.ofNullable(db.courses.get(code)); }
    @Override public List<Course> findAll() { return new ArrayList<>(db.courses.values()); }
    @Override public void save(Course c) { db.courses.put(c.getCode(), c); }
    @Override public void update(Course c) { db.courses.put(c.getCode(), c); }
    @Override public void delete(String code) { db.courses.remove(code); }

    @Override public Map<String, List<String>> loadPrerequisiteGraph() {
        Map<String, List<String>> graph = new HashMap<>();
        for (Course c : db.courses.values()) graph.put(c.getCode(), new ArrayList<>(c.getPrerequisites()));
        return graph;
    }
}
