package com.crs;

import com.crs.app.AppContext;
import com.crs.exception.AuthException;
import com.crs.exception.CourseFullException;
import com.crs.exception.DuplicateRegistrationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the day-one skeleton works. Each member adds their own test file
 * in a matching package, e.g. src/test/java/com/crs/service/rules/TimeConflictRuleTest.java
 */
class SkeletonTest {

    @Test
    void loginWorksWithSampleData() throws Exception {
        AppContext ctx = AppContext.createWithFakes();
        assertEquals("STUDENT", ctx.getAuthService().login("S001", "pass").getRole());
        assertEquals("ADMIN", ctx.getAuthService().login("A001", "admin").getRole());
        assertThrows(AuthException.class, () -> ctx.getAuthService().login("S001", "wrong"));
    }

    @Test
    void fullCourseThrowsCourseFull() throws Exception {
        AppContext ctx = AppContext.createWithFakes();
        ctx.getRegistrationService().register("S001", "CS202"); // CS202 has 1 seat
        assertThrows(CourseFullException.class, () -> ctx.getRegistrationService().register("S002", "CS202"));
    }

    @Test
    void registeringTwiceThrowsDuplicate() throws Exception {
        AppContext ctx = AppContext.createWithFakes();
        ctx.getRegistrationService().register("S001", "CS101");
        assertThrows(DuplicateRegistrationException.class, () -> ctx.getRegistrationService().register("S001", "CS101"));
    }

    @Test
    void cancelFreesTheSeat() throws Exception {
        AppContext ctx = AppContext.createWithFakes();
        ctx.getRegistrationService().register("S001", "CS202");
        ctx.getRegistrationService().cancel("S001", "CS202");
        assertEquals(1, ctx.getCourseService().findByCode("CS202").orElseThrow().getSeatsLeft());
    }
}
