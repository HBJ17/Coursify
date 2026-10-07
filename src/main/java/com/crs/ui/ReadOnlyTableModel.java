package com.crs.ui;

import java.util.List;
import javax.swing.table.DefaultTableModel;

/** A JTable model the user can't type into. setRows(...) replaces all rows at once. */
public class ReadOnlyTableModel extends DefaultTableModel {

    public ReadOnlyTableModel(String... columns) {
        super(columns, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void setRows(List<Object[]> rows) {
        setRowCount(0);
        for (Object[] row : rows) addRow(row);
    }
}
