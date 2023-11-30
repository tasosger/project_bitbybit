package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorThread extends Thread {
    private static int measurement = 0;
    private final DockerClient dockerClient;
    private String containerid;
    private final DockerInstance dockerInstance;
    private volatile boolean shouldRun;
    private static final ReentrantLock dockerLock = new ReentrantLock();
    private boolean running;
    private final long startTime;
    public static final ArrayList<ContainerMetrics> metricsList = new ArrayList<>();

    // Map to store container metrics
    private final Map<String, Object> containerMetrics;

    public MonitorThread(DockerClient dockerClient, DockerInstance dockerInstance, String containerid) {
        this.dockerClient = dockerClient;
        this.containerMetrics = new HashMap<>();
        this.shouldRun = true;
        this.dockerInstance =dockerInstance;
        this.startTime = System.currentTimeMillis();
        this.containerid = containerid;
    }

    @Override
    public void run() {
        try {
            while (shouldRun && (System.currentTimeMillis() - startTime) <= 30000) {
                dockerLock.lock();
                measurement++;
                dockerLock.unlock();
                collectAndPersistMetrics(measurement);
                Thread.sleep(5000);
                if(!isContainerRunning(containerid)) {
                    shouldRun = false;
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Monitoring interrupted: " +containerid);
            e.printStackTrace();
        } catch (Exception e) {

        }
    }


    private void collectAndPersistMetrics(int m) {
        try {
            dockerClient.statsCmd(containerid).exec(new StatsCallback(measurement));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class StatsCallback implements ResultCallback<Statistics> {
        private int measurement;
        public StatsCallback(int m){
            measurement = m;
        }
        @Override
        public void onStart(Closeable closeable) {;
        }

        @Override
        public void onNext(Statistics stats) {
            try {
                long memoryUsage = stats.getMemoryStats().getUsage();
                double cpuUsage = stats.getCpuStats().getCpuUsage().getTotalUsage();
                long networkRx = stats.getNetworks().get("eth0").getRxBytes();
                long networkTx = stats.getNetworks().get("eth0").getTxBytes();
                String timestamp = stats.getRead();
                containerMetrics.put("MemoryUsage", memoryUsage);
                containerMetrics.put("CpuUsage", cpuUsage);
                containerMetrics.put("NetworkRx", networkRx);
                containerMetrics.put("NetworkTx", networkTx);
                containerMetrics.put("Timestamp", timestamp);
                containerMetrics.put("ContainerID", containerid);
                containerMetrics.put("ImageName", dockerInstance.getContainer(containerid).getImage());
                containerMetrics.put("Measurement", measurement);
                persistMetrics(memoryUsage, cpuUsage, timestamp, containerid, dockerInstance.getContainer(containerid).getImage(), measurement);
            } catch (NullPointerException e){
                System.err.println("Error getting metrics " + e.getMessage());
            } catch (Exception e){
                System.err.println("Error during metrics collection "+e.getMessage());
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


    private void persistMetrics(long memoryUsage, double cpuUsage, String timestamp, String containerid, String imageName, int measurement) {
        dockerLock.lock();
        ContainerMetrics containerMetrics = new ContainerMetrics(memoryUsage, cpuUsage, timestamp, containerid, imageName,measurement);
        metricsList.add(containerMetrics);
        dockerLock.unlock();
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

        public ContainerMetrics(long memoryUsage, double cpuUsage, String timestamp, String containerId, String imageName,int measurement) {
            this.memoryUsage = memoryUsage;
            this.cpuUsage = cpuUsage;
            this.timestamp = timestamp;
            this.containerId = containerId;
            this.imageName = imageName;
            this.measurement=measurement;
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
            return (measurement+" "+containerId + " " + memoryUsage +" "+ cpuUsage + " " + imageName +" "+" " + timestamp);
        }
    }

}