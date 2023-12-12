package bitbybit.docker;

import java.util.PriorityQueue;
import java.util.Queue;

public class DatabaseThread extends Thread{
    private static Queue<MonitorThread.ContainerMetrics> metrics = new  PriorityQueue<MonitorThread.ContainerMetrics>() {
    };
    private boolean shouldrun=true;
    @Override
    public void run(){
        while (shouldrun){
            while (!metrics.isEmpty()){
                MonitorThread.ContainerMetrics c = metrics.poll();
                DatabaseHandler.add_metrics(c);
            }
        }

    }
    public static void addm(MonitorThread.ContainerMetrics c){
        metrics.add(c);
    }

}
