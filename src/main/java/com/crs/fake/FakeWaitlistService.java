package com.crs.fake;

import com.crs.dao.StudentDAO;
import com.crs.dao.WaitlistDAO;
import com.crs.model.Student;
import com.crs.model.WaitlistEntry;
import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationSubject;
import com.crs.service.WaitlistService;
import java.util.List;
import java.util.Optional;

/** Simplified: plain first-come-first-served list, and promoteNext only removes (doesn't register). */
public class FakeWaitlistService implements WaitlistService {
    private final WaitlistDAO waitlist;
    private final StudentDAO students;
    private final RegistrationSubject subject;

    public FakeWaitlistService(WaitlistDAO waitlist, StudentDAO students, RegistrationSubject subject) {
        this.waitlist = waitlist;
        this.students = students;
        this.subject = subject;
    }

    @Override public void join(String studentId, String code) {
        if (positionOf(studentId, code) != -1) return;
        double cgpa = students.findById(studentId).map(Student::getCgpa).orElse(0.0);
        waitlist.add(new WaitlistEntry(studentId, code, cgpa));
        subject.publish(new RegistrationEvent(RegistrationEvent.Type.WAITLISTED, studentId, code));
    }

    @Override public void leave(String studentId, String code) { waitlist.remove(studentId, code); }

    @Override public Optional<WaitlistEntry> promoteNext(String code) {
        List<WaitlistEntry> list = waitlist.findByCourse(code);
        if (list.isEmpty()) return Optional.empty();
        WaitlistEntry next = list.get(0);
        waitlist.remove(next.getStudentId(), code);
        return Optional.of(next);
    }

    @Override public int positionOf(String studentId, String code) {
        List<WaitlistEntry> list = waitlist.findByCourse(code);
        for (int i = 0; i < list.size(); i++) if (list.get(i).getStudentId().equals(studentId)) return i + 1;
        return -1;
    }

    @Override public List<WaitlistEntry> getWaitlist(String code) { return waitlist.findByCourse(code); }
    @Override public List<WaitlistEntry> getMyWaitlists(String studentId) { return waitlist.findByStudent(studentId); }
}
