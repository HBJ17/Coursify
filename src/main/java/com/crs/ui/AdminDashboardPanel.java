package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.AdminController;
import com.crs.model.CourseDemand;
import com.crs.model.Registration;
import com.crs.service.AnalyticsService;
import com.crs.service.RegistrationService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

/**
 * Admin screen. Top: the most in-demand courses (ranked by the analytics max-heap).
 * Bottom: every active registration. Both refresh automatically on every registration event.
 * The toolbar lets the admin add and remove courses.
 */
public class AdminDashboardPanel extends RefreshablePanel<AdminDashboardPanel.Data> {
    static final int TOP_K = 5;

    /** Everything the dashboard shows, loaded together in the background. */
    record Data(List<CourseDemand> topCourses, List<Registration> registrations) { }

    private final AnalyticsService analyticsService;
    private final RegistrationService registrationService;
    private final ReadOnlyTableModel demandModel = new ReadOnlyTableModel(
            "Rank", "Code", "Title", "Capacity", "Registered", "Waitlisted", "Demand");
    private final ReadOnlyTableModel registrationModel = new ReadOnlyTableModel(
            "Reg ID", "Student", "Course", "Registered at");
    private final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

    public AdminDashboardPanel(AppContext ctx) {
        super(ctx.getSubject());
        this.analyticsService = ctx.getAnalyticsService();
        this.registrationService = ctx.getRegistrationService();

        AdminController controller = new AdminController(this, ctx.getCourseService());
        JButton addButton = new JButton("Add course");
        addButton.addActionListener(e -> controller.addCourse(this::reload));
        JButton removeButton = new JButton("Remove course");
        removeButton.addActionListener(e -> controller.removeCourse(this::reload));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> reload());
        toolbar.add(addButton);
        toolbar.add(removeButton);
        toolbar.add(refreshButton);

        JScrollPane demandPane = new JScrollPane(new JTable(demandModel));
        demandPane.setBorder(BorderFactory.createTitledBorder("Top " + TOP_K + " in-demand courses"));
        JScrollPane registrationPane = new JScrollPane(new JTable(registrationModel));
        registrationPane.setBorder(BorderFactory.createTitledBorder("All active registrations"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, demandPane, registrationPane);
        split.setResizeWeight(0.5);

        add(toolbar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        reload();
    }

    @Override
    protected Data loadData() {
        return new Data(analyticsService.topInDemand(TOP_K), registrationService.getAllActiveRegistrations());
    }

    @Override
    protected void render(Data data) {
        List<Object[]> demandRows = new ArrayList<>();
        int rank = 1;
        for (CourseDemand d : data.topCourses()) {
            demandRows.add(new Object[]{rank++, d.getCourseCode(), d.getTitle(), d.getCapacity(),
                    d.getRegistered(), d.getWaitlisted(), String.format("%.0f%%", d.getDemandScore() * 100)});
        }
        demandModel.setRows(demandRows);

        List<Object[]> regRows = new ArrayList<>();
        for (Registration r : data.registrations()) {
            regRows.add(new Object[]{r.getRegId(), r.getStudentId(), r.getCourseCode(), UiFormat.dateTime(r.getRegTime())});
        }
        registrationModel.setRows(regRows);
    }
}
