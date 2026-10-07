package com.crs.service.waitlist;

import com.crs.dao.CourseDAO;
import com.crs.dao.RegistrationDAO;
import com.crs.dao.StudentDAO;
import com.crs.dao.WaitlistDAO;
import com.crs.model.WaitlistEntry;
import com.crs.observer.RegistrationSubject;
import com.crs.service.WaitlistService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Optional;
import com.crs.model.Student;
import com.crs.model.Course;

/** STUB. Member 3 replaces the method bodies (keep the constructor signature). One PriorityQueue per course. */
public class WaitlistServiceImpl implements WaitlistService {
    private final WaitlistDAO waitlistDAO;
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final RegistrationDAO registrationDAO;
    private final RegistrationSubject subject;
    private final Comparator<WaitlistEntry> priority;
    private final Map<String, PriorityQueue<WaitlistEntry>> queues = new HashMap<>();

    public WaitlistServiceImpl(WaitlistDAO waitlistDAO, StudentDAO studentDAO, CourseDAO courseDAO,
                               RegistrationDAO registrationDAO, RegistrationSubject subject,
                               Comparator<WaitlistEntry> priority) {
        this.waitlistDAO = waitlistDAO;
        this.studentDAO = studentDAO;
        this.courseDAO = courseDAO;
        this.registrationDAO = registrationDAO;
        this.subject = subject;
        this.priority = priority;

        for (WaitlistEntry entry : waitlistDAO.findAll()) {
            queues.computeIfAbsent(entry.getCourseCode(), k -> new PriorityQueue<>(priority))
                  .add(entry);
        }
    }

    @Override public void join(String studentId, String courseCode) {
        Student student = studentDAO.findById(studentId).orElseThrow(() -> new IllegalStateException("Student not found"));
        Course course = courseDAO.findByCode(courseCode).orElseThrow(() -> new IllegalStateException("Course not found"));

        if (!course.isFull()) throw new IllegalStateException("Course is not full");

        boolean alreadyRegistered = registrationDAO.findActiveByStudent(studentId).stream()
                .anyMatch(r -> r.getCourseCode().equals(courseCode));
        if (alreadyRegistered) throw new IllegalStateException("Already registered");

        PriorityQueue<WaitlistEntry> q = queues.computeIfAbsent(courseCode, k -> new PriorityQueue<>(priority));
        if (q.stream().anyMatch(e -> e.getStudentId().equals(studentId))) {
            throw new IllegalStateException("Already on waitlist");
        }

        if (q.size() >= course.getWaitlistCap()) {
            throw new IllegalStateException("Waitlist is full");
        }

        WaitlistEntry entry = new WaitlistEntry(studentId, courseCode, student.getCgpa());
        waitlistDAO.add(entry);
        q.add(entry);

        subject.publish(new com.crs.observer.RegistrationEvent(
                com.crs.observer.RegistrationEvent.Type.WAITLISTED, studentId, courseCode));
    }
    
    @Override public void leave(String studentId, String courseCode) {
        PriorityQueue<WaitlistEntry> q = queues.get(courseCode);
        if (q != null) {
            q.removeIf(e -> e.getStudentId().equals(studentId));
        }
        waitlistDAO.remove(studentId, courseCode);
    }
    @Override public Optional<WaitlistEntry> promoteNext(String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public int positionOf(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public List<WaitlistEntry> getWaitlist(String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public List<WaitlistEntry> getMyWaitlists(String studentId) { throw new UnsupportedOperationException("TODO Member 3"); }
}
