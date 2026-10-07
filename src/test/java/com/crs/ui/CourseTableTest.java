package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.model.Course;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTableTest {

    private final List<Course> courses = AppContext.createWithFakes().getCourseService().getAllSorted();

    private Course course(String code) {
        return courses.stream().filter(c -> c.getCode().equals(code)).findFirst().orElseThrow();
    }

    @Test
    void statusReflectsRegistrationWaitlistAndSeats() {
        CourseTable table = new CourseTable(CourseTable.Mode.CATALOG);
        Course full = Course.builder("CS999", "Full course").capacity(1).seatsLeft(0).build();
        table.setData(List.of(course("CS101"), course("CS102"), full), Set.of("CS101"), Set.of("CS102"));

        assertEquals(CourseTable.Status.REGISTERED, table.statusOf(course("CS101")));
        assertEquals(CourseTable.Status.WAITLISTED, table.statusOf(course("CS102")));
        assertEquals(CourseTable.Status.FULL, table.statusOf(full));
        assertEquals(CourseTable.Status.OPEN, table.statusOf(course("CS201")));
    }

    @Test
    void onlyOpenOrFullCoursesHaveAButtonInTheCatalogue() {
        CourseTable table = new CourseTable(CourseTable.Mode.CATALOG);
        table.setData(courses, Set.of("CS101"), Set.of());
        assertFalse(table.hasAction(course("CS101")));   // already registered
        assertTrue(table.hasAction(course("CS102")));    // Register
    }

    @Test
    void myCoursesAlwaysOfferCancelAndAdminHasNoButtons() {
        assertTrue(new CourseTable(CourseTable.Mode.MY_COURSES).hasAction(course("CS101")));
        assertFalse(new CourseTable(CourseTable.Mode.ADMIN).hasAction(course("CS101")));
    }

    @Test
    void firstColumnIsTheCourseCode() {
        CourseTable table = new CourseTable(CourseTable.Mode.ADMIN);
        table.setData(courses, Set.of(), Set.of());
        assertEquals(courses.size(), table.getRowCount());
        assertEquals(courses.get(0).getCode(), table.getModel().getValueAt(0, 0));
    }
}
