package bitbybit.docker;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class DatabaseThread extends Thread{

    private boolean shouldrun=true;
    @Override
    public void run(){
        while (shouldrun){
           try{
               Thread.sleep(1000);
           } catch (InterruptedException e){}
            while (!DatabaseHandler.metrics.isEmpty()){
                MonitorThread.ContainerMetrics c = DatabaseHandler.metrics.poll();
                DatabaseHandler.add_metrics(c);
            }
        }

    }


}
