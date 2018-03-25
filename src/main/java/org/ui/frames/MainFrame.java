package org.ui.frames;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.logic.exceptions.ValueNotSetException;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.models.responses.MarketSummaryResponse;
import org.logic.models.responses.OrderResponse;
import org.logic.schedulers.monitors.model.MarketDetails;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.logic.transactions.model.stoploss.modes.StopLossCondition;
import org.logic.transactions.model.stoploss.modes.StopLossMode;
import org.logic.utils.ModelBuilder;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;
import org.ui.views.list.orders.ButtonColumn;
import org.ui.views.list.orders.TableElement;
import org.ui.views.list.orders.open.OrderTableCellRenderer;
import org.ui.views.list.orders.open.OrderTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.preferences.Constants.*;

/**
 * Created by niewinskip on 2016-12-28.
 */
public class MainFrame extends JFrame {

    private static Logger logger = Logger.getLogger(MainFrame.class);
    // List of market orders
    private static JTable ordersTable, slOrdersTable;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) screenSize.getWidth();
    int height = (int) screenSize.getHeight();
    private JLabel labelOpenOrdersStatus, labelOpenSLOrdersStatus, labelEmailAddress, labelApi, labelApiSecret;
    private ClassicTransactionFrame classicTransactionFrame;
    private StopLossFrame stopLossFrame;
    private EmailSetupFrame emailSetupFrame;
    private APISetupFrame apiSetupFrame;
    private PieChart pieChartPanel;
    private double LEFT_PANEL_WIDTH_RATIO = 0.4f;
    private double LIST_PANEL_WIDTH_RATIO = 0.3f;

    public MainFrame() {
        this.setTitle("AltBot " + Constants.VERSION);
        createMenuBar();
        Container cp = getContentPane();
        GridBagLayout bag = new GridBagLayout();
        cp.setLayout(bag);

        JPanel leftPanel = createLeftPanel();
        JPanel midPanel = createOpenOrdersTablePanel();
        JPanel rightPanel = createSLOrdersTablePanel();

        // Merge all main column panels. Add grid layout.
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.4;
        c.weighty = 1;
        c.gridx = 0;
        this.add(leftPanel, c);
        c.gridx = 1;
        c.weightx = 0.3;
        this.add(midPanel, c);
        c.gridx = 2;
        this.add(rightPanel, c);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // Initialize only after all components sizes were calculated.
        pieChartPanel.initChart();
    }

    private JPanel createLeftPanel() {
        // Left panel
        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(new TitledBorder(new EtchedBorder()));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));
        leftPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));
        leftPanel.setMinimumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), height));

        // All top panels in left panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new TitledBorder(new EtchedBorder()));
        statusPanel.setLayout(new GridLayout(2, 1));

        // Email address panel
        JPanel mailBar = new JPanel();
        labelEmailAddress = new JLabel();
        labelEmailAddress.setText(PreferenceManager.getEmailAddress());
        mailBar.add(labelEmailAddress);
        statusPanel.add(mailBar);

        // Api status bar
        JPanel apiStatusBar = new JPanel();
        apiStatusBar.setLayout(new GridLayout(1, 2));
        labelApi = new JLabel();
        labelApi.setText("API: " + PreferenceManager.getApiKeyObfucate());
        JPanel apiStatusBar1 = new JPanel();
        apiStatusBar1.setAlignmentX(Component.LEFT_ALIGNMENT);
        apiStatusBar1.add(labelApi);
        labelApiSecret = new JLabel();
        labelApiSecret.setText("Secret: " + PreferenceManager.getApiKeySecretObfucate());
        JPanel apiStatusBar2 = new JPanel();
        apiStatusBar2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        apiStatusBar2.add(labelApiSecret);
        apiStatusBar.add(apiStatusBar1);
        apiStatusBar.add(apiStatusBar2);
        statusPanel.add(apiStatusBar);
        leftPanel.add(statusPanel, BorderLayout.NORTH);

        // Mid view - pie chart
        pieChartPanel = new PieChart((int) (width * LEFT_PANEL_WIDTH_RATIO), height);
        leftPanel.add(pieChartPanel, BorderLayout.CENTER);

        // Bottom view - donations
        JPanel donationPanel = new JPanel();
        donationPanel.setLayout(new GridLayout(2, 2));
        donationPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 60));
        donationPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 60));
        Border border = LineBorder.createGrayLineBorder();
        JTextField btcLabel = new JTextField("Donate BTC: " + BTC_DONATION_ADDRESS, SwingConstants.CENTER);
        btcLabel.setEditable(false);
        btcLabel.setBorder(border);
        btcLabel.setHorizontalAlignment(JTextField.CENTER);
        donationPanel.add(btcLabel);

        JTextField ethLabel = new JTextField("Donate ETH: " + ETH_DONATION_ADDRESS);
        ethLabel.setEditable(false);
        ethLabel.setBorder(border);
        ethLabel.setHorizontalAlignment(JTextField.CENTER);
        donationPanel.add(ethLabel);

        JTextField ltcLabel = new JTextField("Donate LTC: " + LTC_DONATION_ADDRESS);
        ltcLabel.setEditable(false);
        ltcLabel.setBorder(border);
        ltcLabel.setHorizontalAlignment(JTextField.CENTER);
        donationPanel.add(ltcLabel);
        leftPanel.add(donationPanel, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createOpenOrdersTablePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder()));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMinimumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMaximumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));

        // Create status panel
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout(10, 5));
        statusBar.setBorder(new TitledBorder(new EtchedBorder()));
        labelOpenOrdersStatus = new JLabel("Open orders: ?", SwingConstants.CENTER);
        Font font = new Font("Courier", Font.BOLD, 16);
        labelOpenOrdersStatus.setFont(font);
        statusBar.add(labelOpenOrdersStatus);
        JButton newOrderButton = new JButton("Add new");
        newOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClassicTransactionFrame();
            }
        });
        statusBar.add(newOrderButton, BorderLayout.LINE_END);

        ordersTable = new JTable(new OrderTableModel());
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        ordersTable.setDefaultRenderer(TableElement.class, new OrderTableCellRenderer());
        ordersTable.setFillsViewportHeight(true);
        ordersTable.setRowHeight(75);
        ordersTable.setTableHeader(null);
        //Action when button clicked
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int id = Integer.parseInt(e.getActionCommand());
                logger.debug("Removing item with id: " + id);
                TableElement tableElement = (TableElement) ordersTable.getModel().getValueAt(id, 0);
                String orderId = tableElement.getUuid();
                String coinName = tableElement.getCoinName();
                boolean success = ((OrderTableModel) ordersTable.getModel()).removeRowById(id);
                if (success) {
                    OrderResponse orderResponse = ModelBuilder.buildCancelOrderById(orderId);
                    if (orderResponse.isSuccess()) {
                        logger.debug("Successfully cancelled order with id: " + orderId + " for coin " + coinName);
                    } else {
                        logger.error("Failed to cancel order with id: " + orderId + " for coin " + coinName);
                    }
                }
            }
        };
        ButtonColumn buttonColumn = new ButtonColumn(ordersTable, action, 1);
        buttonColumn.resize();
        JScrollPane listScroller = new JScrollPane(ordersTable);
        listScroller.setPreferredSize(new Dimension(panel.getMaximumSize()));

        panel.add(statusBar, BorderLayout.NORTH);
        panel.add(listScroller);
        return panel;
    }

    private JPanel createSLOrdersTablePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder()));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMinimumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));
        panel.setMaximumSize(new Dimension((int) (width * LIST_PANEL_WIDTH_RATIO), height));

        // Create status panel
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout(10, 5));
        statusBar.setBorder(new TitledBorder(new EtchedBorder()));
        labelOpenSLOrdersStatus = new JLabel("Open stop-loss orders: ?", SwingConstants.CENTER);
        Font font = new Font("Courier", Font.BOLD, 16);
        labelOpenSLOrdersStatus.setFont(font);
        statusBar.add(labelOpenSLOrdersStatus);
        JButton newOrderButton = new JButton("Add new");
        newOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openStopLossFrame();
            }
        });
        statusBar.add(newOrderButton, BorderLayout.LINE_END);

        slOrdersTable = new JTable(new OrderTableModel());
        slOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        slOrdersTable.setDefaultRenderer(TableElement.class, new OrderTableCellRenderer());
        slOrdersTable.setFillsViewportHeight(true);
        slOrdersTable.setRowHeight(75);
        slOrdersTable.setTableHeader(null);
        //Action when button clicked
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int id = Integer.parseInt(e.getActionCommand());
                logger.debug("Removing item with id: " + id);
                TableElement tableElement = (TableElement) slOrdersTable.getModel().getValueAt(id, 0);
                boolean success = ((OrderTableModel) slOrdersTable.getModel()).removeRowById(id);
                String mode = tableElement.getOrderType();
                mode = mode.substring(0, mode.indexOf(" "));
                logger.debug("MODE: " + mode);
                if (success) {
                    try {
                        StopLossOptionManager.getInstance().removeOptionByMarketNameAndMode(tableElement.getCoinName(), StopLossMode.valueOf(mode));
                        logger.debug("Successfully removed stop loss option for market " + tableElement.getCoinName() + " and mode " + mode);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        logger.error("Failed to remove stop-loss option with market name " + tableElement.getCoinName() + " and mode " + mode);
                    } catch (JAXBException e1) {
                        logger.error("Failed to remove stop-loss option with market name " + tableElement.getCoinName() + " and mode " + mode);
                    }
                }
            }
        };
        ButtonColumn buttonColumn = new ButtonColumn(slOrdersTable, action, 1);
        buttonColumn.resize();
        JScrollPane listScroller = new JScrollPane(slOrdersTable);
        listScroller.setPreferredSize(new Dimension(panel.getMaximumSize()));

        panel.add(statusBar, BorderLayout.NORTH);
        panel.add(listScroller);
        return panel;
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
        JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem("Email notifications");
        jCheckBoxMenuItem.setToolTipText("Enable email notifications.");
        jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
        jCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PreferenceManager.changeEmailNotificationEnabled();
                jCheckBoxMenuItem.setState(PreferenceManager.isEmailNotificationEnabled());
            }
        });
        JMenuItem emailSetupMenuItem = new JMenuItem("Email setup");
        emailSetupMenuItem.setToolTipText("Setup your email address to use notifications");
        emailSetupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailSetupFrame == null || emailSetupFrame.isClosed()) {
                    emailSetupFrame = new EmailSetupFrame();
                }
            }
        });
        JMenuItem apiSetupMenuItem = new JMenuItem("API setup");
        apiSetupMenuItem.setToolTipText("Setup your API keys");
        apiSetupMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (apiSetupFrame == null || apiSetupFrame.isClosed()) {
                    apiSetupFrame = new APISetupFrame();
                }
            }
        });
        JCheckBoxMenuItem jCheckBoxMenuItem2 = new JCheckBoxMenuItem("Hide if insignificant");
        jCheckBoxMenuItem2.setMnemonic(KeyEvent.VK_H);
        jCheckBoxMenuItem2.setToolTipText("Hide when value less than " + CHART_SIGNIFICANT_MINIMUM + " BTC");
        jCheckBoxMenuItem2.addActionListener((ActionEvent event) -> {
            PreferenceManager.changeHideInsignificantEnabled();
            jCheckBoxMenuItem2.setState(PreferenceManager.isHideInsignificantEnabled());
        });
        jCheckBoxMenuItem2.setState(PreferenceManager.isHideInsignificantEnabled());

        jMenuBar.add(file);
        jMenuBar.add(settings);
        settings.add(apiSetupMenuItem);
        settings.add(jCheckBoxMenuItem);
        settings.add(jCheckBoxMenuItem2);
        settings.add(emailSetupMenuItem);

        jMenuBar.add(file);
        jMenuBar.add(settings);
        setJMenuBar(jMenuBar);
    }

    /**
     * Update status bar above list of open orders.
     *
     * @param openOrders
     * @param buyOrdersCount
     */
    public void updateOpenOrdersStatusBar(int openOrders, int buyOrdersCount) {
        this.labelOpenOrdersStatus.setText("Open orders: " + openOrders + " / Buy: " + buyOrdersCount + " / Sell: " + (openOrders - buyOrdersCount));
        this.labelOpenOrdersStatus.validate();

        String email = PreferenceManager.getEmailAddress();
        if (email.isEmpty()) {
            this.labelEmailAddress.setText("Email address not found - go to Settings menu");
        } else {
            this.labelEmailAddress.setText("Welcome: " + email);
        }
        this.labelEmailAddress.validate();
        logger.info("Status bar value: " + labelOpenOrdersStatus.getText() + " || " + email);
    }

    /**
     * Update status bar above list of open orders.
     */
    public void updateOpenSLOrdersStatusBar() {
        List<StopLossOption> stopLossOptionList = new ArrayList<>(StopLossOptionManager.getInstance().getOptionList());
        this.labelOpenSLOrdersStatus.setText("Open stop-loss orders: " + stopLossOptionList.size());
        this.labelOpenSLOrdersStatus.validate();
    }

    public void updateAPIStatusBar() {
        labelApi.setText("API: " + PreferenceManager.getApiKeyObfucate());
        labelApiSecret.setText("Secret: " + PreferenceManager.getApiKeySecretObfucate());
    }

    private void openClassicTransactionFrame() {
        if (classicTransactionFrame == null || classicTransactionFrame.isClosed()) {
            classicTransactionFrame = new ClassicTransactionFrame();
        }
    }

    private void openStopLossFrame() {
        if (stopLossFrame == null || stopLossFrame.isClosed()) {
            stopLossFrame = new StopLossFrame();
        }
    }

    public void updatePieChartFrame(Map<String, BalancesSet> map) {
        if (pieChartPanel != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pieChartPanel.updateChart(map);
                }
            });
        }
    }

    public PieChart getPieChartFrame() {
        return pieChartPanel;
    }

    /**
     * Update orders list model - is equivalent to right outer join, where right side is model's collection.
     * Does not update fields that already exist in the model. See equals method of ListElementOrders class.
     *
     * @param openMarketOrders Collection of open market orders.
     */
    public synchronized void updateOrdersList(final MarketOrderResponse openMarketOrders, Map<String, MarketDetails> marketDetailsMap) {
        // Remove from model
        List<TableElement> toRemove = new ArrayList<>();
        OrderTableModel model = (OrderTableModel) ordersTable.getModel();
        for (int x = 0; x < model.getRowCount(); x++) {
            boolean contains = false;
            TableElement tableElement1 = model.getValueAt(x, 0);
            for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
                double last = getLast(marketDetailsMap, result.getExchange());
                TableElement tableElement2 = new TableElement(result.getExchange(), result.getOrderType(), last, result.getLimit());
                if (tableElement2.equals(tableElement1)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(model.getValueAt(x, 0));
            }
        }
        for (TableElement tableElement : toRemove) {
            model.removeRow(tableElement);
        }

        // Merge & update & add new
        int x = -1;
        for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            x++;
            double last = getLast(marketDetailsMap, result.getExchange());
            if (last <= 0.0d) {
                // get last price for limit buy
                if (result.getOrderType().equalsIgnoreCase("LIMIT_BUY")) {
                    MarketSummaryResponse marketSummaryResponse = ModelBuilder.buildMarketSummary(result.getExchange());
                    if (marketSummaryResponse.isSuccess()) {
                        last = marketSummaryResponse.getResult().get(0).getLast();
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            boolean sell = result.getOrderType().equalsIgnoreCase(ORDER_TYPE_SELL) ? true : false;
            double max = result.getLimit();
            double min = result.getLimit();
            if (!sell) {
                max *= 2;
                while (max < last) {
                    max *= 2;
                }
            } else {
                min /= 2;
                while (min > last) {
                    min /= 2;
                }
            }
            TableElement tableElement = new TableElement(result.getExchange(),
                    result.getOrderType(), last, max);
            tableElement.setUuid(result.getOrderUuid());
            if (!model.rowExists(tableElement)) {
                if (sell) {
                    tableElement.setMaxLabel("Sell at:");
                } else {
                    tableElement.setMinLabel("Buy at:");
                    tableElement.setMin(min);
                }
                model.insert(tableElement);
            } else {
                TableElement order = model.getValueAt(x, 0);
                order.setLast(last);
            }
        }
        // Sort
        model.getOrderList().sort(TableElement.getComparator());
        ordersTable.validate();
        ordersTable.repaint();
    }

    /**
     * Update orders list model - is equivalent to right outer join, where right side is model's collection.
     * Does not update fields that already exist in the model. See equals method of ListElementOrders class.
     *
     * @param marketDetailsMap
     */
    public synchronized void updateSLOrdersList(Map<String, MarketDetails> marketDetailsMap) {
        List<StopLossOption> stopLossOptionList = new ArrayList<>(StopLossOptionManager.getInstance().getOptionList());
        // Remove from model if not present in stop loss options anymore
        List<TableElement> toRemove = new ArrayList<>();
        OrderTableModel model = (OrderTableModel) slOrdersTable.getModel();
        for (int x = 0; x < model.getRowCount(); x++) {
            boolean contains = false;
            TableElement tableElement1 = model.getValueAt(x, 0);
            for (StopLossOption stopLossOption : stopLossOptionList) {
                String orderType = stopLossOption.getMode() + " / " + stopLossOption.getCondition();
                if (stopLossOption.getMarketName().equals(tableElement1.getCoinName()) &&
                        tableElement1.getOrderType().equals(orderType)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(model.getValueAt(x, 0));
            }
        }
        for (TableElement tableElement : toRemove) {
            model.removeRow(tableElement);
        }

        // Merge & update
        int x = -1;
        for (StopLossOption stopLossOption : stopLossOptionList) {
            x++;
            boolean below = stopLossOption.getCondition().equals(StopLossCondition.BELOW) ? true : false;
            double last = getLast(marketDetailsMap, stopLossOption.getMarketName());
            double max = stopLossOption.getCancelAt();
            double min = stopLossOption.getCancelAt();
            if (below) {
                max *= 2;
                while (max < last) {
                    max *= 2;
                }
            } else {
                min /= 2;
                while (min > last) {
                    min /= 2;
                }
            }
            TableElement tableElement = new TableElement(stopLossOption.getMarketName(),
                    stopLossOption.getMode() + " / " + stopLossOption.getCondition(), last, max);
            if (!model.rowExists(tableElement)) {
                tableElement.setMin(min);
                if (below) {
                    tableElement.setMaxLabel("High:");
                    tableElement.setMinLabel("Stop-loss:");
                } else {
                    tableElement.setMaxLabel("Stop-loss:");
                    tableElement.setMinLabel("Low:");
                }
                model.insert(tableElement);
            } else {
                model.getValueAt(x, 0).setLast(last);
            }
        }
        model.getOrderList().sort(TableElement.getComparator());
        slOrdersTable.validate();
        slOrdersTable.repaint();
    }


    private double getLast(Map<String, MarketDetails> marketDetailsMap, String exchange) {
        double last;
        try {
            if (exchange.equalsIgnoreCase("ALL")) {
                return pieChartPanel.getBtcSum();
            }
            last = marketDetailsMap.get(exchange).getLast();
        } catch (ValueNotSetException e) {
            return 0.0d;
        } catch (NullPointerException e) {
            return 0.0d;
        }
        return last;
    }
}

