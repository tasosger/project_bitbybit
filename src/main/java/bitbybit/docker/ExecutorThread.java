package bitbybit.docker;



import java.util.LinkedList;
import java.util.Queue;

public class ExecutorThread extends Thread {


    private final DockerInstance dockerInstance;
    private final Queue<ContainerTask> taskQueue;
    private String containerid;
    private boolean shouldrun;



    public ExecutorThread(DockerInstance dockerInstance) {


        this.taskQueue = new LinkedList<>();
        this.dockerInstance = dockerInstance;
        this.shouldrun = true;
    }

    public DockerInstance getDockerInstance() {
        return dockerInstance;
    }



    public void addTask(ContainerTask task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
      while(shouldrun || !taskQueue.isEmpty()) {
            while (!taskQueue.isEmpty()) {
                ContainerTask task = taskQueue.poll();
                if (task != null) {
                    task.execute();
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
    }


    public interface ContainerTask {

        void execute();
    }




    public static class StartContainerTask implements ContainerTask {


        private final String containerid;
        private final DockerInstance dockerInstance;

        public StartContainerTask(String containerid, DockerInstance dockerInstance) {
            this.containerid = containerid;
               this.dockerInstance = dockerInstance;
        }

        @Override
        public void execute() {
        try {
            dockerInstance.startContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while Starting container");
        }
        }
    }
    public static class StopContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;

        public StopContainerTask(String containerid, DockerInstance dockerInstance) {
            this.dockerInstance = dockerInstance;
            this.containerid = containerid;
        }

        @Override
        public void execute() {
        try {
            dockerInstance.stopContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while stoping container");
        }
        }
    }
    public static class PauseContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;
    public PauseContainerTask(String containerid, DockerInstance dockerInstance) {
        this.dockerInstance = dockerInstance;
        this.containerid = containerid;
    }

    @Override
    public void execute() {
        try {
            dockerInstance.pauseContainer(containerid);
        } catch (Exception e) {
            System.err.println("Error while pausing container");
        }
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
    }
    public static class RestartContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;
        public RestartContainerTask( String containerid,DockerInstance dockerInstance) {
            this.dockerInstance = dockerInstance;
            this.containerid = containerid;
        }

        @Override
        public void execute() {
            try {
                dockerInstance.restartContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while restarting container");
            }
        }
    }
    public static class RemoveContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;
        public RemoveContainerTask(String containerid, DockerInstance dockerInstance) {
            this.dockerInstance = dockerInstance;
            this.containerid =containerid;
        }

        @Override
        public void execute() {
            try {
                dockerInstance.removeContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while removing container");
            }
        }
    }
    public  static class ExecuteCommandContainerTask implements ContainerTask {
        private final String containerid;
        private final DockerInstance dockerInstance;
        private final String[] command;
        public ExecuteCommandContainerTask(String containerid, String[] command, DockerInstance dockerInstance) {
            this.containerid = containerid;
            this.command = command;
            this.dockerInstance=dockerInstance;
        }
        @Override
        public void execute() {
            try {
                dockerInstance.executeCommandInContainer(containerid, command);
            } catch (Exception e) {
                System.err.println("Error while executing command in container");
            }
        }
    }
    public static class KillContainerTask implements ContainerTask {

        private final String containerid;
        private final DockerInstance dockerInstance;
        public KillContainerTask(String containerid, DockerInstance dockerInstance) {
            this.containerid = containerid;
            this.dockerInstance = dockerInstance;
        }

        @Override
        public void execute() {
            try {
                dockerInstance.killContainer(containerid);
            } catch (Exception e) {
                System.err.println("Error while killing container");
            }
        }
    }
    public void stopthread(){
        shouldrun = false;
    }
    public boolean getstopthread(){
       return shouldrun ;
    }

}
