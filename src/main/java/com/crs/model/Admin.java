package com.crs.model;

/** An admin can add/remove courses and see all registrations and analytics. */
public class Admin extends Person {
    public Admin(String id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override
    public String getRole() { return "ADMIN"; }
}
