package com.crs.service.catalog;

import com.crs.dao.AdminDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.AuthException;
import com.crs.model.Person;
import com.crs.service.AuthService;
import java.util.Optional;

/** Checks an ID and password against students first, then admins. */
public class AuthServiceImpl implements AuthService {
    private final StudentDAO studentDAO;
    private final AdminDAO adminDAO;

    public AuthServiceImpl(StudentDAO studentDAO, AdminDAO adminDAO) {
        this.studentDAO = studentDAO;
        this.adminDAO = adminDAO;
    }

    @Override
    public Person login(String id, String password) throws AuthException {
        if (id != null && password != null) {
            // Student and Admin are both Persons, so one variable type can hold either (Polymorphism).
            Optional<? extends Person> user = studentDAO.findById(id);
            if (user.isEmpty()) user = adminDAO.findById(id);
            if (user.isPresent() && user.get().getPassword().equals(password)) return user.get();
        }
        // Same message for "no such ID" and "wrong password" so an attacker learns nothing.
        throw new AuthException("Invalid ID or password");
    }
}
