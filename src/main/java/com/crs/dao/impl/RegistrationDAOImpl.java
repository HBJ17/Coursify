package com.crs.dao.impl;

import com.crs.dao.RegistrationDAO;
import com.crs.model.Registration;
import java.util.List;

/** STUB. Member 1 replaces the method bodies with Oracle JDBC code (keep the class name and no-arg constructor). */
public class RegistrationDAOImpl implements RegistrationDAO {
    public RegistrationDAOImpl() { }

    @Override public void registerAtomic(Registration registration) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void cancelAtomic(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Registration> findActiveByStudent(String studentId) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Registration> findActiveByCourse(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<Registration> findAllActive() { throw new UnsupportedOperationException("TODO Member 1"); }
}
