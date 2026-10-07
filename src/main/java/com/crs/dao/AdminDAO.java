package com.crs.dao;

import com.crs.model.Admin;
import java.util.Optional;

/** CONTRACT. Real version: Member 1 (dao.impl.AdminDAOImpl). */
public interface AdminDAO {
    Optional<Admin> findById(String adminId);
}
