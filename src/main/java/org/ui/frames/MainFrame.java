package org.ui.frames;

import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.logic.models.misc.BalancesSet;
import org.logic.models.responses.v2.MarketTicksResponse;
import org.logic.utils.ModelBuilder;
import org.preferences.managers.PreferenceManager;
import org.ui.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
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
    private JLabel labelOpenOrdersStatus, labelEmailAddress, labelApi, labelApiSecret;
    private JComboBox<String> jComboBoxMode;
    private JTextArea jtaScrollPane;
    private JScrollPane jScrollPane;
    private JButton btnCreateTransaction;
    private ClassicTransactionFrame classicTransactionFrame;
    private StopLossFrame stopLossFrame;
    private PieChartFrame pieChartFrame;
    private EmailSetupFrame emailSetupFrame;
    private APISetupFrame apiSetupFrame;

    public MainFrame() {
        this.setTitle("AltBot " + Constants.VERSION);
        createMenuBar();

        Container cp = getContentPane();
        JPanel pMain = new JPanel();
        pMain.setBorder(new TitledBorder(new EtchedBorder()));
        pMain.setLayout(new GridLayout(4, 1));

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
        cp.add(pMain, BorderLayout.NORTH);

        jtaScrollPane = new JTextArea(getWidth(), 300);
        jtaScrollPane.setEditable(false);
        jScrollPane = new JScrollPane(jtaScrollPane);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        cp.add(jScrollPane, BorderLayout.CENTER);

        JPanel donationPanel = new JPanel();
        donationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        donationPanel.setBorder(new TitledBorder(new EtchedBorder()));
        donationPanel.setLayout(new GridLayout(3, 1));
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
        cp.add(donationPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        // Full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        showPieChart();
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
        settings.add(apiSetupMenuItem);
        settings.add(jCheckBoxMenuItem);
        settings.add(emailSetupMenuItem);

        // Chart button
        JButton button = new JButton();
//        button.setBorder(BorderFactory.createEmptyBorder());
//        button.setContentAreaFilled(false);
        try {
            Image img = ImageIO.read(getClass().getClassLoader().getResource("pie_chart.png"));
            img = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPieChart();
            }
        });

        jMenuBar.add(file);
        jMenuBar.add(settings);
        jMenuBar.add(button);
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

    private void showPieChart() {
        if (pieChartFrame == null || pieChartFrame.isClosed()) {
            pieChartFrame = new PieChartFrame();
            return;
        }
        if (!pieChartFrame.isActive() || !pieChartFrame.isVisible()) {
            pieChartFrame.toFront();
        }
    }

    public void updatePieChartFrame(Map<String, BalancesSet> map) {
        if (pieChartFrame != null && !pieChartFrame.isClosed()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pieChartFrame.updateChart(map);
                }
            });
        }
    }

    public PieChartFrame getPieChartFrame() {
        return pieChartFrame;
    }

    public boolean isPieChartVisible() {
        return pieChartFrame != null && !pieChartFrame.isClosed();
    }
}

