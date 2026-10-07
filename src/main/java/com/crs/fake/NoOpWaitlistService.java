package com.crs.fake;

import com.crs.model.WaitlistEntry;
import com.crs.service.WaitlistService;
import java.util.List;
import java.util.Optional;

/** Does nothing. Member 2 can pass this in while testing cancel(), before Member 3 is done. */
public class NoOpWaitlistService implements WaitlistService {
    @Override public void join(String studentId, String courseCode) { }
    @Override public void leave(String studentId, String courseCode) { }
    @Override public Optional<WaitlistEntry> promoteNext(String courseCode) { return Optional.empty(); }
    @Override public int positionOf(String studentId, String courseCode) { return -1; }
    @Override public List<WaitlistEntry> getWaitlist(String courseCode) { return List.of(); }
    @Override public List<WaitlistEntry> getMyWaitlists(String studentId) { return List.of(); }
}
