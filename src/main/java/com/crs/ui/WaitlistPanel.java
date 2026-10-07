package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.WaitlistEntry;
import com.crs.service.CourseService;
import com.crs.service.WaitlistService;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/** Waitlists the student is on, with their position in each queue (1 = next to get a seat). */
public class WaitlistPanel extends RefreshablePanel<List<Object[]>> {
    private final WaitlistService waitlistService;
    private final CourseService courseService;
    private final Session session;
    private final ReadOnlyTableModel model = new ReadOnlyTableModel("Code", "Title", "Your position", "Joined");
    private final JTable table = new JTable(model);

    public WaitlistPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.waitlistService = ctx.getWaitlistService();
        this.courseService = ctx.getCourseService();
        this.session = session;
        RegisterController controller = new RegisterController(this, ctx.getRegistrationService(),
                waitlistService, session);

        JButton leaveButton = new JButton("Leave selected waitlist");
        leaveButton.addActionListener(e -> controller.leaveWaitlist(selectedCourseCode(), this::reload));

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("When a seat frees up, position 1 is registered automatically."), BorderLayout.WEST);
        top.add(leaveButton, BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        reload();
    }

    @Override
    protected List<Object[]> loadData() {
        String studentId = session.getUserId();
        List<Object[]> rows = new ArrayList<>();
        for (WaitlistEntry entry : waitlistService.getMyWaitlists(studentId)) {
            String code = entry.getCourseCode();
            String title = courseService.findByCode(code).map(c -> c.getTitle()).orElse("?");
            rows.add(new Object[]{code, title, waitlistService.positionOf(studentId, code),
                    UiFormat.dateTime(entry.getJoinedTime())});
        }
        return rows;
    }

    @Override
    protected void render(List<Object[]> rows) {
        model.setRows(rows);
    }

    private String selectedCourseCode() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) model.getValueAt(row, 0);
    }
}
