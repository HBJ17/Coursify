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
import java.util.Optional;

/** STUB. Member 3 replaces the method bodies (keep the constructor signature). One PriorityQueue per course. */
public class WaitlistServiceImpl implements WaitlistService {
    private final WaitlistDAO waitlistDAO;
    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final RegistrationDAO registrationDAO;
    private final RegistrationSubject subject;
    private final Comparator<WaitlistEntry> priority;

    public WaitlistServiceImpl(WaitlistDAO waitlistDAO, StudentDAO studentDAO, CourseDAO courseDAO,
                               RegistrationDAO registrationDAO, RegistrationSubject subject,
                               Comparator<WaitlistEntry> priority) {
        this.waitlistDAO = waitlistDAO;
        this.studentDAO = studentDAO;
        this.courseDAO = courseDAO;
        this.registrationDAO = registrationDAO;
        this.subject = subject;
        this.priority = priority;
    }

    @Override public void join(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public void leave(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public Optional<WaitlistEntry> promoteNext(String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public int positionOf(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public List<WaitlistEntry> getWaitlist(String courseCode) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public List<WaitlistEntry> getMyWaitlists(String studentId) { throw new UnsupportedOperationException("TODO Member 3"); }
}
