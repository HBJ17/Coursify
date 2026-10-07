package com.crs.ui;

import com.crs.model.Admin;
import com.crs.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void startsLoggedOut() {
        Session session = new Session();
        assertFalse(session.isLoggedIn());
        assertFalse(session.isAdmin());
        assertThrows(IllegalStateException.class, session::getUser);
    }

    @Test
    void remembersStudent() {
        Session session = new Session();
        session.login(new Student("S001", "Arun Kumar", "arun@college.edu", "pass", 8.5));
        assertTrue(session.isLoggedIn());
        assertFalse(session.isAdmin());
        assertEquals("S001", session.getUserId());
    }

    @Test
    void recognisesAdmin() {
        Session session = new Session();
        session.login(new Admin("A001", "Admin", "admin@college.edu", "admin"));
        assertTrue(session.isAdmin());
    }

    @Test
    void logoutClearsUser() {
        Session session = new Session();
        session.login(new Admin("A001", "Admin", "admin@college.edu", "admin"));
        session.logout();
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginRejectsNull() {
        assertThrows(NullPointerException.class, () -> new Session().login(null));
    }
}
