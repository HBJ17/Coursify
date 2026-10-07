package com.crs.dao.impl;

import com.crs.dao.StudentDAO;
import com.crs.exception.DatabaseOperationException;
import com.crs.model.Student;
import com.crs.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Oracle JDBC version of StudentDAO. Every query uses PreparedStatement and try-with-resources. */
public class StudentDAOImpl implements StudentDAO {
    private static final String SELECT = "SELECT student_id, name, email, password, cgpa FROM students";

    public StudentDAOImpl() { }

    @Override
    public Optional<Student> findById(String studentId) {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE student_id = ?")) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapStudent(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load student " + studentId, e);
        }
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY student_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) students.add(mapStudent(rs));
            return students;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load students", e);
        }
    }

    /** Saves a student: updates the row if the ID exists, otherwise inserts a new one. */
    @Override
    public void save(Student student) {
        String update = "UPDATE students SET name = ?, email = ?, password = ?, cgpa = ? WHERE student_id = ?";
        String insert = "INSERT INTO students (name, email, password, cgpa, student_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            if (execute(conn, update, student) == 0) execute(conn, insert, student);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not save student " + student.getId(), e);
        }
    }

    @Override
    public List<String> findCompletedCourses(String studentId) {
        List<String> codes = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT course_code FROM completed_courses WHERE student_id = ? ORDER BY course_code")) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) codes.add(rs.getString("course_code"));
            }
            return codes;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load completed courses of " + studentId, e);
        }
    }

    // Both SQL statements above use the same parameter order, so one helper runs either.
    private int execute(Connection conn, String sql, Student s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPassword());
            ps.setDouble(4, s.getCgpa());
            ps.setString(5, s.getId());
            return ps.executeUpdate();
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(rs.getString("student_id"), rs.getString("name"), rs.getString("email"),
                rs.getString("password"), rs.getDouble("cgpa"));
    }
}
