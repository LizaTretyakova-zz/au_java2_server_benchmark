package UI;

import Metrics.MetricsAggregator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

class Canvas extends JPanel implements DefaultMouseListener {

    private static final Logger LOGGER = LogManager.getLogger(Canvas.class.getName());

    // private final JPopupMenu popupMenu;
    private final ButtonGroup serverButtonGroup = new ButtonGroup();
    private final ButtonGroup metricButtonGroup = new ButtonGroup();
    private final JTextField requestsInput = new JTextField(10);
    private final JTextField startField = new JTextField(10);
    private final JTextField endField = new JTextField(10);
    private final JTextField stepField = new JTextField(10);


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
        JRadioButton singleThreadServerButton = new JRadioButton("Singlethread ");
        JRadioButton multiThreadServerButton = new JRadioButton("Multithread ");
        JRadioButton threadPoolServerButton = new JRadioButton("Threadpool ");
        JRadioButton nonBlockingServerButton = new JRadioButton("Nonblocking ");
//        ButtonGroup serverButtonGroup = new ButtonGroup();
        server.setLayout(new BoxLayout(server, BoxLayout.Y_AXIS));
        serverButtonGroup.add(singleThreadServerButton);
        serverButtonGroup.add(multiThreadServerButton);
        serverButtonGroup.add(threadPoolServerButton);
        serverButtonGroup.add(nonBlockingServerButton);
        server.add(singleThreadServerButton);
        server.add(multiThreadServerButton);
        server.add(threadPoolServerButton);
        server.add(nonBlockingServerButton);

        // also there is a text field for number of requests for each client
        JPanel requests = new JPanel();
        requests.setLayout(new BoxLayout(requests, BoxLayout.X_AXIS));
        // JLabel requestsTitle = new JLabel("Specify the number of requests for each client:");
        JLabel requestsTitle = new JLabel("X: ");
        requests.add(requestsTitle);
//        JFormattedTextField requestsInput = new JFormattedTextField(new MaskFormatter("##########"));
        requests.add(requestsInput);

        // choose parameter
        JPanel metric = new JPanel();
        JLabel metricTitle = new JLabel("Metric:");
        metric.add(metricTitle);
        JRadioButton elementsNumber = new JRadioButton("N");
        JRadioButton clientsNumber = new JRadioButton("M");
        JRadioButton time = new JRadioButton("Delta");
//        ButtonGroup metricButtonGroup = new ButtonGroup();
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


        // range
        JLabel start = new JLabel("Range start: ");
//        JFormattedTextField startField = new JFormattedTextField(new MaskFormatter("##########"));
        settings.add(start);
        settings.add(startField);
        JLabel end = new JLabel("Range end: ");
//        JFormattedTextField endField = new JFormattedTextField(new MaskFormatter("##########"));
        settings.add(end);
        settings.add(endField);
        JLabel step = new JLabel("Range step: ");
//        JFormattedTextField stepField = new JFormattedTextField(new MaskFormatter("##########"));
        settings.add(step);
        settings.add(stepField);

        add(settings);

        // Poekhali!1!
        JButton run = new JButton("Run");
        run.addActionListener(e -> calculate());
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

    public void calculate() {
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
        int start = Integer.parseInt(startField.getText());
        int end = Integer.parseInt(endField.getText());
        int step = Integer.parseInt(stepField.getText());

        // debug
        LOGGER.debug("read parameters");
        MetricsAggregator.drawMetric("Sample", "X", "Y", Arrays.asList(0L, 1L, 2L), Arrays.asList(2L, 1L, 0L));

        // call server with parameters
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
