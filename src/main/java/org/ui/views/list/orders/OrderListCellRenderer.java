package org.ui.views.list.orders;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;

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
//        row.add(jLabel, BorderLayout.LINE_START);
//        row.add(jLabel2, BorderLayout.LINE_END);
        // Progress bar
        final JFXPanel jfxPanel = new JFXPanel();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(jfxPanel);
            }
        });
        jfxPanel.setBackground(Color.lightGray);
        jfxPanel.setSize(100,100);
        row.add(jfxPanel, CENTER_ALIGNMENT);
        return row;
    }

    private void initFX(JFXPanel jfxPanel) {
        Scene scene = createScene();
        jfxPanel.setScene(scene);
        jfxPanel.setSize(100,100);
    }

    private Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(69);
        progressBar.setMinSize(100,100);
        root.getChildren().add(progressBar);
        return (scene);
    }
}
