package org.ui.views.list.orders;

import org.logic.utils.TextUtils;

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
        String minLimit = TextUtils.getDoubleAsText(listElementOrder.getLimit() * 0.9d);
        JPanel row2 = new JPanel(new BorderLayout());
        JLabel jLabel2 = new JLabel(minLimit + " - 90% of Limit");
        jLabel2.setHorizontalAlignment(JLabel.LEFT);
        jLabel2.setVerticalAlignment(JLabel.CENTER);
        JLabel jLabel4 = new JLabel("Limit: " + listElementOrder.getLimitAsText());
        jLabel4.setHorizontalAlignment(JLabel.RIGHT);
        jLabel4.setVerticalAlignment(JLabel.CENTER);

        row2.add(jLabel2, BorderLayout.WEST);
        row2.add(jLabel4, BorderLayout.EAST);

        double max = listElementOrder.getLimit();
        double min  = 0.9d * max;
        double percentValue = ((listElementOrder.getLast() - min) * 100) / (max - min);
        JPanel row3 = new JPanel(new BorderLayout());
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setOpaque(false);
        jProgressBar.setValue((int) percentValue);
        jProgressBar.setStringPainted(true);
        jProgressBar.setString("Last: " + listElementOrder.getLastAsString() + " - " + TextUtils.getDoubleAsText(percentValue,2) + "%");
        row3.add(jProgressBar);

        jPanel.add(row1);
        jPanel.add(row2);
        jPanel.add(row3);

        return jPanel;
    }
}
