package com.crs.dao.impl;

import com.crs.dao.WaitlistDAO;
import com.crs.exception.DatabaseOperationException;
import com.crs.model.WaitlistEntry;
import com.crs.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle JDBC version of WaitlistDAO.
 * The waitlist table has no cgpa column, so reads JOIN the students table to get it.
 */
public class WaitlistDAOImpl implements WaitlistDAO {
    private static final String SELECT =
            "SELECT w.waitlist_id, w.student_id, w.course_code, w.joined_time, s.cgpa "
            + "FROM waitlist w JOIN students s ON s.student_id = w.student_id ";

    public WaitlistDAOImpl() { }

    @Override
    public void add(WaitlistEntry entry) {
        String sql = "INSERT INTO waitlist (student_id, course_code, joined_time) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"WAITLIST_ID"})) {
            ps.setString(1, entry.getStudentId());
            ps.setString(2, entry.getCourseCode());
            ps.setTimestamp(3, Timestamp.valueOf(entry.getJoinedTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setWaitlistId(keys.getInt(1)); // give the generated ID back to the object
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not add " + entry.getStudentId()
                    + " to the waitlist of " + entry.getCourseCode() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(String studentId, String courseCode) {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM waitlist WHERE student_id = ? AND course_code = ?")) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not remove " + studentId
                    + " from the waitlist of " + courseCode + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<WaitlistEntry> findByCourse(String courseCode) {
        return query("WHERE w.course_code = ? ", courseCode);
    }

    @Override
    public List<WaitlistEntry> findByStudent(String studentId) {
        return query("WHERE w.student_id = ? ", studentId);
    }

    @Override
    public List<WaitlistEntry> findAll() {
        return query("", null);
    }

    /** Runs the joined SELECT. "param" is the one ? value, or null when there is none. */
    private List<WaitlistEntry> query(String where, String param) {
        List<WaitlistEntry> list = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + where + "ORDER BY w.waitlist_id")) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WaitlistEntry(rs.getInt("waitlist_id"), rs.getString("student_id"),
                            rs.getString("course_code"), rs.getDouble("cgpa"),
                            rs.getTimestamp("joined_time").toLocalDateTime()));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load the waitlist: " + e.getMessage(), e);
        }
    }
}
