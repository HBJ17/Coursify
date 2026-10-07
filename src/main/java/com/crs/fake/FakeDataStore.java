package com.crs.fake;

import com.crs.model.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Pretend database kept in memory. All the InMemory*DAO classes share one of these,
 * so registering a student also changes the course's seat count, just like the real DB.
 *
 * The sample data matches database/schema.sql, so behaviour is the same before and after integration.
 * Sample logins: S001/pass, S002/pass, S003/pass (students), A001/admin (admin).
 */
public class FakeDataStore {
    final Map<String, Student> students = new LinkedHashMap<>();
    final Map<String, Admin> admins = new LinkedHashMap<>();
    final Map<String, Course> courses = new LinkedHashMap<>();
    final Map<String, List<String>> completedCourses = new HashMap<>();
    final List<Registration> registrations = new ArrayList<>();
    final List<WaitlistEntry> waitlist = new ArrayList<>();
    int nextRegId = 1;
    int nextWaitlistId = 1;

    /** Creates a store filled with the sample data. */
    public static FakeDataStore withSampleData() {
        FakeDataStore s = new FakeDataStore();

        s.students.put("S001", new Student("S001", "Arun Kumar", "arun@college.edu", "pass", 8.5));
        s.students.put("S002", new Student("S002", "Priya Sharma", "priya@college.edu", "pass", 9.2));
        s.students.put("S003", new Student("S003", "Rahul Das", "rahul@college.edu", "pass", 7.8));
        s.admins.put("A001", new Admin("A001", "Admin", "admin@college.edu", "admin"));

        s.addCourse(Course.builder("CS101", "Programming in Java").credits(4).capacity(3)
                .timeSlot(slot(DayOfWeek.MONDAY, 9, 0, 10, 0)).build());
        s.addCourse(Course.builder("CS102", "Data Structures").credits(4).capacity(3)
                .timeSlot(slot(DayOfWeek.MONDAY, 9, 30, 10, 30)).build());          // clashes with CS101
        s.addCourse(Course.builder("CS201", "Advanced Java").credits(3).capacity(3)
                .timeSlot(slot(DayOfWeek.TUESDAY, 11, 0, 12, 0))
                .prerequisites(List.of("CS101")).build());                            // needs CS101
        s.addCourse(Course.builder("CS202", "Database Systems").credits(3).capacity(1)
                .timeSlot(slot(DayOfWeek.WEDNESDAY, 14, 0, 15, 0)).build());        // only 1 seat
        s.addCourse(Course.builder("CS301", "Machine Learning").credits(3).capacity(2)
                .timeSlot(slot(DayOfWeek.THURSDAY, 10, 0, 11, 0))
                .prerequisites(List.of("CS102", "CS201")).build());                   // chain of prereqs

        s.completedCourses.put("S002", new ArrayList<>(List.of("CS101")));            // Priya passed CS101
        return s;
    }

    private void addCourse(Course c) { courses.put(c.getCode(), c); }

    private static TimeSlot slot(DayOfWeek d, int sh, int sm, int eh, int em) {
        return new TimeSlot(d, LocalTime.of(sh, sm), LocalTime.of(eh, em));
    }
}
