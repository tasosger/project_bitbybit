package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorThread extends Thread {
    private static int measurement = 0;
    private final DockerClient dockerClient=initializeDockerClient();
    private String containerid;
    private volatile boolean shouldRun;
    protected static final ReentrantLock dockerLock = new ReentrantLock();
    private boolean running;
    private final long startTime;
    public static  ArrayList<ContainerMetrics> metricsList = new ArrayList<>();
    private final Map<String, Object> containerMetrics;
    private static final int MEASTIME = 5000;

    public MonitorThread(String containerid) {
        this.containerMetrics = new HashMap<>();
        this.shouldRun = true;
        this.startTime = System.currentTimeMillis();
        this.containerid = containerid;
    }

    @Override
    public void run() {
        try {
            while (shouldRun) {
                if (DockerInstance.isContainerRunning(containerid)) {
                    dockerLock.lock();
                    collectAndPersistMetrics();
                    dockerLock.unlock();
                    Thread.sleep(MEASTIME);
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Monitoring interrupted: " +containerid);
            e.printStackTrace();
        } catch (Exception e) {

        }
    }
    public static void increaseM(){
        measurement++;
    }
    public static int getMeasurement(){
        return measurement;
    }


    private void collectAndPersistMetrics() {
        try {
            dockerClient.statsCmd(containerid).exec(new StatsCallback());
        } catch (Exception e) {
            System.err.println("Error during metrics collection"+ e.getMessage());
        }
    }
    public void stopThread(){
        shouldRun = false;
    }


    private class StatsCallback implements ResultCallback<Statistics> {
        private int measurement;
        //public StatsCallback(int m){
           // measurement = m;
        //}
        @Override
        public void onStart(Closeable closeable) {;
        }
        @Override
        public void onNext(Statistics stats) {
            try {
                dockerLock.lock();
                MonitorThread.increaseM();
                long memoryUsage = stats.getMemoryStats().getUsage();
                double cpuUsage = stats.getCpuStats().getCpuUsage().getTotalUsage();
                long networkRx = stats.getNetworks().get("eth0").getRxBytes();
                long networkTx = stats.getNetworks().get("eth0").getTxBytes();
                String timestamp = stats.getRead();
                persistMetrics(memoryUsage, cpuUsage, timestamp, containerid, networkRx,networkTx,
                        DockerInstance.getContainer(containerid).getImage(), MonitorThread.getMeasurement());
                dockerLock.unlock();
            } catch (NullPointerException e){
                if(isContainerRunning(containerid)) {
                    System.err.println("Error getting metrics " + e.getMessage());
                }
            } catch (Exception e){
                System.err.println("Error during metrics collection "+e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Error during monitoring "+throwable.getMessage());
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void close() {
        }
    }

    public Map<String, Object> getContainerMetrics() {
        return containerMetrics;
    }


    private void persistMetrics(long memoryUsage, double cpuUsage, String timestamp, String containerid, long nr,long nt, String imageName, int measurement) {
        dockerLock.lock();
        ContainerMetrics containerMetrics = new ContainerMetrics(memoryUsage, cpuUsage, timestamp, containerid,nr ,nt, imageName,measurement);
        DatabaseHandler.addm(containerMetrics);
        metricsList.add(containerMetrics);
        dockerLock.unlock();
    }
    private void writeMetricsToCSV(ContainerMetrics containerMetrics) {

        String resourcePath = "metrics.csv";

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                Path filePath = Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("")).toURI()).resolve(resourcePath);
                Files.createFile(filePath);
            }
            Path filePath = Path.of(getClass().getClassLoader().getResource(resourcePath).toURI());
            Files.writeString(filePath, containerMetrics.toString() + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.err.println("Error writing metrics to CSV: " + e.getMessage());
        }
    }
    private boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
            InspectContainerResponse.ContainerState state = inspectionResponse.getState();
            return state != null && state.getRunning();
        } catch (NotFoundException e) {
            System.err.println("Container not found: " + containerId); // Container not found, consider it not running
        } catch (Exception e) {
            System.err.println("An error has occurred "+e.getMessage());
        }
        return false;
    }
    public static class ContainerMetrics {
        private final long memoryUsage;
        private final String containerId;
        private final double cpuUsage;
        private final String  timestamp;
        private final String imageName;
        private final int measurement;
        private final long networkRx;
        private final long networkTx;

        public ContainerMetrics(long memoryUsage, double cpuUsage, String timestamp, String containerId, long nr,long nt, String imageName,int measurement) {
            this.memoryUsage = memoryUsage;
            this.cpuUsage = cpuUsage;
            this.timestamp = timestamp;
            this.containerId = containerId;
            this.imageName = imageName;
            this.measurement=measurement;
            this.networkRx = nr;
            this.networkTx = nt;
        }

        public long getMemoryUsage() {
            try {
                return memoryUsage;
            } catch (Exception e) {
              System.err.println("Can not return Memory");
            }
            return -1;
        }
        public double getCPUusage() {
            try {
                return cpuUsage;
            } catch (Exception e) {
                System.err.println("Can not return CPU usage");
            }
            return -1;
        }
        public String getContainerID() {
            try {
                return containerId;
            } catch (Exception e) {
                System.err.println("Can not return container ID");
            }
            return null;
        }
        public int getMeasurement(){
            try {
                return measurement;
            } catch (Exception e) {
                System.err.println("Can not return metrics measurement");
            }
            return -1;
        }
        public long getNetworkRx(){
            return networkRx;
        }
        public long getNetworkTx(){
            return networkTx;
        }
        public String getimageName() {
            try {
                return imageName;
            } catch (Exception e) {
                System.err.println("Can not return container ID");
            }
            return null;
        }
        public String getTimestamp() {
            try {
                return timestamp;
            } catch (Exception e) {
                System.err.println("Can not return timestamp");
            }
            return null;
        }
        public String toString() {
            return (measurement+","+containerId + "," + memoryUsage +","+ cpuUsage + "," + imageName +","+"," + timestamp);
        }
    }
    private static DockerClient initializeDockerClient() {
        return DockerClientBuilder.getInstance
                        ("tcp://localhost:2375")
                .build();
    }
}