package com.crs.service.catalog;

import com.crs.dao.AdminDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.AuthException;
import com.crs.model.Person;
import com.crs.service.AuthService;

/** STUB. Member 1 replaces the method body (keep the constructor signature). */
public class AuthServiceImpl implements AuthService {
    private final StudentDAO studentDAO;
    private final AdminDAO adminDAO;

    public AuthServiceImpl(StudentDAO studentDAO, AdminDAO adminDAO) {
        this.studentDAO = studentDAO;
        this.adminDAO = adminDAO;
    }

    @Override
    public Person login(String id, String password) throws AuthException {
        throw new UnsupportedOperationException("TODO Member 1");
    }
}
