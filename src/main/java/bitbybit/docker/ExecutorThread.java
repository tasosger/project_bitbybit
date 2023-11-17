package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ExecutorThread extends Thread {
    private final DockerClient dockerClient;
    private final String containerId;
    private final DockerImage dockerImage;
    private final DockerInstance dockerInstance;
    private final Queue<ContainerTask> taskQueue;


    public ExecutorThread(DockerClient dockerClient, String containerId, List<ContainerTask> tasks, DockerImage dockerImage, DockerInstance dockerInstance) {
        this.dockerClient = dockerClient;
        this.containerId = containerId;
        this.taskQueue = new LinkedList<>();
        this.dockerImage = dockerImage;
        this.dockerInstance = dockerInstance;

    }

    public void addTask(ContainerTask task) {
        taskQueue.add(task);
    }

    @Override
    public void run()  {
        // Process tasks in the queue
        while (!taskQueue.isEmpty()) {
            ContainerTask task = taskQueue.poll();
            if (task != null) {
                task.execute();
            }
        }
    }

    // Define an interface for container-related tasks
    public interface ContainerTask {

        void execute();
    }

    // Example task: StartContainerTask
    public static class StartContainerTask implements ContainerTask {
        private final DockerClient dockerClient;
        private final String containerId;
        private final DockerImage dockerImage;
        private final DockerInstance dockerInstance;

        public StartContainerTask(DockerClient dockerClient, String containerId, DockerInstance dockerInstance, DockerImage dockerImage) {
            this.dockerClient = dockerClient;
            this.containerId = containerId;
            this.dockerImage = dockerImage;
            this.dockerInstance = dockerInstance;
        }

        @Override
        public void execute() {
            dockerInstance.startContainer(containerId);
        }
    }

    // Example task: StopContainerTask
    public static class StopContainerTask implements ContainerTask {
        private final DockerClient dockerClient;
        private final String containerId;
        private final DockerImage dockerImage;
        private final DockerInstance dockerInstance;

        public StopContainerTask(DockerClient dockerClient, String containerId, DockerInstance dockerInstance, DockerImage dockerImage) {
            this.dockerClient = dockerClient;
            this.containerId = containerId;
            this.dockerImage = dockerImage;
            this.dockerInstance = dockerInstance;
        }

        @Override
        public void execute() {
            dockerInstance.stopContainer(containerId);
        }
    }
}