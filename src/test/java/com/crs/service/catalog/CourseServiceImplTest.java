package com.crs.service.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryCourseDAO;
import com.crs.model.Course;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs without Oracle: the service talks to the in-memory DAO. */
class CourseServiceImplTest {
    private CourseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CourseServiceImpl(new InMemoryCourseDAO(FakeDataStore.withSampleData()));
    }

    @Test
    void getAllSortedReturnsCoursesInCodeOrder() {
        List<String> codes = service.getAllSorted().stream().map(Course::getCode).toList();
        assertEquals(List.of("CS101", "CS102", "CS201", "CS202", "CS301"), codes);
    }

    @Test
    void getAllSortedStaysSortedAfterAddingAnEarlierCode() {
        service.addCourse(Course.builder("CS050", "Intro").build());
        assertEquals("CS050", service.getAllSorted().get(0).getCode());
    }

    @Test
    void findByCodeFindsOrReturnsEmpty() {
        assertEquals("Advanced Java", service.findByCode("CS201").orElseThrow().getTitle());
        assertTrue(service.findByCode("NOPE").isEmpty());
    }

    @Test
    void searchMatchesCodeIgnoringCase() {
        List<Course> result = service.search("cs30");
        assertEquals(1, result.size());
        assertEquals("CS301", result.get(0).getCode());
    }

    @Test
    void searchMatchesTitleIgnoringCase() {
        List<String> codes = service.search("JAVA").stream().map(Course::getCode).toList();
        assertEquals(List.of("CS101", "CS201"), codes);
    }

    @Test
    void blankSearchReturnsAllCourses() {
        assertEquals(5, service.search("").size());
        assertEquals(5, service.search("   ").size());
        assertEquals(5, service.search(null).size());
    }

    @Test
    void searchWithNoMatchReturnsEmptyList() {
        assertTrue(service.search("zzz").isEmpty());
    }

    @Test
    void removeCourseDeletesIt() {
        service.removeCourse("CS202");
        assertTrue(service.findByCode("CS202").isEmpty());
        assertEquals(4, service.getAllSorted().size());
    }

    @Test
    void seatCountsAreFreshOnEveryRead() {
        FakeDataStore store = FakeDataStore.withSampleData();
        CourseServiceImpl svc = new CourseServiceImpl(new InMemoryCourseDAO(store));
        assertEquals(1, svc.findByCode("CS202").orElseThrow().getSeatsLeft());
        new InMemoryCourseDAO(store).findByCode("CS202").orElseThrow().setSeatsLeft(0);
        assertEquals(0, svc.findByCode("CS202").orElseThrow().getSeatsLeft());
    }
}
