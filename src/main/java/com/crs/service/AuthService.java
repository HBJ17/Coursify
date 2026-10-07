package com.crs.service;

import com.crs.exception.AuthException;
import com.crs.model.Person;

/** CONTRACT. Real version: Member 1 (service.catalog.AuthServiceImpl). */
public interface AuthService {
    /** Returns a Student or an Admin. The UI checks person.getRole() to decide which screens to show. */
    Person login(String id, String password) throws AuthException;
}
