package com.crs.dao.impl;

import com.crs.dao.WaitlistDAO;
import com.crs.model.WaitlistEntry;
import java.util.List;

/** STUB. Member 1 replaces the method bodies with Oracle JDBC code (keep the class name and no-arg constructor). */
public class WaitlistDAOImpl implements WaitlistDAO {
    public WaitlistDAOImpl() { }

    @Override public void add(WaitlistEntry entry) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void remove(String studentId, String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<WaitlistEntry> findByCourse(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<WaitlistEntry> findByStudent(String studentId) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public List<WaitlistEntry> findAll() { throw new UnsupportedOperationException("TODO Member 1"); }
}
