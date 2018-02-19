package org.ui.frames;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.logic.models.misc.BalancesSet;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

import static org.preferences.Constants.*;

/**
 * Created by niewinskip on 2016-12-28.
 */
public class MainFrame extends JFrame {

    private final static String[] ARR_MODES = {"Classic", "Stop-loss", "Buy&Sell"};
    private static Logger logger = Logger.getLogger(MainFrame.class);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) screenSize.getWidth();
    int height = (int) screenSize.getHeight();
    private JLabel labelOpenOrdersStatus, labelEmailAddress, labelApi, labelApiSecret;
    private JComboBox<String> jComboBoxMode;
    private JButton btnCreateTransaction;
    private ClassicTransactionFrame classicTransactionFrame;
    private StopLossFrame stopLossFrame;
    private EmailSetupFrame emailSetupFrame;
    private APISetupFrame apiSetupFrame;
    private PieChart pieChartPanel;
    private double LEFT_PANE_WIDTH_RATIO = 0.4f;
    private double CENTER_PANE_WIDTH_RATIO = 0.3f;
    private double RIGHT_PANE_WIDTH_RATIO = 0.3f;

    public MainFrame() {
        this.setTitle("AltBot " + Constants.VERSION);
        createMenuBar();
        Container cp = getContentPane();
        GridBagLayout bag = new GridBagLayout();
        cp.setLayout(bag);

        JPanel leftPanel = createLeftPanel();

        // Merge all main column panels. Add grid layout.
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.4;
        c.weighty = 1;
        c.gridx = 0;
        this.add(leftPanel, c);
        JButton empty1 = new JButton("DAAisfiofdsjsdjsdjofsdijsfdoijsiodfjidsfjsdfojidsfoifsdjF");
        JButton empty2 = new JButton("DAidsjijsfdoifsdjoidsfjiosfjsfodijfdsojfdsojfsdodfsjoidsfAF");
        c.gridx = 1;
        c.weightx = 0.3;
        this.add(empty1, c);
        c.gridx = 2;
        this.add(empty2, c);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // Initialize only after all components sizes were calculated.
        pieChartPanel.initChart();
    }

    private JPanel createLeftPanel() {
        // Top left panel.
        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(new TitledBorder(new EtchedBorder()));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), height));
        leftPanel.setMinimumSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), height));

        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setLayout(new GridLayout(4, 1));
        pMain.setPreferredSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), 120));
        pMain.setMinimumSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), 120));

        // Status bar
        JPanel statusBar = new JPanel();
        statusBar.setBorder(new TitledBorder(new EtchedBorder()));
        statusBar.setLayout(new GridLayout(1, 2));
        labelOpenOrdersStatus = new JLabel();
        labelOpenOrdersStatus.setText("Open orders: ?");
        JPanel status1 = new JPanel();
        status1.setAlignmentX(Component.LEFT_ALIGNMENT);
        status1.add(labelOpenOrdersStatus);
        labelEmailAddress = new JLabel();
        labelEmailAddress.setText(PreferenceManager.getEmailAddress());
        JPanel status2 = new JPanel();
        status2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        status2.add(labelEmailAddress);
        statusBar.add(status1);
        statusBar.add(status2);
        pMain.add(statusBar);

        // Api status bar
        JPanel apiStatusBar = new JPanel();
        apiStatusBar.setBorder(new TitledBorder(new EtchedBorder()));
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
        pMain.add(apiStatusBar);

        // Combo box
        jComboBoxMode = new JComboBox<>(ARR_MODES);
        btnCreateTransaction = new JButton("Create transaction");
        btnCreateTransaction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = jComboBoxMode.getSelectedIndex();
                switch (id) {
                    case 0:
                        openClassicTransactionFrame();
                        break;
                    case 1:
                        openStopLossFrame();
                        break;
                    default:
                        break;
                }
            }
        });
        pMain.add(jComboBoxMode);
        pMain.add(btnCreateTransaction);
        leftPanel.add(pMain, BorderLayout.NORTH);

        // Mid view.
        pieChartPanel = new PieChart((int) (width * LEFT_PANE_WIDTH_RATIO), height);
        leftPanel.add(pieChartPanel, BorderLayout.CENTER);

        // Bottom view
        JPanel donationPanel = new JPanel();
        donationPanel.setLayout(new GridLayout(3, 1));
        donationPanel.setMaximumSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), 60));
        donationPanel.setPreferredSize(new Dimension((int) (width * LEFT_PANE_WIDTH_RATIO), 60));
        Border border = LineBorder.createGrayLineBorder();
        JTextField btcLabel = new JTextField("Donate BTC: " + BTC_DONATION_ADDRESS);
        btcLabel.setEditable(false);
        btcLabel.setBorder(border);
        donationPanel.add(btcLabel);
        JTextField ethLabel = new JTextField("Donate ETH: " + ETH_DONATION_ADDRESS);
        ethLabel.setEditable(false);
        ethLabel.setBorder(border);
        donationPanel.add(ethLabel);
        JTextField ltcLabel = new JTextField("Donate LTC: " + LTC_DONATION_ADDRESS);
        ltcLabel.setEditable(false);
        ltcLabel.setBorder(border);
        donationPanel.add(ltcLabel);
        leftPanel.add(donationPanel, BorderLayout.SOUTH);
        return leftPanel;
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

    public void updateStatusBar(int openOrders, int buyOrdersCount) {
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
}

