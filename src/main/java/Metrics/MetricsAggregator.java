package Metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MetricsAggregator {

    public static final String NAME = "results";
    public static final String INFO = "info";
    public static final String REQUEST = "request";
    public static final String CLIENT = "client";
    public static final String AVG = "avg";

    private static final Logger LOGGER = LogManager.getLogger(MetricsAggregator.class);

    private final String arch;
    private final int x;
    private final Parameter n;
    private final Parameter m;
    private final Parameter d;

    private final List<Long> requestTime = new ArrayList<>();
    private final List<Long> clientTime = new ArrayList<>();
    private final List<Long> avgTime = new ArrayList<>();

    public MetricsAggregator(String arch, int x, Parameter n, Parameter m, Parameter d) {
        this.arch = arch;
        this.x = x;
        this.n = n;
        this.m = m;
        this.d = d;
    }

    public void submitRequest(long val) {
        requestTime.add(val);
    }

    public void submitClient(long val) {
        clientTime.add(val);
    }

    public void submitAvg(long val) {
        avgTime.add(val);
    }

    public void submit(long request, long client, long avg) {
        submitRequest(request);
        submitClient(client);
        submitAvg(avg);
    }

    private FileWriter createFile(String purpose) throws IOException {
        if (!Files.exists(Paths.get(NAME))) {
            Files.createDirectory(Paths.get(NAME));
        }

        String filename =
                Paths.get(NAME, NAME + purpose + Long.toString(System.currentTimeMillis()) + ".txt").toString();
        return new FileWriter(new File(filename));
    }

    public void storeInfo() throws IOException {
        FileWriter writer = createFile(INFO);

        writer.append(arch);
        writer.append("\n");
        writer.append(Integer.toString(x));
        writer.append("\n");
        n.writeTo(writer);
        m.writeTo(writer);
        d.writeTo(writer);
        writer.append("\n");
        writer.flush();
        writer.close();
    }

    private void storeMetric(String metricName, List<Long> val) throws IOException {
        FileWriter writer = createFile(metricName);
        writer.append(metricName);
        writer.append("\n");
        for(long a: val) {
            writer.append(Long.toString(a));
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }

    public void storeRequest() throws IOException {
        storeMetric(REQUEST, requestTime);
    }
    public void storeClient() throws IOException {
        storeMetric(CLIENT, clientTime);
    }
    public void storeAvg() throws IOException {
        storeMetric(AVG, avgTime);
    }

    public void store() throws IOException {
        storeInfo();
        storeRequest();
        storeClient();
        storeAvg();
    }

    public static void drawMetric(String metricName, String xName, String yName, List<Long> xData, List<Long> yData) {
//        double[] xData = new double[] { 0.0, 1.0, 2.0 };
//        double[] yData = new double[] { 2.0, 1.0, 0.0 };

        // Create Chart
        XYChart chart = QuickChart.getChart(metricName, xName, yName, yName + "(" + xName + ")", xData, yData);

        // Show it
        new SwingWrapper(chart).displayChart();
    }

    public void draw() {
        Parameter changing = null;
        if(n.isChanging()) {
            changing = n;
        } else if (m.isChanging()) {
            changing = m;
        } else if (d.isChanging()) {
            changing = d;
        }

        List<Long> xAxis = new ArrayList<>();
        for(long i = (long) changing.getStart(); i <= (long) changing.getEnd(); i += changing.getStep()) {
            xAxis.add(i);
        }

        drawMetric("Request time", "Request time", changing.getName(), xAxis, requestTime);
        drawMetric("Client time", "Client time", changing.getName(), xAxis, clientTime);
        drawMetric("Avg time", "Avg time", changing.getName(), xAxis, avgTime);
    }
}
