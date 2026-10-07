package com.crs.dao.impl;

import com.crs.dao.RegistrationDAO;
import com.crs.exception.DatabaseOperationException;
import com.crs.model.Registration;
import com.crs.model.RegistrationStatus;
import com.crs.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle JDBC version of RegistrationDAO.
 * registerAtomic and cancelAtomic are TRANSACTIONS: two SQL statements that either both happen or neither does.
 */
public class RegistrationDAOImpl implements RegistrationDAO {
    public RegistrationDAOImpl() { }

    /** Takes one seat and inserts the registration. If there is no seat, nothing is changed. */
    @Override
    public void registerAtomic(Registration registration) {
        String code = registration.getCourseCode();
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            conn.setAutoCommit(false); // start the transaction: nothing is permanent until commit()
            try {
                // Step 1: take a seat. "seats_left > 0" in the WHERE makes this safe when two students click at once.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE courses SET seats_left = seats_left - 1 WHERE course_code = ? AND seats_left > 0")) {
                    ps.setString(1, code);
                    if (ps.executeUpdate() == 0) {
                        throw new DatabaseOperationException("No seats left in " + code, null); // caught below: rollback
                    }
                }
                // Step 2: record the registration and read back the ID Oracle generated for it.
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO registrations (student_id, course_code, status, reg_time) VALUES (?, ?, ?, ?)",
                        new String[]{"REG_ID"})) {
                    ps.setString(1, registration.getStudentId());
                    ps.setString(2, code);
                    ps.setString(3, registration.getStatus().name());
                    ps.setTimestamp(4, Timestamp.valueOf(registration.getRegTime()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) registration.setRegId(keys.getInt(1));
                    }
                }
                conn.commit(); // both steps succeeded
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // undo the seat change too
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Could not register " + registration.getStudentId() + " in " + code + ": " + e.getMessage(), e);
        }
    }

    /** Marks the ACTIVE registration CANCELLED and gives the seat back. */
    @Override
    public void cancelAtomic(String studentId, String courseCode) {
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE registrations SET status = 'CANCELLED' "
                        + "WHERE student_id = ? AND course_code = ? AND status = 'ACTIVE'")) {
                    ps.setString(1, studentId);
                    ps.setString(2, courseCode);
                    if (ps.executeUpdate() == 0) {
                        throw new DatabaseOperationException(
                                "No active registration of " + studentId + " in " + courseCode, null);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE courses SET seats_left = seats_left + 1 WHERE course_code = ?")) {
                    ps.setString(1, courseCode);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Could not cancel " + studentId + " in " + courseCode + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Registration> findActiveByStudent(String studentId) {
        return queryActive("WHERE status = 'ACTIVE' AND student_id = ?", studentId);
    }

    @Override
    public List<Registration> findActiveByCourse(String courseCode) {
        return queryActive("WHERE status = 'ACTIVE' AND course_code = ?", courseCode);
    }

    @Override
    public List<Registration> findAllActive() {
        return queryActive("WHERE status = 'ACTIVE'", null);
    }

    /** Runs the SELECT with the given WHERE part. "param" is the one ? value, or null when there is none. */
    private List<Registration> queryActive(String where, String param) {
        String sql = "SELECT reg_id, student_id, course_code, status, reg_time FROM registrations "
                + where + " ORDER BY reg_id";
        List<Registration> list = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(rs.getInt("reg_id"), rs.getString("student_id"),
                            rs.getString("course_code"), RegistrationStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("reg_time").toLocalDateTime()));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load registrations: " + e.getMessage(), e);
        }
    }
}
