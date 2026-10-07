package com.crs.ui;

import com.crs.model.Course;
import com.crs.model.TimeSlot;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** Modal form for a new course. Builds the Course with Course.builder(...) (Builder pattern). */
public class AddCourseDialog extends JDialog {
    private static final String NO_DAY = "No fixed slot";

    private final JTextField codeField = new JTextField(10);
    private final JTextField titleField = new JTextField(20);
    private final JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1));
    private final JComboBox<Object> dayBox = new JComboBox<>();
    private final JTextField startField = new JTextField("09:00", 5);
    private final JTextField endField = new JTextField("10:00", 5);
    private final JTextField prerequisitesField = new JTextField(20);
    private Course result;

    public AddCourseDialog(Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Add course", ModalityType.APPLICATION_MODAL);
        dayBox.addItem(NO_DAY);
        for (DayOfWeek day : DayOfWeek.values()) dayBox.addItem(day);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Code (e.g. CS401):")); form.add(codeField);
        form.add(new JLabel("Title:"));             form.add(titleField);
        form.add(new JLabel("Credits:"));           form.add(creditsSpinner);
        form.add(new JLabel("Capacity:"));          form.add(capacitySpinner);
        form.add(new JLabel("Day:"));               form.add(dayBox);
        form.add(new JLabel("Start (HH:mm):"));     form.add(startField);
        form.add(new JLabel("End (HH:mm):"));       form.add(endField);
        form.add(new JLabel("Prerequisites (comma separated):")); form.add(prerequisitesField);

        JButton save = new JButton("Add");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(save);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(parent);
    }

    /** Shows the dialog and waits. Returns the new course, or empty if the admin cancelled. */
    public Optional<Course> showDialog() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

    private void onSave() {
        try {
            result = buildCourse();
            dispose();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            // Course and TimeSlot validate their own fields (encapsulation); just show what was wrong.
            JOptionPane.showMessageDialog(this, e.getMessage(), "Please fix the form", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Course buildCourse() {
        TimeSlot slot = null;
        if (dayBox.getSelectedItem() instanceof DayOfWeek day) {
            slot = new TimeSlot(day, LocalTime.parse(startField.getText().trim()), LocalTime.parse(endField.getText().trim()));
        }
        List<String> prerequisites = new ArrayList<>();
        for (String code : prerequisitesField.getText().split(",")) {
            if (!code.isBlank()) prerequisites.add(code.trim().toUpperCase());
        }
        return Course.builder(codeField.getText().trim().toUpperCase(), titleField.getText().trim())
                .credits((Integer) creditsSpinner.getValue())
                .capacity((Integer) capacitySpinner.getValue())
                .timeSlot(slot)
                .prerequisites(prerequisites)
                .build();
    }
}
