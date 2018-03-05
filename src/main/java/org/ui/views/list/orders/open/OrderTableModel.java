package org.ui.views.list.orders.open;

import org.ui.views.list.orders.TableElement;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class OrderTableModel extends DefaultTableModel {

    List<TableElement> orderList;

    public OrderTableModel() {
        this.orderList = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        if (orderList == null) {
            return 0;
        }
        return orderList.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public TableElement getValueAt(int rowIndex, int columnIndex) {
        return (orderList == null) ? null : orderList.get(rowIndex);
    }

    public Class getColumnClass(int columnIndex) {
        return TableElement.class;
    }

    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return false;
        }
        return true;
    }

    public boolean removeRow(TableElement row) {
        if (orderList.contains(row)) {
            orderList.remove(row);
            return true;
        }
        return false;
    }

    public boolean removeRowById(int id) {
        try {
            orderList.remove(id);
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean rowExists(TableElement row) {
        return orderList.contains(row);
    }

    public void insert(TableElement row) {
        orderList.add(row);
    }

    public List<TableElement> getOrderList() {
        return orderList;
    }
}
