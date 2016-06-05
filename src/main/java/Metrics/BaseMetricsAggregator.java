package Metrics;

public interface BaseMetricsAggregator {
    int REQUEST = 1;
    int CLIENT = 2;

    void submitRequest(long val);
    void submitClient(long val);
}
