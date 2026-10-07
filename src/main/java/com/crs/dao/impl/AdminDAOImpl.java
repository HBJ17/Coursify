package com.crs.dao.impl;

import com.crs.dao.AdminDAO;
import com.crs.exception.DatabaseOperationException;
import com.crs.model.Admin;
import com.crs.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Oracle JDBC version of AdminDAO. */
public class AdminDAOImpl implements AdminDAO {
    public AdminDAOImpl() { }

    @Override
    public Optional<Admin> findById(String adminId) {
        String sql = "SELECT admin_id, name, email, password FROM admins WHERE admin_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Admin(rs.getString("admin_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("password")));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load admin " + adminId, e);
        }
    }
}
