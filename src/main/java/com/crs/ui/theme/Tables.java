package com.crs.ui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/** Gives any JTable the Coursify look: tall rows, light dividers, uppercase header, hover highlight. */
public final class Tables {
    private static final String HOVER_ROW = "coursify.hoverRow";
    private static final Color HOVER = new Color(0xF8FAFC);

    /** Tables that want some rows tinted (e.g. courses you are registered for) implement this. */
    public interface RowTint {
        Color tintFor(int viewRow);
    }

    private Tables() { }

    public static void style(JTable table) {
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.DIVIDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.PRIMARY_SOFT);
        table.setSelectionForeground(Theme.TEXT);
        table.setFont(Theme.font(13));
        table.setForeground(Theme.TEXT);
        table.setBackground(Theme.SURFACE);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new TextCell());
        table.setDefaultRenderer(Integer.class, new TextCell());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderCell());
        header.setPreferredSize(new Dimension(0, 36));
        header.setBackground(Theme.BG);

        MouseAdapter hover = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { setHoverRow(table, table.rowAtPoint(e.getPoint())); }
            @Override public void mouseExited(MouseEvent e) { setHoverRow(table, -1); }
        };
        table.addMouseMotionListener(hover);
        table.addMouseListener(hover);
    }

    private static void setHoverRow(JTable table, int row) {
        Object old = table.getClientProperty(HOVER_ROW);
        if (old == null || (int) old != row) {
            table.putClientProperty(HOVER_ROW, row);
            table.repaint();
        }
    }

    /** Background a cell should use: selection, then hover, then the table's own tint, then white. */
    public static Color rowBackground(JTable table, int row, boolean selected) {
        if (selected) return Theme.PRIMARY_SOFT;
        Object hover = table.getClientProperty(HOVER_ROW);
        if (hover != null && (int) hover == row) return HOVER;
        if (table instanceof RowTint tint) {
            Color c = tint.tintFor(row);
            if (c != null) return c;
        }
        return Theme.SURFACE;
    }

    /** Borderless scroll pane with slim scrollbars, for putting a table (or list) inside a Card. */
    public static JScrollPane scroll(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(Theme.SURFACE);
        scroll.setBackground(Theme.SURFACE);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ThinScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Plain text cell with padding and the shared row colours. */
    public static class TextCell extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setBackground(rowBackground(table, row, selected));
            setForeground(Theme.TEXT_SECONDARY);
            setFont(Theme.font(13));
            return this;
        }
    }

    /** Monospaced variant, for IDs and codes. */
    public static class MonoCell extends TextCell {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setFont(Theme.mono(12.5f));
            return this;
        }
    }

    /** Shows the value as a small monospaced chip, e.g. a course code. */
    public static class ChipCell extends JComponent implements TableCellRenderer {
        private JTable table;
        private Object value;
        private int row;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            this.table = table;
            this.value = value;
            this.row = row;
            this.selected = selected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Draw.smooth(g);
            g2.setColor(rowBackground(table, row, selected));
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (value != null) Draw.chip(g2, value.toString(), Theme.monoBold(11.5f), Chip.Style.NEUTRAL, 16, getHeight() / 2);
            g2.dispose();
        }
    }

    private static class HeaderCell extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            label.setText(value == null ? "" : value.toString().toUpperCase());
            label.setFont(Theme.label(10.5f));
            label.setForeground(Theme.MUTED);
            label.setBackground(Theme.BG);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 16, 0, 16)));
            return label;
        }
    }
}
