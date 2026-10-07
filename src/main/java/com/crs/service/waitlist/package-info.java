/**
 * OWNER: Member 3 (Waitlist & Analytics)
 *
 * Write here: WaitlistServiceImpl. One PriorityQueue<WaitlistEntry> per course, with
 * comparators for 'earliest first' and 'highest CGPA first'. promoteNext registers the student
 * via RegistrationDAO.registerAtomic and publishes PROMOTED. Load queues from WaitlistDAO at startup.
 */
package com.crs.service.waitlist;
