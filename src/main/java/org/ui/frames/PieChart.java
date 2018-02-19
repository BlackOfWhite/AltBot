package org.ui.frames;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import org.logic.models.misc.BalancesSet;
import org.preferences.managers.PreferenceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Map;

import static org.preferences.Constants.BALANCE_MINIMUM;
import static org.preferences.Constants.CHART_SIGNIFICANT_MINIMUM;

public class PieChart extends JPanel {

    private static final String LOADING_MESSAGE = "Loading..";
    private static final String WALLET_EMPTY = "Wallet is empty";
    private static final String TITLE = "Wallet Pie Chart";
    private static javafx.scene.chart.PieChart pieChart = null;
    private static boolean isConnected = true;
    private static double btcSum = 0.0d;
    private ObservableList<javafx.scene.chart.PieChart.Data> pieChartData;
    private JFXPanel jfxPanel = new JFXPanel();

    public PieChart(int width, int height) {
        pieChartData = FXCollections.observableArrayList();
        pieChartData.add(new javafx.scene.chart.PieChart.Data(LOADING_MESSAGE, 100));
        createMenuBar();
        this.setMinimumSize(new Dimension(width, height));
        this.add(jfxPanel, CENTER_ALIGNMENT);
    }

    public void initChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                jfxPanel.setScene(createScene());
            }
        });
    }

    private Scene createScene() {
        Group root = new Group();
//        ObservableList<PieChart.Data> pieChartData =
//                FXCollections.observableArrayList(
//                        new PieChart.Data("January", 100),
//                        new PieChart.Data("February", 200),
//                        new PieChart.Data("March", 50),
//                        new PieChart.Data("April", 75),
//                        new PieChart.Data("May", 110),
//                        new PieChart.Data("June", 300),
//                        new PieChart.Data("July", 111),
//                        new PieChart.Data("August", 30),
//                        new PieChart.Data("September", 75),
//                        new PieChart.Data("October", 55),
//                        new PieChart.Data("November", 225),
//                        new PieChart.Data("December", 99));

        pieChart = new javafx.scene.chart.PieChart(pieChartData);
        pieChart.setTitle(TITLE);
        pieChart.setPrefSize(getWidth() * 0.985, getHeight() * 0.985);
        pieChart.setMinSize(getWidth() * 0.985, getHeight() * 0.985);
        root.getChildren().add(pieChart);
        return new Scene(root, getWidth(), getHeight());
    }

    public void updateChart(Map<String, BalancesSet> map) {
        if (map.isEmpty()) {
            pieChartData.clear();
            pieChartData.add(new javafx.scene.chart.PieChart.Data(WALLET_EMPTY, 0));
            return;
        }
        if (pieChartData.size() == 1 && (pieChartData.get(0).getName().equalsIgnoreCase(WALLET_EMPTY)
                || pieChartData.get(0).getName().equalsIgnoreCase(LOADING_MESSAGE))) {
            pieChartData.clear();
        }
        btcSum = 0.0d;
        for (Map.Entry<String, BalancesSet> entry : map.entrySet()) {
            if (entry.getValue().getBtc() >= BALANCE_MINIMUM) {
                btcSum += entry.getValue().getBtc();
            }
        }
        pieChart.setTitle("Estimated Value: " + String.format("%.5f", btcSum) + " BTC");

        // Remove unused
        Iterator<javafx.scene.chart.PieChart.Data> i = pieChartData.iterator();
        while (i.hasNext()) {
            javafx.scene.chart.PieChart.Data data = i.next();
            boolean exists = false;
            for (Map.Entry<String, BalancesSet> entry : map.entrySet()) {
                if (data.getName().startsWith(entry.getKey()) && data.getPieValue() >= BALANCE_MINIMUM) {
                    exists = true;
                }
            }
            if (!exists) {
                i.remove();
            }
        }

        // Display
        boolean hideInsignificant = PreferenceManager.isHideInsignificantEnabled();
        for (Map.Entry<String, BalancesSet> entry : map.entrySet()) {
            boolean put = false;
            double btc = entry.getValue().getBtc();
            int index = 0;
            boolean removeInsignificant = false;
            for (javafx.scene.chart.PieChart.Data data : pieChartData) {
                if (data.getName().startsWith(entry.getKey())) {
                    if (hideInsignificant && btc < CHART_SIGNIFICANT_MINIMUM) {
                        removeInsignificant = true;
                        break;
                    }
                    String key = makeKey(entry, btcSum);
                    data.setName(key);
                    data.setPieValue(btc);
                    put = true;
                    break;
                }
                index++;
            }
            if (removeInsignificant) {
                pieChartData.remove(index);
                continue;
            }
            if (hideInsignificant && btc < CHART_SIGNIFICANT_MINIMUM) {
                continue;
            }
            if (!put) {
                String key = makeKey(entry, btcSum);
                pieChartData.add(new javafx.scene.chart.PieChart.Data(key, btc));
            }
        }
        pieChartData.sorted();
    }

    private void createMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        ImageIcon icon = new ImageIcon("exit.png");

        // File
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem eMenuItem = new JMenuItem("Exit", icon);
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        file.add(eMenuItem);

        // Settings
        JMenu settings = new JMenu("Settings");
        file.setMnemonic(KeyEvent.VK_S);
        JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem("Hide if insignificant");
        jCheckBoxMenuItem.setMnemonic(KeyEvent.VK_H);
        jCheckBoxMenuItem.setToolTipText("Hide when value less than " + CHART_SIGNIFICANT_MINIMUM + " BTC");
        jCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            PreferenceManager.changeHideInsignificantEnabled();
            jCheckBoxMenuItem.setState(PreferenceManager.isHideInsignificantEnabled());
        });
        jCheckBoxMenuItem.setState(PreferenceManager.isHideInsignificantEnabled());
        settings.add(jCheckBoxMenuItem);

        jMenuBar.add(file);
        jMenuBar.add(settings);
    }

    private String makeKey(Map.Entry<String, BalancesSet> entry, double btcSum) {
        if (entry.getKey().equalsIgnoreCase("BTC")) {
            return entry.getKey() + " " + String.format("%.4f", entry.getValue().getAmount()) + "\n" +
                    "(" + String.format("%.2f", entry.getValue().getBtc() / btcSum * 100)
                    + "%)";
        }
        return entry.getKey() + " " + String.format("%.4f", entry.getValue().getAmount()) + "\n" +
                String.format("%.5f", entry.getValue().getBtc()) + " BTC\n" +
                "(" + String.format("%.2f", entry.getValue().getBtc() / btcSum * 100)
                + "%)";
    }

    public void setIsConnected(boolean set) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (isConnected && !set) {
                    pieChart.setTitle(pieChart.getTitle() + " (Reconnecting..)");
                    pieChartData.sorted();
                }
                isConnected = set;
            }
        });
    }

    public double getBtcSum() {
        return btcSum;
    }
}
