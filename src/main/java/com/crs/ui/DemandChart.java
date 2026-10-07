package com.crs.ui;

import com.crs.model.CourseDemand;
import com.crs.ui.theme.Chip;
import com.crs.ui.theme.Draw;
import com.crs.ui.theme.Theme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.List;
import javax.swing.JComponent;

/** Ranked horizontal bars: one row per course, bar length = demand (registered + waiting) / capacity. */
public class DemandChart extends JComponent {
    private static final int ROW = 38;
    private List<CourseDemand> rows = List.of();

    public DemandChart() {
        setPreferredSize(new Dimension(600, ROW * 5));
    }

    public void setRows(List<CourseDemand> rows) {
        this.rows = List.copyOf(rows);
        setPreferredSize(new Dimension(600, ROW * Math.max(5, rows.size())));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Draw.smooth(g);
        int w = getWidth();
        if (rows.isEmpty()) {
            Draw.text(g2, "No courses yet.", Theme.font(12.5f), Theme.FAINT, 0, ROW / 2);
            g2.dispose();
            return;
        }
        double maxScore = 1.0; // bars are scaled so 100% fills the track, unless a course is over-subscribed
        for (CourseDemand d : rows) maxScore = Math.max(maxScore, d.getDemandScore());

        int barStart = 300, barEnd = w - 200;
        for (int i = 0; i < rows.size(); i++) {
            CourseDemand d = rows.get(i);
            int cy = i * ROW + ROW / 2;

            // rank bubble: #1 is filled indigo
            Color bubble = i == 0 ? Theme.PRIMARY : Theme.PRIMARY_SOFT;
            g2.setColor(bubble);
            g2.fill(new Ellipse2D.Double(0, cy - 11, 22, 22));
            String rank = String.valueOf(i + 1);
            int rw = Draw.width(g2, rank, Theme.bold(11));
            Draw.text(g2, rank, Theme.bold(11), i == 0 ? Color.WHITE : Theme.PRIMARY, 11 - rw / 2, cy);

            int chipW = Draw.chip(g2, d.getCourseCode(), Theme.monoBold(11.5f), Chip.Style.NEUTRAL, 34, cy);
            int titleX = 34 + chipW + 10;
            Draw.text(g2, Draw.fit(g2, d.getTitle(), Theme.bold(13), barStart - titleX - 12), Theme.bold(13), Theme.TEXT, titleX, cy);

            double score = d.getDemandScore();
            Color color = score > 1 ? Theme.DANGER : score >= 0.75 ? Theme.WARNING : Theme.PRIMARY;
            int track = Math.max(40, barEnd - barStart);
            Draw.roundRect(g2, barStart, cy - 4, track, 8, 4, Theme.SURFACE_MUTED, null);
            if (score > 0) Draw.roundRect(g2, barStart, cy - 4, Math.max(8, track * score / maxScore), 8, 4, color, null);

            String pct = Math.round(score * 100) + "%";
            Draw.text(g2, pct, Theme.monoBold(12), score > 1 ? Theme.DANGER_TEXT : Theme.TEXT, barEnd + 12, cy);
            String meta = d.getRegistered() + " reg · " + d.getWaitlisted() + " waiting · cap " + d.getCapacity();
            Draw.text(g2, Draw.fit(g2, meta, Theme.font(11.5f), 140), Theme.font(11.5f), Theme.FAINT, barEnd + 58, cy);

            if (i < rows.size() - 1) {
                g2.setColor(Theme.DIVIDER);
                g2.drawLine(34, (i + 1) * ROW, w, (i + 1) * ROW);
            }
        }
        g2.dispose();
    }
}
