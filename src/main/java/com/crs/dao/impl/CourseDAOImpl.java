package com.crs.dao.impl;

import com.crs.dao.CourseDAO;
import com.crs.exception.DatabaseOperationException;
import com.crs.model.Course;
import com.crs.model.TimeSlot;
import com.crs.util.DBConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Oracle JDBC version of CourseDAO. A course is one row in courses plus rows in prerequisites. */
public class CourseDAOImpl implements CourseDAO {
    private static final String SELECT = "SELECT course_code, title, credits, capacity, seats_left, "
            + "waitlist_cap, class_day, start_time, end_time FROM courses";

    public CourseDAOImpl() { }

    @Override
    public Optional<Course> findByCode(String courseCode) {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE course_code = ?")) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                // read the row first, then its prerequisites
                return Optional.of(mapCourse(rs, loadPrerequisites(conn, courseCode)));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load course " + courseCode, e);
        }
    }

    @Override
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            // one query for all prerequisites, instead of one query per course
            Map<String, List<String>> graph = loadPrerequisiteGraph(conn);
            try (PreparedStatement ps = conn.prepareStatement(SELECT + " ORDER BY course_code");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<String> prereqs = graph.getOrDefault(rs.getString("course_code"), List.of());
                    courses.add(mapCourse(rs, prereqs));
                }
            }
            return courses;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load courses", e);
        }
    }

    @Override public void save(Course course) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void update(Course course) { throw new UnsupportedOperationException("TODO Member 1"); }
    @Override public void delete(String courseCode) { throw new UnsupportedOperationException("TODO Member 1"); }

    /** Adjacency list: course code -> codes of its prerequisites. Every course is a key, even with no prerequisites. */
    @Override
    public Map<String, List<String>> loadPrerequisiteGraph() {
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            return loadPrerequisiteGraph(conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load the prerequisite graph", e);
        }
    }

    private Map<String, List<String>> loadPrerequisiteGraph(Connection conn) throws SQLException {
        Map<String, List<String>> graph = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT course_code FROM courses ORDER BY course_code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) graph.put(rs.getString(1), new ArrayList<>());
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT course_code, prereq_code FROM prerequisites ORDER BY course_code, prereq_code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) graph.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2));
        }
        return graph;
    }

    private List<String> loadPrerequisites(Connection conn, String courseCode) throws SQLException {
        List<String> prereqs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT prereq_code FROM prerequisites WHERE course_code = ? ORDER BY prereq_code")) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) prereqs.add(rs.getString(1));
            }
        }
        return prereqs;
    }

    private Course mapCourse(ResultSet rs, List<String> prerequisites) throws SQLException {
        return Course.builder(rs.getString("course_code"), rs.getString("title"))
                .credits(rs.getInt("credits"))
                .capacity(rs.getInt("capacity"))
                .seatsLeft(rs.getInt("seats_left"))
                .waitlistCap(rs.getInt("waitlist_cap"))
                .timeSlot(mapTimeSlot(rs))
                .prerequisites(prerequisites)
                .build();
    }

    /** class_day is NULL for a course without a fixed time: that becomes a null TimeSlot. */
    private TimeSlot mapTimeSlot(ResultSet rs) throws SQLException {
        String day = rs.getString("class_day");
        if (day == null) return null;
        return new TimeSlot(DayOfWeek.valueOf(day),
                LocalTime.parse(rs.getString("start_time")), LocalTime.parse(rs.getString("end_time")));
    }
}
