package UI;

import Metrics.Launcher;
import Metrics.MetricsAggregator;
import Metrics.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

class Canvas extends JPanel implements DefaultMouseListener {

    private static final Logger LOGGER = LogManager.getLogger(Canvas.class.getName());

    // private final JPopupMenu popupMenu;
    private final ButtonGroup serverButtonGroup = new ButtonGroup();
    private final ButtonGroup metricButtonGroup = new ButtonGroup();
    private final JTextField requestsInput = new JTextField(10);

    private final JTextField startFieldN = new JTextField(10);
    private final JTextField endFieldN = new JTextField(10);
    private final JTextField stepFieldN = new JTextField(10);
    private final JTextField startFieldM = new JTextField(10);
    private final JTextField endFieldM = new JTextField(10);
    private final JTextField stepFieldM = new JTextField(10);
    private final JTextField startFieldD = new JTextField(10);
    private final JTextField endFieldD = new JTextField(10);
    private final JTextField stepFieldD = new JTextField(10);


    Canvas() throws ParseException {
        addMouseListener(this);
        // popupMenu = new JPopupMenu();
        // popupMenu.add(buildPopupMenuItem());

        // general view
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // all settings above
        JPanel settings = new JPanel();
        //settings.setLayout(new BoxLayout(settings, BoxLayout.X_AXIS));
        settings.setLayout(new FlowLayout());

        // in settings above there is a radio group: server
        JPanel server = new JPanel();
        JLabel serverTitle = new JLabel("Server implementation:");
        server.add(serverTitle);
        JRadioButton multiThreadUDPServerButton = new JRadioButton(Launcher.UDP_MULTI);
        multiThreadUDPServerButton.setSelected(true);
        JRadioButton threadPoolUDPServerButton = new JRadioButton(Launcher.UDP_POOL);
        server.setLayout(new BoxLayout(server, BoxLayout.Y_AXIS));
        serverButtonGroup.add(multiThreadUDPServerButton);
        serverButtonGroup.add(threadPoolUDPServerButton);
        server.add(multiThreadUDPServerButton);
        server.add(threadPoolUDPServerButton);

        // also there is a text field for number of requests for each client
        JPanel requests = new JPanel();
        requests.setLayout(new BoxLayout(requests, BoxLayout.X_AXIS));
        // JLabel requestsTitle = new JLabel("Specify the number of requests for each client:");
        JLabel requestsTitle = new JLabel("X: ");
        requests.add(requestsTitle);
//        JFormattedTextField requestsInput = new JFormattedTextField(new MaskFormatter("##########"));
        requests.add(requestsInput);
        requestsInput.setText("10");

        // choose parameter
        JPanel metric = new JPanel();
        JLabel metricTitle = new JLabel("Metric:");
        metric.add(metricTitle);
        JRadioButton elementsNumber = new JRadioButton("N");
        elementsNumber.setSelected(true);
        JRadioButton clientsNumber = new JRadioButton("M");
        JRadioButton time = new JRadioButton("Delta");
        metric.setLayout(new BoxLayout(metric, BoxLayout.Y_AXIS));
        metricButtonGroup.add(elementsNumber);
        metricButtonGroup.add(clientsNumber);
        metricButtonGroup.add(time);
        metric.add(elementsNumber);
        metric.add(clientsNumber);
        metric.add(time);

        settings.add(server);
        settings.add(requests);
        settings.add(metric);


        // range N
        JLabel startN = new JLabel("Range N start: ");
        settings.add(startN);
        settings.add(startFieldN);
        startFieldN.setText("1000");
        JLabel endN = new JLabel("Range N end: ");
        settings.add(endN);
        settings.add(endFieldN);
        endFieldN.setText("1000");
        JLabel stepN = new JLabel("Range N step: ");
        settings.add(stepN);
        settings.add(stepFieldN);
        stepFieldN.setText("0");

        // range M
        JLabel startM = new JLabel("Range M start: ");
        settings.add(startM);
        settings.add(startFieldM);
        startFieldM.setText("20");
        JLabel endM = new JLabel("Range M end: ");
        settings.add(endM);
        settings.add(endFieldM);
        endFieldM.setText("30");
        JLabel stepM = new JLabel("Range M step: ");
        settings.add(stepM);
        settings.add(stepFieldM);
        stepFieldM.setText("1");

        // range d
        JLabel startD = new JLabel("Range D start: ");
        settings.add(startD);
        settings.add(startFieldD);
        startFieldD.setText("500");
        JLabel endD = new JLabel("Range D end: ");
        settings.add(endD);
        settings.add(endFieldD);
        endFieldD.setText("500");
        JLabel stepD = new JLabel("Range D step: ");
        settings.add(stepD);
        settings.add(stepFieldD);
        stepFieldD.setText("0");

        add(settings);

        JButton run = new JButton("Run");
        run.addActionListener(e -> {
            try {
                calculate();
            } catch (
                    NoSuchMethodException
                            | InterruptedException
                            | IllegalAccessException
                            | ExecutionException
                            | IOException
                            | InstantiationException
                            | InvocationTargetException e1) {
                e1.printStackTrace();
            }
        });
        add(run);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                repaint();
                break;
            case MouseEvent.BUTTON3:
                break;
            default:
                break;
        }
    }

    public void calculate()
            throws
            NoSuchMethodException,
            InterruptedException,
            ExecutionException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException,
            IOException {
        // pick parameters

        // get architecture
        String arch = "";
        for (Enumeration<AbstractButton> e = serverButtonGroup.getElements(); e.hasMoreElements();) {
            JRadioButton cur = (JRadioButton) e.nextElement();
            if(cur.isSelected()) {
                arch = cur.getText();
                break;
            }
        }
        // get X
        int x = Integer.parseInt(requestsInput.getText());
        // get metric
        String metric = "";
        for (Enumeration<AbstractButton> e = metricButtonGroup.getElements(); e.hasMoreElements();) {
            JRadioButton cur = (JRadioButton) e.nextElement();
            if(cur.isSelected()) {
                metric = cur.getText();
                break;
            }
        }
        // get range
        Parameter n = new Parameter(
                "N",
                Integer.parseInt(startFieldN.getText()),
                Integer.parseInt(endFieldN.getText()),
                Integer.parseInt(stepFieldN.getText())
        );
        Parameter m = new Parameter(
                "M",
                Integer.parseInt(startFieldM.getText()),
                Integer.parseInt(endFieldM.getText()),
                Integer.parseInt(stepFieldM.getText())
        );
        Parameter d = new Parameter(
                "D",
                Integer.parseInt(startFieldD.getText()),
                Integer.parseInt(endFieldD.getText()),
                Integer.parseInt(stepFieldD.getText())
        );


        // call server with parameters
        Launcher launcher;
        try {
            launcher = new Launcher(d, m, n, x, arch);
        } catch (
                InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e
        ) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        MetricsAggregator ma = launcher.launch();
        ma.draw();
        ma.storeInfo();
        ma.store();

        // repaint
        repaint();
    }

    public void clear() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // To execute this code call repaint method
    }

    private JMenuItem buildPopupMenuItem() {
        // Return JMenuItem called "Remove point"
        // Point should be removed after click
        return null;
    }
}
