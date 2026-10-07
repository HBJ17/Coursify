package com.crs.service.waitlist;

import com.crs.model.WaitlistEntry;
import java.util.Comparator;

/** STUB. Member 3 fills these in (keep the two field names). */
public final class WaitlistComparators {
    private WaitlistComparators() { }

    /** Earliest join time first. */
    public static final Comparator<WaitlistEntry> BY_JOIN_TIME = Comparator.comparing(WaitlistEntry::getJoinedTime);

    /** Highest CGPA first; ties: earlier join time, then studentId. */
    public static final Comparator<WaitlistEntry> BY_CGPA_THEN_TIME = (a, b) -> {
        int cmp = Double.compare(b.getCgpa(), a.getCgpa());
        if (cmp != 0) return cmp;
        cmp = a.getJoinedTime().compareTo(b.getJoinedTime());
        if (cmp != 0) return cmp;
        return a.getStudentId().compareTo(b.getStudentId());
    };
}
