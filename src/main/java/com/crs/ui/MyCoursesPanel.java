package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.Course;
import com.crs.service.RegistrationService;
import com.crs.service.WaitlistService;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.EmptyState;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.StatCard;
import com.crs.ui.theme.Tables;
import com.crs.ui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The student's registered courses: summary tiles, a weekly timetable and a table with Cancel buttons. */
public class MyCoursesPanel extends RefreshablePanel<MyCoursesPanel.Data> {

    record Data(List<Course> mine, int waitlists) { }

    private final RegistrationService registrationService;
    private final WaitlistService waitlistService;
    private final Session session;
    private final CourseTable table = new CourseTable(CourseTable.Mode.MY_COURSES);
    private final TimetableStrip timetable = new TimetableStrip();
    private final StatCard coursesCard = new StatCard("Registered courses", Icons.Name.BOOK, Chip.Style.PRIMARY);
    private final StatCard creditsCard = new StatCard("Total credits", Icons.Name.BOOKMARK, Chip.Style.SUCCESS);
    private final StatCard waitlistCard = new StatCard("Waitlists", Icons.Name.HOURGLASS, Chip.Style.WARNING);
    private final CardLayout listCards = new CardLayout();
    private final JPanel listHolder = new JPanel(listCards);
    private final JLabel totalLabel = new JLabel();

    public MyCoursesPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.registrationService = ctx.getRegistrationService();
        this.waitlistService = ctx.getWaitlistService();
        this.session = session;
        RegisterController controller = new RegisterController(this, registrationService, waitlistService, session);
        table.onAction(course -> controller.cancel(course.getCode()));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(coursesCard);
        stats.add(creditsCard);
        stats.add(waitlistCard);

        Card timetableCard = new Card(new BorderLayout(0, 10)).padding(14, 16, 12, 16);
        JLabel title = new JLabel("Weekly timetable");
        title.setFont(Theme.bold(14));
        title.setForeground(Theme.TEXT);
        timetableCard.add(title, BorderLayout.NORTH);
        timetableCard.add(timetable, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(stats);
        north.add(Box.createVerticalStrut(16));
        north.add(timetableCard);

        add(north, BorderLayout.NORTH);
        add(tableCard(), BorderLayout.CENTER);
        reload();
    }

    private Card tableCard() {
        Card card = new Card(new BorderLayout()).padding(1, 1, 1, 1);
        listHolder.setOpaque(false);
        listHolder.add(Tables.scroll(table), "table");
        listHolder.add(new EmptyState(Icons.Name.BOOK, "No courses yet",
                "Browse the catalogue and register for your first course."), "empty");
        card.add(listHolder, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 36));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        JLabel hint = new JLabel("Cancelling frees your seat for the next student on the waitlist.");
        for (JLabel l : new JLabel[]{hint, totalLabel}) {
            l.setFont(Theme.font(12));
            l.setForeground(Theme.MUTED);
        }
        footer.add(hint, BorderLayout.WEST);
        footer.add(totalLabel, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    @Override
    protected Data loadData() {
        String studentId = session.getUserId();
        return new Data(registrationService.getMyCourses(studentId), waitlistService.getMyWaitlists(studentId).size());
    }

    @Override
    protected void render(Data data) {
        List<Course> mine = data.mine();
        Set<String> codes = mine.stream().map(Course::getCode).collect(Collectors.toSet());
        table.setData(mine, codes, Set.of());
        timetable.setCourses(mine);
        listCards.show(listHolder, mine.isEmpty() ? "empty" : "table");

        int credits = mine.stream().mapToInt(Course::getCredits).sum();
        coursesCard.setValue(String.valueOf(mine.size()), mine.size() == 1 ? "course" : "courses");
        creditsCard.setValue(String.valueOf(credits), "credits");
        waitlistCard.setValue(String.valueOf(data.waitlists()), "queued");
        totalLabel.setText(mine.size() + (mine.size() == 1 ? " course" : " courses") + "  ·  " + credits + " credits");
    }
}
