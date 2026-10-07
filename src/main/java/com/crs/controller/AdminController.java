package com.crs.controller;

import com.crs.service.CourseService;
import com.crs.ui.AddCourseDialog;
import com.crs.ui.theme.MessageDialog.Tone;
import java.awt.Component;

/** Admin-only actions: add and remove courses. afterChange lets the screen reload. */
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

    /** Removes the given course after a confirmation. code is null when nothing is selected. */
    public void removeCourse(String code, Runnable afterChange) {
        if (code == null) {
            showHint("Select a course in the table first.");
            return;
        }
        if (!confirm(Tone.DANGER, "Remove " + code + "?",
                "The course, its registrations and its waitlist entries will be deleted.",
                "Remove course", "Cancel")) return;
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
