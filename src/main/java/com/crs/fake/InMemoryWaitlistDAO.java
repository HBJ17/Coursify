package com.crs.fake;

import com.crs.dao.WaitlistDAO;
import com.crs.model.WaitlistEntry;
import java.util.ArrayList;
import java.util.List;

public class InMemoryWaitlistDAO implements WaitlistDAO {
    private final FakeDataStore db;
    public InMemoryWaitlistDAO(FakeDataStore db) { this.db = db; }

    @Override public void add(WaitlistEntry e) { e.setWaitlistId(db.nextWaitlistId++); db.waitlist.add(e); }
    @Override public void remove(String studentId, String courseCode) {
        db.waitlist.removeIf(e -> e.getStudentId().equals(studentId) && e.getCourseCode().equals(courseCode));
    }
    @Override public List<WaitlistEntry> findByCourse(String code) {
        return db.waitlist.stream().filter(e -> e.getCourseCode().equals(code)).toList();
    }
    @Override public List<WaitlistEntry> findByStudent(String id) {
        return db.waitlist.stream().filter(e -> e.getStudentId().equals(id)).toList();
    }
    @Override public List<WaitlistEntry> findAll() { return new ArrayList<>(db.waitlist); }
}
