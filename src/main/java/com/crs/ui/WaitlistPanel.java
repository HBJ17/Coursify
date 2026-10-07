package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.WaitlistEntry;
import com.crs.service.CourseService;
import com.crs.service.WaitlistService;
import com.crs.ui.theme.Banner;
import com.crs.ui.theme.Card;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.EmptyState;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.ThinScrollBarUI;
import com.crs.ui.theme.UiButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Waitlists the student is on, one card each, with their position in the queue (1 = next to get a seat). */
public class WaitlistPanel extends RefreshablePanel<List<WaitlistPanel.Item>> {

    record Item(String code, String title, int position, LocalDateTime joined) { }

    private final WaitlistService waitlistService;
    private final CourseService courseService;
    private final Session session;
    private final RegisterController controller;
    private final JPanel list = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public WaitlistPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.waitlistService = ctx.getWaitlistService();
        this.courseService = ctx.getCourseService();
        this.session = session;
        this.controller = new RegisterController(this, ctx.getRegistrationService(), waitlistService, session);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JPanel listTop = new JPanel(new BorderLayout());
        listTop.setOpaque(false);
        listTop.add(list, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(listTop);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        content.setOpaque(false);
        content.add(scroll, "list");
        content.add(new EmptyState(Icons.Name.HOURGLASS, "You're not on any waitlist",
                "When a course is full, choose Join waitlist on Browse Courses and you'll be queued here."), "empty");

        add(new Banner(Icons.Name.INFO,
                "When a seat frees up, the student at position #1 is registered automatically.",
                Chip.Style.PRIMARY), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        reload();
    }

    @Override
    protected List<Item> loadData() {
        String studentId = session.getUserId();
        List<Item> items = new ArrayList<>();
        for (WaitlistEntry entry : waitlistService.getMyWaitlists(studentId)) {
            String code = entry.getCourseCode();
            String title = courseService.findByCode(code).map(c -> c.getTitle()).orElse("");
            items.add(new Item(code, title, waitlistService.positionOf(studentId, code), entry.getJoinedTime()));
        }
        return items;
    }

    @Override
    protected void render(List<Item> items) {
        list.removeAll();
        for (Item item : items) {
            list.add(card(item));
            list.add(Box.createVerticalStrut(10));
        }
        cards.show(content, items.isEmpty() ? "empty" : "list");
        list.revalidate();
        list.repaint();
    }

    private Card card(Item item) {
        Card card = new Card(new BorderLayout(16, 0)).padding(14, 14, 14, 16);

        Card position = new Card(new GridBagLayout()).colors(Theme.WARNING_SOFT, Theme.WARNING_BORDER).padding(0, 0, 0, 0);
        position.setPreferredSize(new Dimension(68, 64));
        JLabel number = new JLabel("#" + item.position());
        number.setFont(Theme.monoBold(22));
        number.setForeground(Theme.WARNING_TEXT);
        JLabel caption = new JLabel("IN LINE");
        caption.setFont(Theme.label(9.5f));
        caption.setForeground(Theme.WARNING_TEXT);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        position.add(number, c);
        position.add(caption, c);

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        heading.setOpaque(false);
        heading.add(new Chip(item.code(), Chip.Style.NEUTRAL).mono());
        JLabel title = new JLabel(item.title());
        title.setFont(Theme.bold(14));
        title.setForeground(Theme.TEXT);
        heading.add(title);

        JLabel joined = new JLabel("Joined " + UiFormat.dateTime(item.joined()),
                Icons.of(Icons.Name.CLOCK, 14, Theme.FAINT), JLabel.LEFT);
        joined.setIconTextGap(6);
        joined.setFont(Theme.font(12));
        joined.setForeground(Theme.MUTED);

        int ahead = item.position() - 1;
        JLabel note = new JLabel(ahead == 0
                ? "You're next: you'll be registered as soon as a seat frees up."
                : ahead + (ahead == 1 ? " student is" : " students are") + " ahead of you.");
        note.setFont(Theme.font(12));
        note.setForeground(ahead == 0 ? Theme.SUCCESS_TEXT : Theme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        for (javax.swing.JComponent part : new javax.swing.JComponent[]{heading, joined, note}) {
            part.setAlignmentX(LEFT_ALIGNMENT);
            text.add(part);
            text.add(Box.createVerticalStrut(5));
        }
        joined.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        note.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        UiButton leave = new UiButton("Leave waitlist", Icons.Name.CLOSE, UiButton.Variant.DANGER);
        leave.addActionListener(e -> controller.leaveWaitlist(item.code(), this::reload));
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(leave);

        card.add(position, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }
}
