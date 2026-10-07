package com.crs.ui;

import com.crs.model.Person;
import java.util.Objects;

/**
 * Remembers who is logged in. One Session is shared by every screen of the window.
 * Holds a Person, so it works for both Student and Admin (polymorphism through getRole()).
 */
public class Session {
    private Person user;

    public void login(Person person) {
        this.user = Objects.requireNonNull(person, "person");
    }

    public void logout() {
        this.user = null;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    /** The logged-in Student or Admin. Throws if nobody is logged in. */
    public Person getUser() {
        if (user == null) throw new IllegalStateException("Nobody is logged in");
        return user;
    }

    public String getUserId() {
        return getUser().getId();
    }

    public boolean isAdmin() {
        return user != null && "ADMIN".equals(user.getRole());
    }
}
