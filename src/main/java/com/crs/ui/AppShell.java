package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.model.Person;
import com.crs.model.Student;
import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import com.crs.observer.RegistrationSubject;
import com.crs.ui.theme.Avatar;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Draw;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * The frame around every logged-in screen: a sidebar with navigation and the user's profile,
 * a header with the page title, the page itself, and a status bar at the bottom.
 * Pages are swapped with a CardLayout. Nav badges (e.g. "2" next to My Courses) refresh on every event.
 */
public class AppShell extends JPanel implements RegistrationListener, LiveComponent {
    private final RegistrationSubject subject;
    private final JPanel nav = new JPanel();
    private final CardLayout pageCards = new CardLayout();
    private final JPanel pages = new JPanel(pageCards);
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final Map<String, Page> pageMap = new LinkedHashMap<>();
    private final Map<String, Callable<Integer>> counters = new LinkedHashMap<>();

    private record Page(NavItem item, String title, String subtitle, JComponent[] actions) { }

    public AppShell(AppContext ctx, Session session, ScreenNavigator navigator) {
        super(new BorderLayout());
        this.subject = ctx.getSubject();
        setBackground(Theme.BG);

        add(sidebar(session, navigator), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.add(header(), BorderLayout.NORTH);
        pages.setBackground(Theme.BG);
        main.add(pages, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        add(statusBar(session.getUser()), BorderLayout.SOUTH);

        subject.addListener(this);
    }

    /** Small uppercase heading in the sidebar, e.g. "ACADEMIC". */
    public void addSection(String name) {
        JLabel label = new JLabel(name.toUpperCase());
        label.setFont(Theme.label(10));
        label.setForeground(Theme.FAINT);
        label.setBorder(BorderFactory.createEmptyBorder(nav.getComponentCount() == 0 ? 4 : 16, 12, 6, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(label);
    }

    /** Adds a page with its sidebar entry. The first page added is shown first. */
    public void addPage(String key, String navLabel, Icons.Name icon, String title, String subtitle,
                        JComponent content, JComponent... actions) {
        NavItem item = new NavItem(navLabel, icon, () -> showPage(key));
        item.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(item);
        nav.add(Box.createVerticalStrut(2));
        pageMap.put(key, new Page(item, title, subtitle, actions));
        pages.add(content, key);
        if (pageMap.size() == 1) showPage(key);
    }

    /** A number shown next to the nav entry, e.g. how many courses you're registered for. */
    public void setCounter(String key, Callable<Integer> counter, Chip.Style style) {
        counters.put(key, counter);
        pageMap.get(key).item().badgeStyle = style;
        refreshCounters();
    }

    public void showPage(String key) {
        Page page = pageMap.get(key);
        pageMap.values().forEach(p -> p.item().setActive(p == page));
        titleLabel.setText(page.title());
        subtitleLabel.setText(page.subtitle());
        headerActions.removeAll();
        for (JComponent action : page.actions()) headerActions.add(action);
        headerActions.revalidate();
        headerActions.repaint();
        pageCards.show(pages, key);
    }

    @Override
    public void onRegistrationChanged(RegistrationEvent event) {
        SwingUtilities.invokeLater(this::refreshCounters);
    }

    @Override
    public void detach() {
        subject.removeListener(this);
    }

    /** Recounts every badge in the background, then updates the sidebar. */
    public void refreshCounters() {
        if (counters.isEmpty()) return;
        Map<String, Callable<Integer>> snapshot = new LinkedHashMap<>(counters);
        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                Map<String, Integer> result = new LinkedHashMap<>();
                for (Map.Entry<String, Callable<Integer>> e : snapshot.entrySet()) result.put(e.getKey(), e.getValue().call());
                return result;
            }

            @Override
            protected void done() {
                try {
                    get().forEach((key, count) -> pageMap.get(key).item().setBadge(String.valueOf(count)));
                } catch (ExecutionException e) {
                    // counts are only decoration; the page itself shows any real error
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------- building blocks

    private JComponent sidebar(Session session, ScreenNavigator navigator) {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SURFACE);
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(240, 60));
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.DIVIDER),
                BorderFactory.createEmptyBorder(12, 6, 0, 0)));
        Card tile = new Card(new GridBagLayout()).colors(Theme.PRIMARY_SOFT, null).radius(8).padding(0, 0, 0, 0);
        tile.setPreferredSize(new Dimension(34, 34));
        tile.add(new JLabel(Icons.of(Icons.Name.SCHOOL, 20, Theme.PRIMARY)));
        JLabel name = new JLabel("<html><b style='font-size:13px'>Coursify</b><br>"
                + "<span style='color:#94A3B8;font-size:9px'>Course Registration</span></html>");
        name.setFont(Theme.font(13));
        name.setForeground(Theme.TEXT);
        brand.add(tile);
        brand.add(name);

        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        JPanel navHolder = new JPanel(new BorderLayout());
        navHolder.setOpaque(false);
        navHolder.add(nav, BorderLayout.NORTH);

        side.add(brand, BorderLayout.NORTH);
        side.add(navHolder, BorderLayout.CENTER);
        side.add(profile(session, navigator), BorderLayout.SOUTH);
        return side;
    }

    private static JComponent profile(Session session, ScreenNavigator navigator) {
        Person user = session.getUser();

        Card card = new Card(new BorderLayout(0, 10)).padding(10, 10, 10, 10);
        JPanel who = new JPanel(new BorderLayout(10, 0));
        who.setOpaque(false);
        who.add(new Avatar(user.getName(), 34), BorderLayout.WEST);
        JLabel name = new JLabel(user.getName());
        name.setFont(Theme.bold(13));
        name.setForeground(Theme.TEXT);
        JLabel id = new JLabel("ID: " + user.getId());
        id.setFont(Theme.mono(11));
        id.setForeground(Theme.FAINT);
        JPanel names = new JPanel(new BorderLayout());
        names.setOpaque(false);
        names.add(name, BorderLayout.NORTH);
        names.add(id, BorderLayout.SOUTH);
        who.add(names, BorderLayout.CENTER);

        Chip chip = user instanceof Student s
                ? new Chip("●  CGPA " + s.getCgpa(), Chip.Style.SUCCESS).pill()
                : new Chip("Administrator", Chip.Style.PRIMARY).pill();
        UiButton logout = new UiButton("Log out", Icons.Name.LOGOUT, UiButton.Variant.DANGER_LINK);
        logout.addActionListener(e -> {
            session.logout();
            navigator.showScreen(PanelFactory.LOGIN);
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.DIVIDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));
        JPanel chipHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chipHolder.setOpaque(false);
        chipHolder.add(chip);
        bottom.add(chipHolder, BorderLayout.WEST);
        bottom.add(logout, BorderLayout.EAST);

        card.add(who, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(new Color(0xFBFCFE));
        holder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.DIVIDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        holder.add(card);
        return holder;
    }

    private JComponent header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setPreferredSize(new Dimension(0, 62));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        titleLabel.setFont(Theme.bold(17));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6)); // room for the last letter
        titleLabel.setForeground(Theme.TEXT);
        subtitleLabel.setFont(Theme.font(12));
        subtitleLabel.setForeground(Theme.MUTED);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Box.createVerticalGlue());
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitleLabel);
        titles.add(Box.createVerticalGlue());
        headerActions.setOpaque(false);
        JPanel actionsHolder = new JPanel(new GridBagLayout());
        actionsHolder.setOpaque(false);
        actionsHolder.add(headerActions);
        header.add(titles, BorderLayout.WEST);
        header.add(actionsHolder, BorderLayout.EAST);
        return header;
    }

    private static JComponent statusBar(Person user) {
        boolean oracle = AppShell.class.getResource("/db.properties") != null;
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE_MUTED);
        bar.setPreferredSize(new Dimension(0, 26));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        JLabel left = new JLabel(oracle ? "Data: Oracle database" : "Data: in-memory sample data",
                new StatusDot(oracle ? Theme.SUCCESS : Theme.WARNING), JLabel.LEFT);
        left.setIconTextGap(7);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH));
        JLabel right = new JLabel(user.getName() + " (" + user.getId() + ")   ·   " + today);
        for (JLabel l : new JLabel[]{left, right}) {
            l.setFont(Theme.mono(11));
            l.setForeground(Theme.MUTED);
        }
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /** Little coloured circle used in the status bar. */
    private record StatusDot(Color color) implements javax.swing.Icon {
        @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Draw.smooth(g);
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y + 1, 7, 7));
            g2.dispose();
        }
        @Override public int getIconWidth() { return 7; }
        @Override public int getIconHeight() { return 9; }
    }

    /** One clickable sidebar entry. Active = indigo tint with a bar on the left. */
    static class NavItem extends JComponent {
        private final String label;
        private final Icons.Name icon;
        private boolean active;
        private boolean hover;
        private String badge;
        private Chip.Style badgeStyle = Chip.Style.NEUTRAL;

        NavItem(String label, Icons.Name icon, Runnable onClick) {
            this.label = label;
            this.icon = icon;
            setPreferredSize(new Dimension(224, 36));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) { this.active = active; repaint(); }
        void setBadge(String badge) { this.badge = badge; repaint(); }

        String getLabel() { return label; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Draw.smooth(g);
            int w = getWidth(), h = getHeight();
            if (active) {
                Draw.roundRect(g2, 0, 0, w, h, 7, Theme.PRIMARY_SOFT, null);
                g2.setColor(Theme.PRIMARY);
                g2.fillRect(0, 6, 3, h - 12);
            } else if (hover) {
                Draw.roundRect(g2, 0, 0, w, h, 7, Theme.SURFACE_MUTED, null);
            }
            Color fg = active ? Theme.PRIMARY : Theme.TEXT_SECONDARY;
            Icons.of(icon, 18, active ? Theme.PRIMARY : Theme.FAINT).paintIcon(this, g2, 12, (h - 18) / 2);
            Draw.text(g2, label, active ? Theme.bold(13) : Theme.font(13), fg, 40, h / 2);
            if (badge != null && !badge.equals("0")) {
                int bw = Draw.width(g2, badge, Theme.monoBold(11)) + 12;
                Draw.chip(g2, badge, Theme.monoBold(11), badgeStyle, w - bw - 10, h / 2);
            }
            g2.dispose();
        }
    }
}
