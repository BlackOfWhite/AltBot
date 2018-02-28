package org.ui.frames;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.logic.exceptions.ValueNotSetException;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.MarketOrderResponse;
import org.logic.schedulers.monitors.model.MarketDetails;
import org.logic.transactions.model.stoploss.StopLossOption;
import org.logic.transactions.model.stoploss.StopLossOptionManager;
import org.logic.transactions.model.stoploss.modes.StopLossCondition;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;
import org.ui.views.list.orders.ListElementOrder;
import org.ui.views.list.orders.open.OrderListCellRenderer;
import org.ui.views.list.orders.stoploss.SLOrderListCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import static org.preferences.Constants.*;

/**
 * Created by niewinskip on 2016-12-28.
 */
public class MainFrame extends JFrame {

    private static Logger logger = Logger.getLogger(MainFrame.class);
    // List of market orders
    private static JList ordersList, slOrdersList;
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
    // Model to update orders list content
    private DefaultListModel<ListElementOrder> model = new DefaultListModel<>();
    private DefaultListModel<ListElementOrder> slModel = new DefaultListModel<>();

    public MainFrame() {
        this.setTitle("AltBot " + Constants.VERSION);
        createMenuBar();
        Container cp = getContentPane();
        GridBagLayout bag = new GridBagLayout();
        cp.setLayout(bag);

        JPanel leftPanel = createLeftPanel();
        JPanel midPanel = createOpenOrdersListPanel(model);
        JPanel rightPanel = createSLOrdersListPanel(slModel);

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
//        statusPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 120));
//        statusPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANEL_WIDTH_RATIO), 120));

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

    private JPanel createOpenOrdersListPanel(DefaultListModel<ListElementOrder> model) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

        ordersList = new JList();
        ordersList.setModel(model);
        ordersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        ordersList.setLayoutOrientation(JList.VERTICAL);
        ordersList.setVisibleRowCount(-1);
        ordersList.setCellRenderer(new OrderListCellRenderer());

        JScrollPane listScroller = new JScrollPane(ordersList);
        listScroller.setPreferredSize(new Dimension(panel.getMaximumSize()));

        panel.add(statusBar, BorderLayout.NORTH);
        panel.add(listScroller);
        return panel;
    }

    private JPanel createSLOrdersListPanel(DefaultListModel<ListElementOrder> model) {
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

        slOrdersList = new JList();
        slOrdersList.setModel(model);
        slOrdersList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        slOrdersList.setLayoutOrientation(JList.VERTICAL);
        slOrdersList.setVisibleRowCount(-1);
        slOrdersList.setCellRenderer(new SLOrderListCellRenderer());

        JScrollPane listScroller = new JScrollPane(slOrdersList);
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
        List<ListElementOrder> toRemove = new ArrayList<>();
        for (int x = 0; x < model.size(); x++) {
            boolean contains = false;
            ListElementOrder listElementOrder1 = model.getElementAt(x);
            for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
                double last = getLast(marketDetailsMap, result.getExchange());
                ListElementOrder listElementOrder2 = new ListElementOrder(result.getExchange(), result.getOrderType(), last, result.getLimit());
                if (listElementOrder2.equals(listElementOrder1)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(model.getElementAt(x));
            }
        }
        for (ListElementOrder listElementOrder : toRemove) {
            model.removeElement(listElementOrder);
        }

        // Merge & update
        int x = -1;
        for (MarketOrderResponse.Result result : openMarketOrders.getResult()) {
            x++;
            double last = getLast(marketDetailsMap, result.getExchange());
            if (last <= 0.0d) {
                continue;
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
            ListElementOrder listElementOrder = new ListElementOrder(result.getExchange(),
                    result.getOrderType(), last, max);
            if (!model.contains(listElementOrder)) {
                if (sell) {
                    listElementOrder.setMaxLabel("Sell at:");
                } else {
                    listElementOrder.setMinLabel("Buy at:");
                    listElementOrder.setMin(min);
                }
                model.addElement(listElementOrder);
            } else {
                model.getElementAt(x).setLast(last);
            }
        }
        // Sort
        Arrays.sort(new Enumeration[]{model.elements()}, ListElementOrder.getComparator());
        ordersList.validate();
        ordersList.repaint();
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
        List<ListElementOrder> toRemove = new ArrayList<>();
        for (int x = 0; x < slModel.size(); x++) {
            boolean contains = false;
            ListElementOrder listElementOrder1 = slModel.getElementAt(x);
            for (StopLossOption stopLossOption : stopLossOptionList) {
                String orderType = stopLossOption.getMode() + " / " + stopLossOption.getCondition();
                if (stopLossOption.getMarketName().equals(listElementOrder1.getCoinName()) &&
                        listElementOrder1.getOrderType().equals(orderType)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                toRemove.add(slModel.getElementAt(x));
            }
        }
        for (ListElementOrder listElementOrder : toRemove) {
            slModel.removeElement(listElementOrder);
        }

        // Merge & update
        int x = -1;
        for (StopLossOption stopLossOption : stopLossOptionList) {
            logger.error(stopLossOption);
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
            ListElementOrder listElementOrder = new ListElementOrder(stopLossOption.getMarketName(),
                    stopLossOption.getMode() + " / " + stopLossOption.getCondition(), last, max);
            if (!slModel.contains(listElementOrder)) {
                listElementOrder.setMin(min);
                if (below) {
                    listElementOrder.setMaxLabel("High:");
                    listElementOrder.setMinLabel("Stop-loss:");
                } else {
                    listElementOrder.setMaxLabel("Stop-loss:");
                    listElementOrder.setMinLabel("Low:");
                }
                slModel.addElement(listElementOrder);
            } else {
                slModel.getElementAt(x).setLast(last);
            }
        }
        Arrays.sort(new Enumeration[]{slModel.elements()}, ListElementOrder.getComparator());
        slOrdersList.validate();
        slOrdersList.repaint();
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

