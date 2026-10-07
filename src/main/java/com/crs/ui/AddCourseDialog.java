package com.crs.ui;

import com.crs.model.Course;
import com.crs.model.TimeSlot;
import com.crs.ui.theme.FieldBorder;
import com.crs.ui.theme.Icons;
import com.crs.ui.theme.InputField;
import com.crs.ui.theme.Theme;
import com.crs.ui.theme.UiButton;
import com.crs.ui.theme.UiComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** Modal form for a new course. Builds the Course with Course.builder(...) (Builder pattern). */
public class AddCourseDialog extends JDialog {
    private static final String NO_DAY = "No fixed slot";

    private final InputField codeField = new InputField("e.g. CS401");
    private final InputField titleField = new InputField("e.g. Computer Networks");
    private final JSpinner creditsSpinner = spinner(new SpinnerNumberModel(3, 0, 10, 1));
    private final JSpinner capacitySpinner = spinner(new SpinnerNumberModel(30, 1, 500, 1));
    private final UiComboBox<String> dayBox = new UiComboBox<>();
    private final InputField startField = new InputField("09:00", Icons.Name.CLOCK);
    private final InputField endField = new InputField("10:00", Icons.Name.CLOCK);
    private final InputField prerequisitesField = new InputField("e.g. CS101, CS102");
    private final JLabel errorLabel = new JLabel(" ");
    private Course result;

    public AddCourseDialog(Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Add course", ModalityType.APPLICATION_MODAL);
        dayBox.addItem(NO_DAY);
        for (DayOfWeek day : DayOfWeek.values()) dayBox.addItem(dayName(day));
        dayBox.setSelectedItem(dayName(DayOfWeek.MONDAY));
        startField.setText("09:00");
        endField.setText("10:00");

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(Theme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));

        JLabel title = new JLabel("Add course");
        title.setFont(Theme.bold(18));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("New courses appear in the catalogue straight away.");
        subtitle.setFont(Theme.font(12.5f));
        subtitle.setForeground(Theme.MUTED);
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(subtitle, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 12, 0);
        form.add(field("Code", codeField), c);
        form.add(field("Title", titleField), c);
        form.add(pair(field("Credits", creditsSpinner), field("Capacity", capacitySpinner)), c);
        form.add(field("Day", dayBox), c);
        form.add(pair(field("Start (HH:mm)", startField), field("End (HH:mm)", endField)), c);
        c.insets = new Insets(0, 0, 4, 0);
        form.add(field("Prerequisites (comma separated)", prerequisitesField), c);
        errorLabel.setFont(Theme.font(12));
        errorLabel.setForeground(Theme.DANGER_TEXT);
        errorLabel.setIconTextGap(6);
        form.add(errorLabel, c);

        UiButton cancel = new UiButton("Cancel", UiButton.Variant.SECONDARY);
        UiButton save = new UiButton("Add course", Icons.Name.PLUS, UiButton.Variant.PRIMARY);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> onSave());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);

        root.add(head, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dayBox.addActionListener(e -> {
            boolean hasSlot = !NO_DAY.equals(dayBox.getSelectedItem());
            startField.setEnabled(hasSlot);
            endField.setEnabled(hasSlot);
        });

        fitHeight();
        setResizable(false);
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
            // Course and TimeSlot validate their own fields (encapsulation); show what was wrong under the form.
            String message = e instanceof DateTimeParseException ? "Times must look like 09:00" : e.getMessage();
            errorLabel.setText(message);
            errorLabel.setIcon(Icons.of(Icons.Name.ERROR, 14, Theme.DANGER));
            fitHeight();
        }
    }

    private Course buildCourse() {
        TimeSlot slot = null;
        if (!NO_DAY.equals(dayBox.getSelectedItem())) {
            DayOfWeek day = DayOfWeek.valueOf(((String) dayBox.getSelectedItem()).toUpperCase());
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

    /** Keeps the width fixed and makes the height fit everything (including an error message). */
    private void fitHeight() {
        setSize(440, 100);
        pack();
        setSize(440, getHeight());
    }

    /** "Monday" instead of "MONDAY". */
    private static String dayName(DayOfWeek day) {
        String name = day.toString();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private static JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.bold(12));
        l.setForeground(Theme.TEXT_SECONDARY);
        panel.add(l, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel pair(JComponent left, JComponent right) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(left);
        panel.add(right);
        return panel;
    }

    /** Number spinner with the rounded input look. */
    private static JSpinner spinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(Theme.font(13));
        spinner.setBorder(new FieldBorder(4));
        spinner.setPreferredSize(new Dimension(0, 36));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor def) {
            def.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
            def.getTextField().setBackground(Theme.SURFACE);
            def.getTextField().setFont(Theme.font(13));
            def.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        spinner.setBackground(Theme.SURFACE);
        for (Component child : spinner.getComponents()) {
            if (child instanceof JComponent jc) jc.setBackground(new Color(0xF8FAFC));
        }
        return spinner;
    }
}
