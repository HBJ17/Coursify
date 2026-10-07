package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Icons;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/** The student area: sidebar shell with Browse Courses, My Courses and My Waitlists. */
public class StudentHomePanel extends JPanel {

    public StudentHomePanel(AppContext ctx, Session session, ScreenNavigator navigator) {
        super(new BorderLayout());
        String studentId = session.getUserId();

        AppShell shell = new AppShell(ctx, session, navigator);
        shell.addSection("Academic");
        shell.addPage("browse", "Browse Courses", Icons.Name.SEARCH,
                "Browse Courses", "Search the catalogue and register for courses",
                new BrowseCoursesPanel(ctx, session));
        shell.addPage("courses", "My Courses", Icons.Name.BOOK,
                "My Courses", "Your registered courses and weekly timetable",
                new MyCoursesPanel(ctx, session));
        shell.addPage("waitlists", "My Waitlists", Icons.Name.HOURGLASS,
                "My Waitlists", "Courses you are queued for",
                new WaitlistPanel(ctx, session));

        shell.setCounter("courses", () -> ctx.getRegistrationService().getMyCourses(studentId).size(), Chip.Style.NEUTRAL);
        shell.setCounter("waitlists", () -> ctx.getWaitlistService().getMyWaitlists(studentId).size(), Chip.Style.WARNING);

        add(shell, BorderLayout.CENTER);
    }
}
