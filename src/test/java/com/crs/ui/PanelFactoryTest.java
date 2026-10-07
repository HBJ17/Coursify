package com.crs.ui;

import com.crs.app.AppContext;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Builds real screens on the fake services. No window is opened, so this also runs on a headless CI machine. */
class PanelFactoryTest {

    private final AppContext ctx = AppContext.createWithFakes();
    private final Session session = new Session();
    private final List<String> shownScreens = new ArrayList<>();
    private final PanelFactory factory = new PanelFactory(ctx, session, shownScreens::add);

    @Test
    void createsLoginScreen() {
        assertInstanceOf(LoginPanel.class, factory.create(PanelFactory.LOGIN));
    }

    @Test
    void createsStudentScreenWithThreeLiveTabs() throws Exception {
        session.login(ctx.getAuthService().login("S001", "pass"));
        JPanel screen = factory.create(PanelFactory.STUDENT);

        assertInstanceOf(StudentHomePanel.class, screen);
        assertEquals(3, find(screen, RefreshablePanel.class).size());
    }

    @Test
    void createsAdminScreenWithDashboard() throws Exception {
        session.login(ctx.getAuthService().login("A001", "admin"));
        JPanel screen = factory.create(PanelFactory.ADMIN);

        assertEquals(1, find(screen, AdminDashboardPanel.class).size());
    }

    @Test
    void unknownScreenIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> factory.create("NOPE"));
    }

    @Test
    void browseTabLoadsAllCourses() throws Exception {
        session.login(ctx.getAuthService().login("S001", "pass"));
        BrowseCoursesPanel browse = find(factory.create(PanelFactory.STUDENT), BrowseCoursesPanel.class).get(0);
        JTable table = find(browse, JTable.class).get(0);

        int courseCount = ctx.getCourseService().getAllSorted().size();
        waitUntil(() -> table.getModel().getRowCount() == courseCount);
    }

    @Test
    void myCoursesTabRefreshesAfterRegistration() throws Exception {
        session.login(ctx.getAuthService().login("S001", "pass"));
        MyCoursesPanel myCourses = find(factory.create(PanelFactory.STUDENT), MyCoursesPanel.class).get(0);
        JTable table = find(myCourses, JTable.class).get(0);
        waitUntil(() -> table.getModel().getRowCount() == 0);

        ctx.getRegistrationService().register("S001", "CS101"); // publishes REGISTERED, so the panel reloads itself

        waitUntil(() -> table.getModel().getRowCount() == 1);
        assertEquals("CS101", table.getModel().getValueAt(0, 0));
    }

    @Test
    void detachedPanelStopsListening() throws Exception {
        session.login(ctx.getAuthService().login("S001", "pass"));
        MyCoursesPanel myCourses = find(factory.create(PanelFactory.STUDENT), MyCoursesPanel.class).get(0);
        JTable table = find(myCourses, JTable.class).get(0);
        waitUntil(() -> table.getModel().getRowCount() == 0);

        myCourses.detach();
        ctx.getRegistrationService().register("S001", "CS101");
        flushSwing();
        Thread.sleep(200);
        flushSwing();

        assertEquals(0, table.getModel().getRowCount());
    }

    @Test
    void logoutButtonReturnsToLogin() throws Exception {
        session.login(ctx.getAuthService().login("S001", "pass"));
        JPanel screen = factory.create(PanelFactory.STUDENT);
        find(screen, javax.swing.JButton.class).stream()
                .filter(b -> "Log out".equals(b.getText()))
                .findFirst().orElseThrow()
                .doClick();

        assertFalse(session.isLoggedIn());
        assertEquals(List.of(PanelFactory.LOGIN), shownScreens);
    }

    // ---------- helpers ----------

    /** All components of the given type inside root (root included). */
    private static <T> List<T> find(Component root, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(root)) found.add(type.cast(root));
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) found.addAll(find(child, type));
        }
        return found;
    }

    /** Waits (max 3 s) for background loads to finish and the Swing thread to update the table. */
    private static void waitUntil(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            flushSwing();
            if (condition.getAsBoolean()) return;
            Thread.sleep(20);
        }
        fail("Condition not reached within 3 seconds");
    }

    private static void flushSwing() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }
}
