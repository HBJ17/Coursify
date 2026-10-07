package com.crs.controller;

import com.crs.model.Course;
import com.crs.service.CourseService;
import com.crs.ui.AddCourseDialog;
import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/** Admin-only actions: add and remove courses. afterChange lets the dashboard reload. */
public class AdminController extends BaseController {
    private final CourseService courseService;

    public AdminController(Component parent, CourseService courseService) {
        super(parent);
        this.courseService = courseService;
    }

    public void addCourse(Runnable afterChange) {
        new AddCourseDialog(parent).showDialog().ifPresent(course ->
                runAsync(() -> {
                            courseService.addCourse(course);
                            return course.getCode();
                        },
                        code -> {
                            showInfo("Course " + code + " was added.");
                            afterChange.run();
                        }));
    }

    /** Loads the course list in the background, lets the admin pick one, then removes it. */
    public void removeCourse(Runnable afterChange) {
        runAsync(courseService::getAllSorted, courses -> pickAndRemove(courses, afterChange));
    }

    private void pickAndRemove(List<Course> courses, Runnable afterChange) {
        if (courses.isEmpty()) {
            showInfo("There are no courses to remove.");
            return;
        }
        JComboBox<String> picker = new JComboBox<>();
        for (Course c : courses) picker.addItem(c.getCode() + " - " + c.getTitle());

        int choice = JOptionPane.showConfirmDialog(parent, picker, "Remove which course?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String code = courses.get(picker.getSelectedIndex()).getCode();
        if (!confirm("Remove " + code + "? Its registrations and waitlist entries are removed too.")) return;
        runAsync(() -> {
                    courseService.removeCourse(code);
                    return code;
                },
                removed -> {
                    showInfo("Course " + removed + " was removed.");
                    afterChange.run();
                });
    }
}
