package com.crs.model;

/**
 * Base class for anyone who can log in. Student and Admin extend this (Inheritance).
 * Fields are private and only changed through validated setters (Encapsulation).
 */
public abstract class Person {
    private final String id;
    private String name;
    private String email;
    private String password; // plain text is OK for a college demo; hashing is a bonus

    protected Person(String id, String name, String email, String password) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be empty");
        this.id = id;
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    /** Each subclass says what kind of user it is, e.g. "STUDENT" or "ADMIN". */
    public abstract String getRole();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Invalid email: " + email);
        this.email = email;
    }

    public void setPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty");
        this.password = password;
    }

    @Override
    public String toString() { return getRole() + "[" + id + ", " + name + "]"; }
}
