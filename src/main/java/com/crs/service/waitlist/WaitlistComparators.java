package com.crs.service.waitlist;

import com.crs.model.WaitlistEntry;
import java.util.Comparator;

/** STUB. Member 3 fills these in (keep the two field names). */
public final class WaitlistComparators {
    private WaitlistComparators() { }

    /** Earliest join time first. */
    public static final Comparator<WaitlistEntry> BY_JOIN_TIME = (a, b) -> 0;       // TODO Member 3

    /** Highest CGPA first; ties: earlier join time, then studentId. */
    public static final Comparator<WaitlistEntry> BY_CGPA_THEN_TIME = (a, b) -> 0;  // TODO Member 3
}
