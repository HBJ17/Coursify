package com.crs.ui;

import com.crs.model.Course;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Draw;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.Tables;
import com.crs.ui.theme.Theme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The course table used on Browse Courses, My Courses and the admin Courses page.
 * Cells are painted by hand: code chips, seat bars, prerequisite chips and an action button per row.
 */
public class CourseTable extends JTable implements Tables.RowTint {
    public enum Mode { CATALOG, MY_COURSES, ADMIN }

    /** What the logged-in student can do with a course. */
    public enum Status { OPEN, FULL, REGISTERED, WAITLISTED }

    private enum Col {
        CODE("Code", 100), TITLE("Title", 0), CREDITS("Credits", 96), SEATS("Seats", 170),
        SCHEDULE("Schedule", 160), PREREQS("Prerequisites", 160), ACTION("", 150);

        final String title;
        final int width;

        Col(String title, int width) {
            this.title = title;
            this.width = width;
        }
    }

    private static final Color REGISTERED_TINT = new Color(0xF5FDF8);

    private final Mode mode;
    private final List<Col> cols;
    private List<Course> courses = List.of();
    private Set<String> registered = Set.of();
    private Set<String> waitlisted = Set.of();
    private Consumer<Course> actionHandler = c -> { };

    public CourseTable(Mode mode) {
        this.mode = mode;
        this.cols = switch (mode) {
            case CATALOG -> List.of(Col.CODE, Col.TITLE, Col.CREDITS, Col.SEATS, Col.SCHEDULE, Col.PREREQS, Col.ACTION);
            case MY_COURSES -> List.of(Col.CODE, Col.TITLE, Col.CREDITS, Col.SCHEDULE, Col.ACTION);
            case ADMIN -> List.of(Col.CODE, Col.TITLE, Col.CREDITS, Col.SEATS, Col.SCHEDULE, Col.PREREQS);
        };
        setModel(new Model());
        Tables.style(this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < cols.size(); i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setCellRenderer(new Cell(cols.get(i)));
            int w = cols.get(i).width;
            column.setPreferredWidth(w > 0 ? w : 260);
            if (w > 0) column.setMinWidth(w * 3 / 4);
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row < 0 || !isActionColumn(columnAtPoint(e.getPoint()))) return;
                Course course = courseAt(row);
                if (hasAction(course)) actionHandler.accept(course);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                boolean overButton = row >= 0 && isActionColumn(columnAtPoint(e.getPoint())) && hasAction(courseAt(row));
                setCursor(Cursor.getPredefinedCursor(overButton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setData(List<Course> courses, Set<String> registered, Set<String> waitlisted) {
        this.courses = List.copyOf(courses);
        this.registered = Set.copyOf(registered);
        this.waitlisted = Set.copyOf(waitlisted);
        ((AbstractTableModel) getModel()).fireTableDataChanged();
    }

    /** Called with the row's course when its action button (Register / Join waitlist / Cancel) is clicked. */
    public void onAction(Consumer<Course> handler) {
        this.actionHandler = handler;
    }

    public Course courseAt(int viewRow) {
        return courses.get(convertRowIndexToModel(viewRow));
    }

    public Status statusOf(Course c) {
        if (registered.contains(c.getCode())) return Status.REGISTERED;
        if (waitlisted.contains(c.getCode())) return Status.WAITLISTED;
        return c.isFull() ? Status.FULL : Status.OPEN;
    }

    /** True when the row's button does something (you can't register twice, for example). */
    public boolean hasAction(Course c) {
        return switch (mode) {
            case CATALOG -> statusOf(c) == Status.OPEN || statusOf(c) == Status.FULL;
            case MY_COURSES -> true;
            case ADMIN -> false;
        };
    }

    private boolean isActionColumn(int viewColumn) {
        return viewColumn >= 0 && cols.get(convertColumnIndexToModel(viewColumn)) == Col.ACTION;
    }

    @Override
    public Color tintFor(int viewRow) {
        return mode == Mode.CATALOG && statusOf(courseAt(viewRow)) == Status.REGISTERED ? REGISTERED_TINT : null;
    }

    private class Model extends AbstractTableModel {
        @Override public int getRowCount() { return courses.size(); }
        @Override public int getColumnCount() { return cols.size(); }
        @Override public String getColumnName(int column) { return cols.get(column).title; }

        @Override
        public Object getValueAt(int row, int column) {
            Course c = courses.get(row);
            return switch (cols.get(column)) {
                case CODE -> c.getCode();
                case TITLE -> c.getTitle();
                case CREDITS -> c.getCredits();
                case SCHEDULE -> UiFormat.timeSlot(c);
                default -> c;
            };
        }
    }

    /** Paints one cell. Swing reuses the same component for every row of a column (flyweight). */
    private class Cell extends JComponent implements TableCellRenderer {
        private final Col col;
        private Course course;
        private int row;
        private boolean selected;

        Cell(Col col) {
            this.col = col;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            this.course = courseAt(row);
            this.row = row;
            this.selected = selected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Draw.smooth(g);
            int w = getWidth(), cy = getHeight() / 2;
            g2.setColor(Tables.rowBackground(CourseTable.this, row, selected));
            g2.fillRect(0, 0, w, getHeight());
            switch (col) {
                case CODE -> Draw.chip(g2, course.getCode(), Theme.monoBold(11.5f), Chip.Style.NEUTRAL, 16, cy);
                case TITLE -> Draw.text(g2, Draw.fit(g2, course.getTitle(), Theme.bold(13), w - 24),
                        Theme.bold(13), Theme.TEXT, 16, cy);
                case CREDITS -> Draw.text(g2, course.getCredits() + " cr", Theme.mono(12.5f), Theme.TEXT_SECONDARY, 16, cy);
                case SEATS -> paintSeats(g2, w, cy);
                case SCHEDULE -> paintSchedule(g2, cy);
                case PREREQS -> paintPrereqs(g2, w, cy);
                case ACTION -> paintAction(g2, w, cy);
            }
            g2.dispose();
        }

        private void paintSeats(Graphics2D g, int w, int cy) {
            int left = course.getSeatsLeft(), capacity = course.getCapacity();
            double ratio = capacity == 0 ? 0 : left / (double) capacity;
            Color color = left == 0 ? Theme.DANGER : ratio <= 0.34 ? Theme.WARNING : Theme.SUCCESS;
            String word = left == 0 ? "Full" : ratio <= 0.34 ? "Filling" : "Open";

            Draw.text(g, left + " / " + capacity + " left", Theme.mono(11.5f),
                    left == 0 ? Theme.DANGER_TEXT : Theme.TEXT_SECONDARY, 16, cy - 7);
            int ww = Draw.width(g, word, Theme.bold(10.5f));
            Draw.text(g, word, Theme.bold(10.5f), color, w - 16 - ww, cy - 7);
            int barW = w - 32;
            Draw.roundRect(g, 16, cy + 5, barW, 5, 2.5, Theme.SURFACE_MUTED, null);
            if (left > 0) Draw.roundRect(g, 16, cy + 5, Math.max(5, barW * ratio), 5, 2.5, color, null);
        }

        private void paintSchedule(Graphics2D g, int cy) {
            if (course.getTimeSlot() == null) {
                Draw.text(g, "No fixed slot", Theme.font(12.5f), Theme.FAINT, 16, cy);
                return;
            }
            Icons.of(Icons.Name.CLOCK, 14, Theme.FAINT).paintIcon(this, g, 16, cy - 7);
            Draw.text(g, UiFormat.timeSlot(course), Theme.font(12.5f), Theme.TEXT_SECONDARY, 36, cy);
        }

        private void paintPrereqs(Graphics2D g, int w, int cy) {
            List<String> prereqs = course.getPrerequisites();
            if (prereqs.isEmpty()) {
                Draw.text(g, "—", Theme.font(13), Theme.FAINT, 16, cy);
                return;
            }
            int x = 16;
            for (int i = 0; i < prereqs.size(); i++) {
                int chipW = Draw.width(g, prereqs.get(i), Theme.monoBold(11)) + 12;
                if (x + chipW > w - 8) {
                    Draw.text(g, "+" + (prereqs.size() - i), Theme.bold(11), Theme.MUTED, x, cy);
                    break;
                }
                x += Draw.chip(g, prereqs.get(i), Theme.monoBold(11), Chip.Style.NEUTRAL, x, cy) + 4;
            }
        }

        private void paintAction(Graphics2D g, int w, int cy) {
            if (mode == Mode.MY_COURSES) {
                button(g, "Cancel", Icons.Name.CLOSE, Theme.SURFACE, Theme.DANGER_BORDER, Theme.DANGER_TEXT, w, cy);
                return;
            }
            switch (statusOf(course)) {
                case OPEN -> button(g, "Register", null, Theme.PRIMARY, null, Color.WHITE, w, cy);
                case FULL -> button(g, "Join waitlist", Icons.Name.HOURGLASS, Theme.WARNING_SOFT, Theme.WARNING_BORDER,
                        Theme.WARNING_TEXT, w, cy);
                case REGISTERED -> chipRight(g, "Registered", Icons.Name.CHECK, Chip.Style.SUCCESS, w, cy);
                case WAITLISTED -> chipRight(g, "On waitlist", Icons.Name.HOURGLASS, Chip.Style.WARNING, w, cy);
            }
        }

        /** A right-aligned button look-alike (the table handles the click). */
        private void button(Graphics2D g, String text, Icons.Name icon, Color fill, Color stroke, Color fg, int w, int cy) {
            Font font = Theme.bold(12);
            int iconW = icon == null ? 0 : 18;
            int bw = Draw.width(g, text, font) + 24 + iconW, bh = 28;
            int x = w - 16 - bw;
            Draw.roundRect(g, x, cy - bh / 2.0, bw, bh, 6, fill, stroke);
            if (icon != null) Icons.of(icon, 14, fg).paintIcon(this, g, x + 11, cy - 7);
            Draw.text(g, text, font, fg, x + 12 + iconW, cy);
        }

        private void chipRight(Graphics2D g, String text, Icons.Name icon, Chip.Style style, int w, int cy) {
            Color[] c = Chip.colors(style);
            Font font = Theme.bold(11.5f);
            int bw = Draw.width(g, text, font) + 34, bh = 26;
            int x = w - 16 - bw;
            Draw.roundRect(g, x, cy - bh / 2.0, bw, bh, 6, c[1], c[2]);
            Icons.of(icon, 13, c[0]).paintIcon(this, g, x + 10, cy - 6);
            Draw.text(g, text, font, c[0], x + 26, cy);
        }
    }
}
