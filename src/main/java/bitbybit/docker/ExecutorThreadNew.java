package bitbybit.docker;

        import com.github.dockerjava.api.DockerClient;
        import com.github.dockerjava.api.model.Container;
        import com.github.dockerjava.core.DockerClientBuilder;
        import com.github.dockerjava.core.DockerClientConfig;
        import com.github.dockerjava.core.DefaultDockerClientConfig;

public class ExecutorThreadNew implements Runnable {
    private volatile boolean running = true;
    private Thread thread;
    private String containerID;
    private DockerClient dockerClient;

    public ExecutorThreadNew(String containerID, DockerClient dockerClient) {
        this.containerID = containerID;
        this.dockerClient = dockerClient;
    }

    public void run() {
        while (running) {
            System.out.println("Thread running for Docker container with ID: " + containerID);

            // Example code using Docker Java API to interact with the Docker container
            Container containerInfo = dockerClient.inspectContainerCmd(containerID).exec();
            System.out.println("Information for container with ID " + containerID + ": " + containerInfo.toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted for Docker container with ID: " + containerID);
                return;
            }
        }
    }

    public void startThread() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stopThread() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void pauseThread() {
        running = false;
    }

    public void resumeThread() {
        running = true;
        synchronized (this) {
            notify();
        }
    }
}