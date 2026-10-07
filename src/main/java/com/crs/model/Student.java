package com.crs.model;

public class Student extends Person {
    private double cgpa;

    public Student(String id, String name, String email, String password, double cgpa) {
        super(id, name, email, password);
        setCgpa(cgpa);
    }

    @Override
    public String getRole() { return "STUDENT"; }

    public double getCgpa() { return cgpa; }

    public void setCgpa(double cgpa) {
        if (cgpa < 0 || cgpa > 10) throw new IllegalArgumentException("CGPA must be between 0 and 10");
        this.cgpa = cgpa;
    }
}
