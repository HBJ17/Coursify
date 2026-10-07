package com.crs.service.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.crs.exception.AuthException;
import com.crs.fake.FakeDataStore;
import com.crs.fake.InMemoryAdminDAO;
import com.crs.fake.InMemoryStudentDAO;
import com.crs.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs without Oracle: the service talks to the in-memory DAOs. */
class AuthServiceImplTest {
    private AuthServiceImpl auth;

    @BeforeEach
    void setUp() {
        FakeDataStore store = FakeDataStore.withSampleData();
        auth = new AuthServiceImpl(new InMemoryStudentDAO(store), new InMemoryAdminDAO(store));
    }

    @Test
    void studentCanLogIn() throws AuthException {
        Person p = auth.login("S001", "pass");
        assertEquals("STUDENT", p.getRole());
        assertEquals("S001", p.getId());
    }

    @Test
    void adminCanLogIn() throws AuthException {
        assertEquals("ADMIN", auth.login("A001", "admin").getRole());
    }

    @Test
    void wrongPasswordIsRejected() {
        AuthException e = assertThrows(AuthException.class, () -> auth.login("S001", "wrong"));
        assertEquals("Invalid ID or password", e.getMessage());
    }

    @Test
    void unknownIdIsRejectedWithSameMessage() {
        AuthException e = assertThrows(AuthException.class, () -> auth.login("S999", "pass"));
        assertEquals("Invalid ID or password", e.getMessage());
    }

    @Test
    void adminPasswordDoesNotWorkForAStudentId() {
        assertThrows(AuthException.class, () -> auth.login("S001", "admin"));
    }

    @Test
    void nullInputsAreRejected() {
        assertThrows(AuthException.class, () -> auth.login(null, null));
        assertThrows(AuthException.class, () -> auth.login("S001", null));
    }
}
