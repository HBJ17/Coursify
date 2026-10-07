package com.crs.ui;

import com.crs.app.AppContext;
import com.crs.controller.RegisterController;
import com.crs.model.Course;
import com.crs.service.CourseService;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** All courses in a table, with a search box that filters by code or title as you type, and a Register button. */
public class BrowseCoursesPanel extends RefreshablePanel<List<Course>> {
    private final CourseService courseService;
    private final JTextField searchField = new JTextField(25);
    private final ReadOnlyTableModel model =
            new ReadOnlyTableModel("Code", "Title", "Credits", "Seats left", "Time", "Prerequisites");
    protected final JTable table = new JTable(model);
    private volatile String query = ""; // written on the Swing thread, read by the background loader

    public BrowseCoursesPanel(AppContext ctx, Session session) {
        super(ctx.getSubject());
        this.courseService = ctx.getCourseService();
        RegisterController controller = new RegisterController(this, ctx.getRegistrationService(),
                ctx.getWaitlistService(), session);
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> controller.register(selectedCourseCode()));

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(new JLabel("Search (code or title):"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(registerButton, BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.addMouseListener(new MouseAdapter() {      // double-click a row = Register
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) controller.register(selectedCourseCode());
            }
        });

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Live search: every key press reloads the table with the new query.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        reload();
    }

    private void onSearchChanged() {
        query = searchField.getText();
        reload();
    }

    @Override
    protected List<Course> loadData() {
        return courseService.search(query);
    }

    @Override
    protected void render(List<Course> courses) {
        List<Object[]> rows = new ArrayList<>();
        for (Course c : courses) {
            rows.add(new Object[]{c.getCode(), c.getTitle(), c.getCredits(), UiFormat.seats(c),
                    UiFormat.timeSlot(c), UiFormat.prerequisites(c)});
        }
        model.setRows(rows);
    }

    /** Course code of the selected row, or null if nothing is selected. */
    protected String selectedCourseCode() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        return (String) model.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    }
}
