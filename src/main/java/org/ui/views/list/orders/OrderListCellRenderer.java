package org.ui.views.list.orders;

import org.ui.views.list.ListElementOrder;

import javax.swing.*;
import java.awt.*;

public class OrderListCellRenderer extends JPanel implements javax.swing.ListCellRenderer {

    private static final Color HIGHLIGHT = Color.lightGray;

    public OrderListCellRenderer() {
        this.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return null;
        }
        JPanel row = new JPanel();
        ListElementOrder listElementOrder = (ListElementOrder) value;
        row.setBackground
                (isSelected ? HIGHLIGHT : Color.white);
        row.setForeground
                (isSelected ? Color.white : HIGHLIGHT);
        JLabel jLabel = new JLabel(listElementOrder.getCoinName());
        JLabel jLabel2 = new JLabel(listElementOrder.getOrderType());
        row.add(jLabel);
        row.add(jLabel2);
        return row;
    }
}
