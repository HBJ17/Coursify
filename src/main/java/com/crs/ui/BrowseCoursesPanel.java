package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.Course;
import com.crs.model.WaitlistEntry;
import com.crs.service.CourseService;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import com.crs.ui.theme.Banner;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.InputField;
import com.crs.ui.theme.StatCard;
import com.crs.ui.theme.Tables;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import com.crs.ui.theme.UiComboBox;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Course catalogue: summary tiles, a live search box with a credits filter, and the course table
 * where each row has its own Register / Join waitlist button.
 */
public class BrowseCoursesPanel extends RefreshablePanel<BrowseCoursesPanel.Data> {

    /** Everything this page shows, loaded together in the background. */
    record Data(List<Course> matches, List<Course> catalogue, List<Course> mine, Set<String> waitlisted) { }

    private static final String ALL_CREDITS = "All credits";

    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final WaitlistService waitlistService;
    private final Session session;
    private final RegisterController controller;

    private final InputField searchField = new InputField("Search by code or title (e.g. CS102, Data Structures)", Icons.Name.SEARCH);
    private final UiComboBox<String> creditsBox = new UiComboBox<>(ALL_CREDITS);
    private final CourseTable table = new CourseTable(CourseTable.Mode.CATALOG);
    private final StatCard creditsCard = new StatCard("Enrolled credits", Icons.Name.BOOKMARK, Chip.Style.PRIMARY);
    private final StatCard registeredCard = new StatCard("Registered courses", Icons.Name.CHECK_CIRCLE, Chip.Style.SUCCESS);
    private final StatCard waitlistCard = new StatCard("Waitlists", Icons.Name.HOURGLASS, Chip.Style.WARNING);
    private final StatCard openCard = new StatCard("Open courses", Icons.Name.BOOK, Chip.Style.NEUTRAL);
    private final JLabel countLabel = new JLabel();
    private final JLabel footerLabel = new JLabel();

    private volatile String query = ""; // written on the Swing thread, read by the background loader
    private Data lastData;
    private boolean updatingFilter;

    public BrowseCoursesPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.courseService = ctx.getCourseService();
        this.registrationService = ctx.getRegistrationService();
        this.waitlistService = ctx.getWaitlistService();
        this.session = session;
        this.controller = new RegisterController(this, registrationService, waitlistService, session);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(creditsCard);
        stats.add(registeredCard);
        stats.add(waitlistCard);
        stats.add(openCard);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(stats);
        north.add(Box.createVerticalStrut(16));
        north.add(toolbar());

        add(north, BorderLayout.NORTH);
        add(tableCard(), BorderLayout.CENTER);
        setFooter(new Banner(Icons.Name.INFO,
                "Real-time checks: time clashes, prerequisites and seat limits are verified the moment you register.",
                Chip.Style.PRIMARY));

        // Live search: every key press reloads the table with the new query.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        creditsBox.addActionListener(e -> {
            if (!updatingFilter && lastData != null) render(lastData);
        });
        table.onAction(course -> controller.register(course.getCode()));
        table.addMouseListener(new MouseAdapter() {      // double-click a row = Register
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row >= 0 && table.hasAction(table.courseAt(row))) {
                    controller.register(table.courseAt(row).getCode());
                }
            }
        });
        reload();
    }

    private JPanel toolbar() {
        Card bar = new Card(new BorderLayout(10, 0)).padding(10, 10, 10, 12);
        creditsBox.setPreferredSize(new Dimension(140, 36));
        UiButton reset = new UiButton("Reset", Icons.Name.REFRESH, UiButton.Variant.GHOST);
        reset.addActionListener(e -> {
            searchField.setText("");
            creditsBox.setSelectedItem(ALL_CREDITS);
        });
        countLabel.setFont(Theme.mono(12));
        countLabel.setForeground(Theme.MUTED);
        countLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 12, 0, 0)));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(creditsBox);
        right.add(reset);
        right.add(countLabel);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, bar.getPreferredSize().height));
        return bar;
    }

    private Card tableCard() {
        Card card = new Card(new BorderLayout()).padding(1, 1, 1, 1);
        card.add(Tables.scroll(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 36));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        JLabel hint = new JLabel("Tip: double-click a row to register");
        for (JLabel l : new JLabel[]{hint, footerLabel}) {
            l.setFont(Theme.font(12));
            l.setForeground(Theme.MUTED);
        }
        footer.add(hint, BorderLayout.WEST);
        footer.add(footerLabel, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void onSearchChanged() {
        query = searchField.getText();
        reload();
    }

    @Override
    protected Data loadData() {
        String studentId = session.getUserId();
        Set<String> waitlisted = waitlistService.getMyWaitlists(studentId).stream()
                .map(WaitlistEntry::getCourseCode).collect(Collectors.toSet());
        return new Data(courseService.search(query), courseService.getAllSorted(),
                registrationService.getMyCourses(studentId), waitlisted);
    }

    @Override
    protected void render(Data data) {
        lastData = data;
        updateCreditOptions(data.catalogue());

        Object choice = creditsBox.getSelectedItem();
        List<Course> shown = new ArrayList<>();
        for (Course c : data.matches()) {
            if (ALL_CREDITS.equals(choice) || (c.getCredits() + " credits").equals(choice)) shown.add(c);
        }
        Set<String> mine = data.mine().stream().map(Course::getCode).collect(Collectors.toSet());
        table.setData(shown, mine, data.waitlisted());

        int credits = data.mine().stream().mapToInt(Course::getCredits).sum();
        long open = data.catalogue().stream().filter(c -> !c.isFull()).count();
        creditsCard.setValue(String.valueOf(credits), "credits");
        registeredCard.setValue(String.valueOf(data.mine().size()), data.mine().size() == 1 ? "course" : "courses");
        waitlistCard.setValue(String.valueOf(data.waitlisted().size()), "queued");
        openCard.setValue(String.valueOf(open), "of " + data.catalogue().size());
        countLabel.setText(shown.size() + (shown.size() == 1 ? " course" : " courses"));
        footerLabel.setText("Showing " + shown.size() + " of " + data.catalogue().size() + " courses");
    }

    /** Fills the credits dropdown with the credit values that exist in the catalogue. */
    private void updateCreditOptions(List<Course> catalogue) {
        Set<Integer> values = new TreeSet<>();
        catalogue.forEach(c -> values.add(c.getCredits()));
        if (creditsBox.getItemCount() == values.size() + 1) return;
        updatingFilter = true;
        Object selected = creditsBox.getSelectedItem();
        creditsBox.removeAllItems();
        creditsBox.addItem(ALL_CREDITS);
        values.forEach(v -> creditsBox.addItem(v + " credits"));
        creditsBox.setSelectedItem(selected);
        updatingFilter = false;
    }
}
