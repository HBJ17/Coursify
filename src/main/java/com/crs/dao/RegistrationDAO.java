package com.crs.dao;

import com.crs.model.Registration;
import java.util.List;

/** CONTRACT. Real version: Member 1 (dao.impl.RegistrationDAOImpl). */
public interface RegistrationDAO {
    /**
     * In ONE transaction: insert the registration AND decrease the course's seats_left by 1.
     * If either step fails, roll back both. Sets the generated regId on the object.
     */
    void registerAtomic(Registration registration);

    /**
     * In ONE transaction: mark the registration CANCELLED AND increase seats_left by 1.
     */
    void cancelAtomic(String studentId, String courseCode);

    List<Registration> findActiveByStudent(String studentId);
    List<Registration> findActiveByCourse(String courseCode);
    List<Registration> findAllActive();
}
