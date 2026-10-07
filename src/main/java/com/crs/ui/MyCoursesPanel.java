package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.Course;
import com.crs.service.RegistrationService;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/** The courses the logged-in student is registered for, with a Cancel button. Refreshes itself on every event. */
public class MyCoursesPanel extends RefreshablePanel<List<Course>> {
    private final RegistrationService registrationService;
    private final Session session;
    private final ReadOnlyTableModel model = new ReadOnlyTableModel("Code", "Title", "Credits", "Time");
    private final JTable table = new JTable(model);
    private final JLabel totalLabel = new JLabel();

    public MyCoursesPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.registrationService = ctx.getRegistrationService();
        this.session = session;
        RegisterController controller = new RegisterController(this, registrationService,
                ctx.getWaitlistService(), session);

        JButton cancelButton = new JButton("Cancel selected course");
        cancelButton.addActionListener(e -> controller.cancel(selectedCourseCode()));

        JPanel top = new JPanel(new BorderLayout());
        top.add(totalLabel, BorderLayout.WEST);
        top.add(cancelButton, BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        reload();
    }

    @Override
    protected List<Course> loadData() {
        return registrationService.getMyCourses(session.getUserId());
    }

    @Override
    protected void render(List<Course> courses) {
        List<Object[]> rows = new ArrayList<>();
        int totalCredits = 0;
        for (Course c : courses) {
            rows.add(new Object[]{c.getCode(), c.getTitle(), c.getCredits(), UiFormat.timeSlot(c)});
            totalCredits += c.getCredits();
        }
        model.setRows(rows);
        totalLabel.setText(courses.size() + " course(s), " + totalCredits + " credits");
    }

    private String selectedCourseCode() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) model.getValueAt(row, 0);
    }
}
