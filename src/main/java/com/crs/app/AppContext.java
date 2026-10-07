package com.crs.app;

import com.crs.dao.*;
import com.crs.fake.*;
import com.crs.observer.RegistrationSubject;
import com.crs.service.*;
import com.crs.dao.impl.*;
import com.crs.observer.impl.RegistrationSubjectImpl;
import com.crs.service.analytics.AnalyticsServiceImpl;
import com.crs.service.catalog.AuthServiceImpl;
import com.crs.service.catalog.CourseServiceImpl;
import com.crs.service.registration.RegistrationServiceImpl;
import com.crs.service.waitlist.WaitlistComparators;
import com.crs.service.waitlist.WaitlistServiceImpl;

/**
 * THE ONLY PLACE where objects get created and connected together.
 * Everyone else just receives what they need through constructors.
 *
 * During development:   AppContext.createWithFakes()
 * With Oracle:          AppContext.createReal()   (already wired to every member's class)
 * Main uses:            AppContext.createDefault() (real if db.properties exists, else fakes)
 *
 * Owner: Member 4. Do NOT change the wiring: every constructor here is fixed for the whole team.
 */
public class AppContext {
    private final AuthService authService;
    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final WaitlistService waitlistService;
    private final AnalyticsService analyticsService;
    private final RegistrationSubject subject;

    private AppContext(AuthService auth, CourseService course, RegistrationService registration,
                       WaitlistService waitlist, AnalyticsService analytics, RegistrationSubject subject) {
        this.authService = auth;
        this.courseService = course;
        this.registrationService = registration;
        this.waitlistService = waitlist;
        this.analyticsService = analytics;
        this.subject = subject;
    }

    /** Everything fake, no database needed. */
    public static AppContext createWithFakes() {
        FakeDataStore store = FakeDataStore.withSampleData();
        StudentDAO studentDAO = new InMemoryStudentDAO(store);
        AdminDAO adminDAO = new InMemoryAdminDAO(store);
        CourseDAO courseDAO = new InMemoryCourseDAO(store);
        RegistrationDAO registrationDAO = new InMemoryRegistrationDAO(store);
        WaitlistDAO waitlistDAO = new InMemoryWaitlistDAO(store);

        RegistrationSubject subject = new SimpleRegistrationSubject();

        return new AppContext(
                new FakeAuthService(studentDAO, adminDAO),
                new FakeCourseService(courseDAO),
                new FakeRegistrationService(courseDAO, registrationDAO, subject),
                new FakeWaitlistService(waitlistDAO, studentDAO, subject),
                new FakeAnalyticsService(courseDAO, registrationDAO, waitlistDAO),
                subject);
    }

    /**
     * Real Oracle-backed objects. Already wired: each member only fills in their own classes,
     * so once all branches are merged this works with no extra integration step.
     */
    public static AppContext createReal() {
        StudentDAO studentDAO = new StudentDAOImpl();                         // Member 1
        AdminDAO adminDAO = new AdminDAOImpl();                               // Member 1
        CourseDAO courseDAO = new CourseDAOImpl();                            // Member 1
        RegistrationDAO registrationDAO = new RegistrationDAOImpl();          // Member 1
        WaitlistDAO waitlistDAO = new WaitlistDAOImpl();                      // Member 1

        RegistrationSubject subject = new RegistrationSubjectImpl();          // Member 3
        WaitlistService waitlist = new WaitlistServiceImpl(waitlistDAO, studentDAO, courseDAO,
                registrationDAO, subject, WaitlistComparators.BY_CGPA_THEN_TIME);  // Member 3
        AnalyticsService analytics = new AnalyticsServiceImpl(courseDAO, registrationDAO, waitlistDAO); // Member 3

        RegistrationService registration = new RegistrationServiceImpl(studentDAO, courseDAO,
                registrationDAO, waitlist, subject);                          // Member 2

        AuthService auth = new AuthServiceImpl(studentDAO, adminDAO);         // Member 1
        CourseService course = new CourseServiceImpl(courseDAO);              // Member 1
        return new AppContext(auth, course, registration, waitlist, analytics, subject);
    }

    /** Uses Oracle if src/main/resources/db.properties exists, otherwise the fakes. */
    public static AppContext createDefault() {
        return AppContext.class.getResource("/db.properties") != null ? createReal() : createWithFakes();
    }

    public AuthService getAuthService() { return authService; }
    public CourseService getCourseService() { return courseService; }
    public RegistrationService getRegistrationService() { return registrationService; }
    public WaitlistService getWaitlistService() { return waitlistService; }
    public AnalyticsService getAnalyticsService() { return analyticsService; }
    public RegistrationSubject getSubject() { return subject; }
}
