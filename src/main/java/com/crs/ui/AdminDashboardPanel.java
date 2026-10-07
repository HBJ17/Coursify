package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.AdminController;
import com.crs.model.CourseDemand;
import com.crs.model.Registration;
import com.crs.service.AnalyticsService;
import com.crs.service.RegistrationService;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.StatCard;
import com.crs.ui.theme.Tables;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Admin dashboard: KPI tiles, the most in-demand courses (ranked by the analytics max-heap)
 * and every active registration. Everything refreshes automatically on each registration event.
 */
public class AdminDashboardPanel extends RefreshablePanel<AdminDashboardPanel.Data> {
    static final int TOP_K = 5;

    /** Everything the dashboard shows, loaded together in the background. */
    record Data(List<CourseDemand> demand, List<Registration> registrations) { }

    private final AnalyticsService analyticsService;
    private final RegistrationService registrationService;
    private final AdminController controller;

    private final StatCard coursesCard = new StatCard("Total courses", Icons.Name.BOOK, Chip.Style.PRIMARY);
    private final StatCard activeCard = new StatCard("Active registrations", Icons.Name.CHECK_CIRCLE, Chip.Style.SUCCESS);
    private final StatCard waitingCard = new StatCard("Students waiting", Icons.Name.HOURGLASS, Chip.Style.WARNING);
    private final StatCard topCard = new StatCard("Most wanted", Icons.Name.TRENDING, Chip.Style.ORANGE);
    private final DemandChart chart = new DemandChart();
    private final DefaultTableModel registrationModel = new DefaultTableModel(
            new String[]{"Reg ID", "Student", "Course", "Registered at"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final Chip regCount = new Chip("0", Chip.Style.NEUTRAL).mono();

    public AdminDashboardPanel(AppContext ctx) {
        super(ctx.getSubject());
        this.analyticsService = ctx.getAnalyticsService();
        this.registrationService = ctx.getRegistrationService();
        this.controller = new AdminController(this, ctx.getCourseService());

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(coursesCard);
        stats.add(activeCard);
        stats.add(waitingCard);
        stats.add(topCard);

        Card demandCard = new Card(new BorderLayout(0, 12)).padding(14, 16, 12, 16);
        demandCard.add(cardTitle("Top " + TOP_K + " in-demand courses",
                "Demand = (registered + waitlisted) ÷ capacity", null), BorderLayout.NORTH);
        demandCard.add(chart, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(stats);
        north.add(Box.createVerticalStrut(16));
        north.add(demandCard);

        add(north, BorderLayout.NORTH);
        add(registrationsCard(), BorderLayout.CENTER);
        reload();
    }

    /** Buttons for the page header: Refresh and Add course. */
    public JComponent[] headerActions() {
        UiButton refresh = new UiButton("Refresh", Icons.Name.REFRESH, UiButton.Variant.GHOST);
        refresh.addActionListener(e -> reload());
        UiButton add = new UiButton("Add course", Icons.Name.PLUS, UiButton.Variant.PRIMARY);
        add.addActionListener(e -> controller.addCourse(this::reload));
        return new JComponent[]{refresh, add};
    }

    private Card registrationsCard() {
        JTable table = new JTable(registrationModel);
        Tables.style(table);
        table.getColumnModel().getColumn(0).setCellRenderer(new Tables.MonoCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new Tables.MonoCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new Tables.ChipCell());

        Card card = new Card(new BorderLayout()).padding(1, 1, 1, 1);
        JPanel top = cardTitle("All active registrations", null, regCount);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        card.add(top, BorderLayout.NORTH);
        card.add(Tables.scroll(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 30));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        JLabel hint = new JLabel("Updates live as students register, cancel and get promoted.");
        hint.setFont(Theme.font(12));
        hint.setForeground(Theme.MUTED);
        footer.add(hint, BorderLayout.WEST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private static JPanel cardTitle(String title, String subtitle, JComponent right) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.bold(14));
        t.setForeground(Theme.TEXT);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(t, BorderLayout.NORTH);
        if (subtitle != null) {
            JLabel s = new JLabel(subtitle);
            s.setFont(Theme.font(12));
            s.setForeground(Theme.MUTED);
            left.add(s, BorderLayout.SOUTH);
        }
        row.add(left, BorderLayout.WEST);
        if (right != null) {
            JPanel holder = new JPanel(new BorderLayout());
            holder.setOpaque(false);
            holder.add(right, BorderLayout.NORTH);
            row.add(holder, BorderLayout.EAST);
        }
        return row;
    }

    @Override
    protected Data loadData() {
        return new Data(analyticsService.topInDemand(Integer.MAX_VALUE), registrationService.getAllActiveRegistrations());
    }

    @Override
    protected void render(Data data) {
        List<CourseDemand> demand = data.demand();
        chart.setRows(demand.subList(0, Math.min(TOP_K, demand.size())));

        int waiting = demand.stream().mapToInt(CourseDemand::getWaitlisted).sum();
        coursesCard.setValue(String.valueOf(demand.size()), "in catalogue");
        activeCard.setValue(String.valueOf(data.registrations().size()), "seats taken");
        waitingCard.setValue(String.valueOf(waiting), waiting == 1 ? "student" : "students");
        if (!demand.isEmpty() && demand.get(0).getDemandScore() > 0) {
            CourseDemand top = demand.get(0);
            topCard.setValue(top.getCourseCode(), Math.round(top.getDemandScore() * 100) + "% demand");
        } else {
            topCard.setValue("—", "no demand yet");
        }

        List<Object[]> rows = new ArrayList<>();
        List<Registration> newestFirst = new ArrayList<>(data.registrations());
        newestFirst.sort((a, b) -> b.getRegTime().compareTo(a.getRegTime()));
        for (Registration r : newestFirst) {
            rows.add(new Object[]{r.getRegId(), r.getStudentId(), r.getCourseCode(), UiFormat.dateTime(r.getRegTime())});
        }
        registrationModel.setRowCount(0);
        rows.forEach(registrationModel::addRow);
        regCount.setText(String.valueOf(rows.size()));
    }
}
