package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MonitorThread extends Thread {

    private final DockerClient dockerClient;
    private final String containerId;
    private volatile boolean shouldRun;
    private final long startTime;

    // Map to store container metrics
    private final Map<String, Object> containerMetrics;

    public MonitorThread(DockerClient dockerClient, String containerId) {
        this.dockerClient = dockerClient;
        this.containerId = containerId;
        this.containerMetrics = new HashMap<>();
        this.shouldRun = true;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            // Run the monitoring loop every 5 seconds
            while (shouldRun && (System.currentTimeMillis() - startTime) <= 30000) { // Run for 30 seconds
                collectAndPersistMetrics();
                System.out.println("Thread is running");
                Thread.sleep(15000); // Sleep for 5 seconds
            }
        } catch (InterruptedException e) {
            // Handle interruption (e.g., thread interrupted while sleeping)
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopMonitoring() {
        shouldRun = false;
        interrupt();
    }
    public void printFinalMetrics() {
        //System.out.println("FINAL METRICS "+ this.containerId + " " );
        //System.out.println("Metrics: " + containerMetrics);
    }

    private void collectAndPersistMetrics() {
        try {
            // Create a StatsCmd to monitor container statistics
            dockerClient.statsCmd(containerId).exec(new StatsCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class StatsCallback implements ResultCallback<Statistics> {
        @Override
        public void onStart(Closeable closeable) {
            //System.out.println("WE ARE IN");
        }

        @Override
        public void onNext(Statistics stats) {
            // Process container statistics

            // Example: Get memory usage
            long memoryUsage = stats.getMemoryStats().getUsage();

            // Example: Get CPU usage
            double cpuUsage = stats.getCpuStats().getCpuUsage().getTotalUsage();

            // Example: Get network stats
            long networkRx = stats.getNetworks().get("eth0").getRxBytes();
            long networkTx = stats.getNetworks().get("eth0").getTxBytes();

            // Example: Get timestamp of the statistics update
            //Date timestamp = new Date(stats.getRead());

            // Store the metrics in the map
            containerMetrics.put("MemoryUsage", memoryUsage);
            containerMetrics.put("CpuUsage", cpuUsage);
            containerMetrics.put("NetworkRx", networkRx);
            containerMetrics.put("NetworkTx", networkTx);
            //containerMetrics.put("Timestamp", timestamp);
            // System.out.println("code entered");
            // Print metrics to console
            printMetrics(containerMetrics);

            // Persist metrics
            persistMetrics(memoryUsage, cpuUsage);
        }

        @Override
        public void onError(Throwable throwable) {
            //System.out.println("Code is wrong" + throwable.getMessage());
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Monitoring complete. Adios");
        }

        @Override
        public void close() {
            System.out.println("Closing...");
        }
    }

    private void printMetrics(Map<String, Object> metrics) {
        // Print metrics to console
       // System.out.println("Metrics: " + metrics);
    }
    public Map<String, Object> getContainerMetrics() {
        return containerMetrics;
    }


    private void persistMetrics(long memoryUsage, double cpuUsage) {
        // Add logic to persist metrics to a storage system (e.g., database)
        // This could involve using an ORM, JDBC, or any other storage mechanism
        // For demonstration purposes, print to console
        //System.out.println("Persisting Metrics: MemoryUsage=" + memoryUsage + ", CpuUsage=" + cpuUsage);
    }
}