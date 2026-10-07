package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.AdminController;
import com.crs.model.Course;
import com.crs.service.CourseService;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.Tables;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Admin catalogue: every course with live seat counts. Add or remove courses from the page header. */
public class AdminCoursesPanel extends RefreshablePanel<List<Course>> {
    private final CourseService courseService;
    private final AdminController controller;
    private final CourseTable table = new CourseTable(CourseTable.Mode.ADMIN);
    private final JLabel totalLabel = new JLabel();

    public AdminCoursesPanel(AppContext ctx) {
        super(ctx.getSubject());
        this.courseService = ctx.getCourseService();
        this.controller = new AdminController(this, courseService);

        Card card = new Card(new BorderLayout()).padding(1, 1, 1, 1);
        card.add(Tables.scroll(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 36));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        JLabel hint = new JLabel("Select a row, then use Remove course in the header.");
        for (JLabel l : new JLabel[]{hint, totalLabel}) {
            l.setFont(Theme.font(12));
            l.setForeground(Theme.MUTED);
        }
        footer.add(hint, BorderLayout.WEST);
        footer.add(totalLabel, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
        reload();
    }

    /** Buttons for the page header: Remove course and Add course. */
    public JComponent[] headerActions() {
        UiButton remove = new UiButton("Remove course", Icons.Name.TRASH, UiButton.Variant.DANGER);
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            controller.removeCourse(row < 0 ? null : table.courseAt(row).getCode(), this::reload);
        });
        UiButton add = new UiButton("Add course", Icons.Name.PLUS, UiButton.Variant.PRIMARY);
        add.addActionListener(e -> controller.addCourse(this::reload));
        return new JComponent[]{remove, add};
    }

    @Override
    protected List<Course> loadData() {
        return courseService.getAllSorted();
    }

    @Override
    protected void render(List<Course> courses) {
        table.setData(courses, Set.of(), Set.of());
        int seats = courses.stream().mapToInt(Course::getSeatsLeft).sum();
        totalLabel.setText(courses.size() + " courses  ·  " + seats + " seats left");
    }
}
