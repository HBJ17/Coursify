package com.crs.ui;

import com.crs.model.Course;
import com.crs.model.TimeSlot;
import com.crs.ui.theme.Draw;
import com.crs.ui.theme.Theme;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.DayOfWeek;
import java.util.List;
import javax.swing.JComponent;

/** Monday-to-Friday grid with one coloured block per registered course. */
public class TimetableStrip extends JComponent {
    private static final DayOfWeek[] DAYS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
    private static final int GUTTER = 48;
    private static final int HEADER = 24;

    private List<Course> courses = List.of();

    public TimetableStrip() {
        setPreferredSize(new Dimension(600, 190));
    }

    public void setCourses(List<Course> courses) {
        this.courses = List.copyOf(courses);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        int w = getWidth(), h = getHeight();

        // Hours shown: 08:00-18:00, stretched if a course starts earlier or ends later.
        int startHour = 8, endHour = 18;
        for (Course c : courses) {
            TimeSlot slot = c.getTimeSlot();
            if (slot == null) continue;
            startHour = Math.min(startHour, slot.getStart().getHour());
            endHour = Math.max(endHour, slot.getEnd().getHour() + (slot.getEnd().getMinute() > 0 ? 1 : 0));
        }
        int gridTop = HEADER, gridH = h - HEADER - 4;
        double perMinute = gridH / ((endHour - startHour) * 60.0);
        double colW = (w - GUTTER) / (double) DAYS.length;

        for (int i = 0; i < DAYS.length; i++) {
            int x = (int) (GUTTER + i * colW);
            Draw.text(g2, DAYS[i].toString().substring(0, 3), Theme.label(10.5f), Theme.MUTED, x + 8, HEADER / 2);
            g2.setColor(Theme.DIVIDER);
            g2.drawLine(x, gridTop, x, gridTop + gridH);
        }
        for (int hour = startHour; hour <= endHour; hour += 2) {
            int y = (int) (gridTop + (hour - startHour) * 60 * perMinute);
            g2.setColor(Theme.DIVIDER);
            g2.drawLine(GUTTER, y, w, y);
            Draw.text(g2, String.format("%02d:00", hour), Theme.mono(10), Theme.FAINT, 0, y);
        }

        boolean any = false;
        for (Course c : courses) {
            TimeSlot slot = c.getTimeSlot();
            if (slot == null || slot.getDay().getValue() > 5) continue;
            any = true;
            int day = slot.getDay().getValue() - 1;
            double x = GUTTER + day * colW + 4;
            double y = gridTop + (slot.getStart().toSecondOfDay() / 60.0 - startHour * 60) * perMinute;
            double bh = Math.max(20, (slot.getEnd().toSecondOfDay() - slot.getStart().toSecondOfDay()) / 60.0 * perMinute);
            Draw.roundRect(g2, x, y, colW - 8, bh, 6, Theme.PRIMARY_SOFT, Theme.PRIMARY_SOFT_BORDER);
            g2.setColor(Theme.PRIMARY);
            g2.fill(Draw.round(x, y, 3, bh, 1.5));
            int textY = (int) (y + Math.min(bh / 2, 11));
            Draw.text(g2, c.getCode(), Theme.monoBold(11), Theme.PRIMARY_PRESSED, (int) x + 9, textY);
            if (bh >= 30) {
                Draw.text(g2, slot.getStart() + "–" + slot.getEnd(), Theme.font(10.5f), Theme.MUTED,
                        (int) x + 9, textY + 14);
            }
        }
        if (!any) {
            String msg = "Your timetable appears here once you register for a course.";
            int mw = Draw.width(g2, msg, Theme.font(12.5f));
            Draw.text(g2, msg, Theme.font(12.5f), Theme.FAINT, GUTTER + (w - GUTTER - mw) / 2, gridTop + gridH / 2);
        }
        g2.dispose();
    }
}
