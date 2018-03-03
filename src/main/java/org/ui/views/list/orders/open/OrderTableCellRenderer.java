package org.ui.views.list.orders.open;

import org.logic.utils.TextUtils;
import org.ui.views.list.orders.TableElement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class OrderTableCellRenderer implements TableCellRenderer {

    private static final Color HIGHLIGHT = Color.lightGray;
    private static final AlphaComposite ac =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
    private JPanel jPanel;


    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null || column == 1) {
            return null;
        }
        TableElement tableElement = (TableElement) value;
        double last = tableElement.getLast();
        init(last <= 0.0d);

        jPanel.setBackground
                (isSelected ? HIGHLIGHT : Color.white);
        jPanel.setForeground
                (isSelected ? Color.white : HIGHLIGHT);

        // 1st row
        JPanel row1 = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(tableElement.getCoinName() + " (" + tableElement.getOrderType() + ")");
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        row1.add(jLabel, BorderLayout.CENTER);

        double min = tableElement.getMin();
        // 2nd row
        JPanel row2 = new JPanel(new BorderLayout());
        JLabel jLabel2 = new JLabel(min > 0.0d ? (tableElement.getMinLabel() + " " + tableElement.getMinText()) : "0%");
        jLabel2.setHorizontalAlignment(JLabel.LEFT);
        jLabel2.setVerticalAlignment(JLabel.CENTER);
        JLabel jLabel4 = new JLabel(tableElement.getMaxLabel() + " " + tableElement.getMaxText());
        jLabel4.setHorizontalAlignment(JLabel.RIGHT);
        jLabel4.setVerticalAlignment(JLabel.CENTER);
        row2.add(jLabel2, BorderLayout.WEST);
        row2.add(jLabel4, BorderLayout.EAST);

        // 3rd row
        double max = tableElement.getMax();
        double percentValue = ((last - min) * 100) / (max - min);

        JPanel row3 = new JPanel(new BorderLayout());
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setValue((int) percentValue);
        jProgressBar.setStringPainted(true);
        jProgressBar.setBorderPainted(true);
        setProgressBarColor(jProgressBar, percentValue);
        jProgressBar.setString("Last: " + tableElement.getLastAsString() + " - " + TextUtils.getDoubleAsText(percentValue, 2) + "%");
        row3.add(jProgressBar);

        jPanel.add(row1);
        jPanel.add(row2);
        jPanel.add(row3);
        jPanel.setBorder(new EmptyBorder(1, 1, 4, 1));

        return jPanel;
    }

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
