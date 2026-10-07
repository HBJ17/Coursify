package com.crs.dao;

import com.crs.model.WaitlistEntry;
import java.util.List;

/** CONTRACT. Real version: Member 1 (dao.impl.WaitlistDAOImpl). */
public interface WaitlistDAO {
    void add(WaitlistEntry entry);
    void remove(String studentId, String courseCode);
    List<WaitlistEntry> findByCourse(String courseCode);
    List<WaitlistEntry> findByStudent(String studentId);
    List<WaitlistEntry> findAll();
}
