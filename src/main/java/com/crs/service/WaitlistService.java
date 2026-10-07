package com.crs.service;

import com.crs.model.WaitlistEntry;
import java.util.List;
import java.util.Optional;

/** CONTRACT. Real version: Member 3 (service.waitlist.WaitlistServiceImpl, uses PriorityQueue). */
public interface WaitlistService {
    /** Adds the student to the course's waitlist and publishes a WAITLISTED event. */
    void join(String studentId, String courseCode);

    void leave(String studentId, String courseCode);

    /**
     * Takes the highest-priority student off the waitlist, registers them directly
     * through RegistrationDAO.registerAtomic(), and publishes a PROMOTED event.
     * Returns empty if nobody is waiting.
     */
    Optional<WaitlistEntry> promoteNext(String courseCode);

    /** 1 = next in line. Returns -1 if the student is not on this waitlist. */
    int positionOf(String studentId, String courseCode);

    /** In priority order (first element = next to be promoted). */
    List<WaitlistEntry> getWaitlist(String courseCode);

    List<WaitlistEntry> getMyWaitlists(String studentId);
}
