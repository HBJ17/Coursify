package com.crs.fake;

import com.crs.dao.AdminDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.AuthException;
import com.crs.model.Person;
import com.crs.service.AuthService;
import java.util.Optional;

public class FakeAuthService implements AuthService {
    private final StudentDAO students;
    private final AdminDAO admins;

    public FakeAuthService(StudentDAO students, AdminDAO admins) { this.students = students; this.admins = admins; }

    @Override
    public Person login(String id, String password) throws AuthException {
        Optional<? extends Person> p = students.findById(id);
        if (p.isEmpty()) p = admins.findById(id);
        if (p.isEmpty() || !p.get().getPassword().equals(password)) throw new AuthException("Invalid ID or password");
        return p.get();
    }
}
