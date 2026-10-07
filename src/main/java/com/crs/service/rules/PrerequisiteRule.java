package com.crs.service.rules;

import com.crs.dao.CourseDAO;
import com.crs.dao.StudentDAO;
import com.crs.exception.PrerequisiteNotMetException;
import com.crs.model.Course;
import com.crs.model.Student;

import java.util.*;

/**
 * Uses DFS over the prerequisite graph to find ALL transitive prerequisites
 * for a course, then checks the student has completed all of them.
 * Throws PrerequisiteNotMetException with a list of the missing ones.
 */
public class PrerequisiteRule extends ValidationRule {
    private final CourseDAO courseDAO;
    private final StudentDAO studentDAO;

    public PrerequisiteRule(CourseDAO courseDAO, StudentDAO studentDAO) {
        this.courseDAO = courseDAO;
        this.studentDAO = studentDAO;
    }

    @Override
    public void validate(Student student, Course course) throws PrerequisiteNotMetException {
        Map<String, List<String>> graph = courseDAO.loadPrerequisiteGraph();

        // DFS to collect all transitive prerequisites
        Set<String> allPrereqs = new HashSet<>();
        Set<String> visited = new HashSet<>();
        dfs(course.getCode(), graph, visited, allPrereqs);

        if (allPrereqs.isEmpty()) return;

        // Check which prerequisites the student has not completed
        Set<String> completed = new HashSet<>(studentDAO.findCompletedCourses(student.getId()));
        List<String> missing = new ArrayList<>();
        for (String prereq : allPrereqs) {
            if (!completed.contains(prereq)) {
                missing.add(prereq);
            }
        }

        if (!missing.isEmpty()) {
            // Sort for deterministic output
            Collections.sort(missing);
            throw new PrerequisiteNotMetException(course.getCode(), missing);
        }
    }

    /**
     * Depth-first search through the prerequisite graph.
     * Collects every transitive prerequisite of the starting course.
     */
    private void dfs(String courseCode, Map<String, List<String>> graph,
                     Set<String> visited, Set<String> allPrereqs) {
        List<String> directPrereqs = graph.getOrDefault(courseCode, List.of());
        for (String prereq : directPrereqs) {
            if (visited.add(prereq)) {       // only visit each node once
                allPrereqs.add(prereq);
                dfs(prereq, graph, visited, allPrereqs);   // recurse into sub-prerequisites
            }
        }
    }
}
