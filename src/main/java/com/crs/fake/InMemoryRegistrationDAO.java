package com.crs.fake;

import com.crs.dao.RegistrationDAO;
import com.crs.model.Course;
import com.crs.model.Registration;
import com.crs.model.RegistrationStatus;
import java.util.List;

public class InMemoryRegistrationDAO implements RegistrationDAO {
    private final FakeDataStore db;
    public InMemoryRegistrationDAO(FakeDataStore db) { this.db = db; }

    @Override
    public void registerAtomic(Registration r) {
        Course c = db.courses.get(r.getCourseCode());
        if (c == null) throw new IllegalArgumentException("No such course: " + r.getCourseCode());
        c.setSeatsLeft(c.getSeatsLeft() - 1);   // throws if already 0, like a DB CHECK constraint
        r.setRegId(db.nextRegId++);
        db.registrations.add(r);
    }

    @Override
    public void cancelAtomic(String studentId, String courseCode) {
        for (Registration r : db.registrations) {
            if (r.getStudentId().equals(studentId) && r.getCourseCode().equals(courseCode)
                    && r.getStatus() == RegistrationStatus.ACTIVE) {
                r.setStatus(RegistrationStatus.CANCELLED);
                Course c = db.courses.get(courseCode);
                if (c != null) c.setSeatsLeft(c.getSeatsLeft() + 1);
                return;
            }
        }
    }

    @Override public List<Registration> findActiveByStudent(String studentId) {
        return db.registrations.stream()
                .filter(r -> r.getStudentId().equals(studentId) && r.getStatus() == RegistrationStatus.ACTIVE).toList();
    }

    @Override public List<Registration> findActiveByCourse(String courseCode) {
        return db.registrations.stream()
                .filter(r -> r.getCourseCode().equals(courseCode) && r.getStatus() == RegistrationStatus.ACTIVE).toList();
    }

    @Override public List<Registration> findAllActive() {
        return db.registrations.stream().filter(r -> r.getStatus() == RegistrationStatus.ACTIVE).toList();
    }
}
