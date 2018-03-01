package org.ui.views.list.orders.open;

import org.logic.utils.TextUtils;
import org.ui.views.list.orders.ListElementOrder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrderListCellRenderer implements javax.swing.ListCellRenderer {

    private static final Color HIGHLIGHT = Color.lightGray;
    private static final AlphaComposite ac =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
    private JPanel jPanel;

    /**
     * Add transparent overlay if order record is disabled - when last price if negative.
     *
     * @param applyOverlay
     */
    private void init(boolean applyOverlay) {
        if (applyOverlay) {
            jPanel = new JPanel() {
                @Override
                public void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(Color.black);
                    g2d.fillRect(1, 1, getWidth() - 1, this.getHeight() - 4);
                    g2d.setComposite(ac);
                }
            };
        } else {
            jPanel = new JPanel();
        }
        jPanel.setLayout(new GridLayout(3, 1));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return null;
        }
        ListElementOrder listElementOrder = (ListElementOrder) value;
        double last = listElementOrder.getLast();
        init(last <= 0.0d);

        jPanel.setBackground
                (isSelected ? HIGHLIGHT : Color.white);
        jPanel.setForeground
                (isSelected ? Color.white : HIGHLIGHT);

        // 1st row
        JPanel row1 = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(listElementOrder.getCoinName() + " (" + listElementOrder.getOrderType() + ")");
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        row1.add(jLabel, BorderLayout.CENTER);
        JButton cancelOrderButton = new JButton("Cancel");
        cancelOrderButton.setEnabled(true);
        cancelOrderButton.setFocusable(true);
        row1.add(cancelOrderButton, BorderLayout.LINE_END);

        double min = listElementOrder.getMin();
        // 2nd row
        JPanel row2 = new JPanel(new BorderLayout());
        JLabel jLabel2 = new JLabel(min > 0.0d ? (listElementOrder.getMinLabel() + " " + listElementOrder.getMinText()) : "0%");
        jLabel2.setHorizontalAlignment(JLabel.LEFT);
        jLabel2.setVerticalAlignment(JLabel.CENTER);
        JLabel jLabel4 = new JLabel(listElementOrder.getMaxLabel() + " " + listElementOrder.getMaxText());
        jLabel4.setHorizontalAlignment(JLabel.RIGHT);
        jLabel4.setVerticalAlignment(JLabel.CENTER);
        row2.add(jLabel2, BorderLayout.WEST);
        row2.add(jLabel4, BorderLayout.EAST);

        // 3rd row
        double max = listElementOrder.getMax();
        double percentValue = ((last - min) * 100) / (max - min);

        JPanel row3 = new JPanel(new BorderLayout());
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setValue((int) percentValue);
        jProgressBar.setStringPainted(true);
        jProgressBar.setBorderPainted(true);
        setProgressBarColor(jProgressBar, percentValue);
        jProgressBar.setString("Last: " + listElementOrder.getLastAsString() + " - " + TextUtils.getDoubleAsText(percentValue, 2) + "%");
        row3.add(jProgressBar);

        jPanel.add(row1);
        jPanel.add(row2);
        jPanel.add(row3);
        jPanel.setBorder(new EmptyBorder(1, 1, 4, 1));
        return jPanel;
    }

    private void setProgressBarColor(JProgressBar jProgressBar, double percent) {
        if (percent >= 90d) {
            jProgressBar.setBackground(Color.GREEN);
        } else if (percent < 35d) {
            jProgressBar.setBackground(Color.RED);
//            defaults.put(defaultColor, defaults.get("nimbusRed"));
        } else {
            jProgressBar.setBackground(Color.ORANGE);
        }
    }
}
