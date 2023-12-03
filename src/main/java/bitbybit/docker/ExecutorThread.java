package bitbybit.docker;



import java.util.LinkedList;
import java.util.Queue;

public class ExecutorThread extends Thread {


    private final Queue<ContainerTask> taskQueue;
    private String containerid;
    private boolean shouldrun;



    public ExecutorThread() {
        this.taskQueue = new LinkedList<>();
        this.shouldrun = true;
    }





    public void addTask(ContainerTask task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
      while(shouldrun) {
            while (!taskQueue.isEmpty() && shouldrun) {
                ContainerTask task = taskQueue.poll();
                if (task != null) {
                    if (task instanceof StartContainerTask){
                        task = (StartContainerTask) task;
                        if (((StartContainerTask) task).getmil()>=0){
                            task.execute(((StartContainerTask) task).getmil());
                        } else task.execute();
                    }else if (task instanceof PauseContainerTask){
                        task = (PauseContainerTask) task;
                        if ((( PauseContainerTask) task).getmil()>=0){
                            task.execute(((PauseContainerTask) task).getmil());
                        } else task.execute();
                    } else if (task instanceof RestartContainerTask){
                        task = (RestartContainerTask) task;
                        if (((RestartContainerTask)task).getmil()>=0){
                            task.execute(((RestartContainerTask) task).getmil());
                        } else task.execute();
                    } else task.execute();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e){
                        System.err.println("Task execution interrupted");
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){

            }

        }
        System.out.println("Stopping");
    }


    public interface ContainerTask {

        void execute(int mil);
        void execute();
    }




    public static class StartContainerTask implements ContainerTask {
        private final String containerid;
        private final int mil;

        public StartContainerTask(String containerid, int mil) {
            this.containerid = containerid;
               this.mil = mil;
        }

        @Override
        public void execute(int mil) {
        try {
            DockerInstance.startContainer(containerid);
            try {
                Thread.sleep(mil);
            } catch (InterruptedException e){
                System.err.println("Error executing container task "+ e.getMessage());
            }
            DockerInstance.stopContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while Starting container");
        }
        }
        public void execute(){
            try {
                DockerInstance.startContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while Starting container");
            }
        }
        public int getmil(){
            return mil;
        }
    }
    public static class StopContainerTask implements ContainerTask {

        private final String containerid;

        public StopContainerTask(String containerid) {
            this.containerid = containerid;
        }
        @Override
        public void execute(int mil){

        }

        @Override
        public void execute() {
        try {
            DockerInstance.stopContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while stoping container");
        }
        }
    }
    public static class PauseContainerTask implements ContainerTask {

        private final String containerid;
        private final int mil;
    public PauseContainerTask(String containerid, int mil) {
        this.containerid = containerid;
        this.mil = mil;
    }

    @Override
    public void execute() {
        try {
            DockerInstance.pauseContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while pausing container");
        }
    }
        @Override
        public void execute(int mil) {
            try {
                DockerInstance.pauseContainer(containerid);
                try{
                    Thread.sleep(mil);
                } catch (InterruptedException e){
                    System.err.println("Error executing container task "+e.getMessage());
                }
                DockerInstance.unpauseContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while pausing container");
            }
        }
        public int getmil(){
            return mil;
        }
    }


    public static class UnpauseContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;
        public UnpauseContainerTask(String containerid, DockerInstance dockerInstance) {
            this.dockerInstance = dockerInstance;
            this.containerid = containerid;
        }

        @Override
        public void execute() {
            try {
                dockerInstance.unpauseContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while unpausing container");
            }
        }
        public void execute(int mil){

        }
    }
    public static class RestartContainerTask implements ContainerTask {

        private final String containerid;
        private final int mil;
        public RestartContainerTask( String containerid, int mil) {
            this.containerid = containerid;
            this.mil = mil;
        }

        @Override
        public void execute() {
            try {
                DockerInstance.restartContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while restarting container");
            }
        }
        public void execute(int mil){
            try {
                DockerInstance.restartContainer(containerid);
                Thread.sleep(mil);
                DockerInstance.stopContainer(containerid);
            } catch (InterruptedException e) {
                System.err.println("Error while executing container task "+ e.getMessage());
            }catch (Exception e) {
                System.err.println("Error while restarting container");
            }
        }
        public int getmil(){
            return mil;
        }
    }
    public static class RemoveContainerTask implements ContainerTask {

        private final String containerid;
        public RemoveContainerTask(String containerid) {
            this.containerid =containerid;
        }

        @Override
        public void execute() {
            try {
                System.out.println("Removing cont");
                DockerInstance.removeContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while removing container");
            }
        }
        @Override
        public void execute(int mil){

        }
    }
    public  static class ExecuteCommandContainerTask implements ContainerTask {
        private final String containerid;
        private final String[] command;
        public ExecuteCommandContainerTask(String containerid, String[] command) {
            this.containerid = containerid;
            this.command = command;
        }
        @Override
        public void execute() {
            try {
                DockerInstance.executeCommandInContainer(containerid, command);
            } catch (Exception e) {
                System.err.println("Error while executing command in container");
            }
        }
        @Override
        public void execute(int mil){}
    }
    public static class KillContainerTask implements ContainerTask {

        private final String containerid;
        public KillContainerTask(String containerid) {
            this.containerid = containerid;
        }

        @Override
        public void execute() {
            try {
                DockerInstance.killContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while killing container");
            }
        }
        @Override
        public void execute(int mil){

        }
    }
    public void stopthread(){
        shouldrun = false;
    }
    public boolean getstopthread(){
       return shouldrun ;
    }

}
