/**
 * OWNER: Member 2 (Registration Engine)
 *
 * Write here: abstract class ValidationRule with abstract validate(Student, Course),
 * and subclasses DuplicateRule (HashSet), SeatAvailabilityRule, PrerequisiteRule (DFS on the graph from
 * CourseDAO.loadPrerequisiteGraph()), TimeConflictRule (interval overlap: a.start < b.end AND b.start < a.end, same day).
 * This is Polymorphism + the Strategy pattern.
 */
package com.crs.service.rules;
