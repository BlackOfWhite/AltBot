package org.ui.views.list.orders;

import javax.swing.*;
import java.awt.*;

public class OrderListCellRenderer implements javax.swing.ListCellRenderer {

    private static final Color HIGHLIGHT = Color.lightGray;
    private JPanel jPanel;

    public OrderListCellRenderer() {
        init();
    }

    private void init() {
        jPanel = new JPanel();
        jPanel.setOpaque(true);
        jPanel.setLayout(new GridLayout(3, 1));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return null;
        }
        init();
        ListElementOrder listElementOrder = (ListElementOrder) value;
        jPanel.setBackground
                (isSelected ? HIGHLIGHT : Color.white);
        jPanel.setForeground
                (isSelected ? Color.white : HIGHLIGHT);

        // 1st row
        JPanel row1 = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(listElementOrder.getCoinName() + " - " + listElementOrder.getOrderType());
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        row1.add(jLabel, BorderLayout.CENTER);

        // 2nd row
        JPanel row2 = new JPanel(new BorderLayout());
        JLabel jLabel2 = new JLabel("-5%");
        jLabel2.setHorizontalAlignment(JLabel.LEFT);
        jLabel2.setVerticalAlignment(JLabel.CENTER);
        JLabel jLabel3 = new JLabel("Last: " + listElementOrder.getLastAsString());
        jLabel3.setHorizontalAlignment(JLabel.CENTER);
        jLabel3.setVerticalAlignment(JLabel.CENTER);
        JLabel jLabel4 = new JLabel("0.00000234234");
        jLabel4.setHorizontalAlignment(JLabel.RIGHT);
        jLabel4.setVerticalAlignment(JLabel.CENTER);

        row2.add(jLabel2, BorderLayout.WEST);
        row2.add(jLabel3, BorderLayout.NORTH);
        row2.add(jLabel4, BorderLayout.EAST);

        JPanel row3 = new JPanel(new BorderLayout());
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setOpaque(false);
        jProgressBar.setValue(69);
        row3.add(jProgressBar);

        jPanel.add(row1);
        jPanel.add(row2);
        jPanel.add(row3);

        return jPanel;
    }
}
