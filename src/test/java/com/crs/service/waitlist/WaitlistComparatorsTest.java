package com.crs.service.waitlist;

import com.crs.model.WaitlistEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WaitlistComparatorsTest {

    @Test
    void testByJoinTime() {
        LocalDateTime now = LocalDateTime.now();
        WaitlistEntry e1 = new WaitlistEntry(1, "S001", "CS101", 8.0, now.minusMinutes(10));
        WaitlistEntry e2 = new WaitlistEntry(2, "S002", "CS101", 9.0, now);

        assertTrue(WaitlistComparators.BY_JOIN_TIME.compare(e1, e2) < 0);
        assertTrue(WaitlistComparators.BY_JOIN_TIME.compare(e2, e1) > 0);
        assertTrue(WaitlistComparators.BY_JOIN_TIME.compare(e1, e1) == 0);
    }

    @Test
    void testByCgpaThenTime() {
        LocalDateTime now = LocalDateTime.now();
        WaitlistEntry e1 = new WaitlistEntry(1, "S001", "CS101", 9.5, now.minusMinutes(10));
        WaitlistEntry e2 = new WaitlistEntry(2, "S002", "CS101", 8.0, now);
        WaitlistEntry e3 = new WaitlistEntry(3, "S003", "CS101", 9.5, now); // Same CGPA, later time
        WaitlistEntry e4 = new WaitlistEntry(4, "A001", "CS101", 9.5, now); // Same CGPA, same time, different ID

        // Higher CGPA comes first
        assertTrue(WaitlistComparators.BY_CGPA_THEN_TIME.compare(e1, e2) < 0);
        
        // Same CGPA, earlier time comes first
        assertTrue(WaitlistComparators.BY_CGPA_THEN_TIME.compare(e1, e3) < 0);
        
        // Same CGPA, same time, ID ascending
        assertTrue(WaitlistComparators.BY_CGPA_THEN_TIME.compare(e4, e3) < 0);
    }
}
