/*package bitbybit.docker;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
public class ExecutorThreadMain {
    public static void main(String[] args) {
        String containerID = "your_container_id_here";

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        ExecutorThread executorThread = new ExecutorThread(containerID, dockerClient);
        executorThread.startThread();

        // Usage examples of the methods
        executorThread.pauseThread(); // Pausing the thread

        // Simulating some delay...
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorThread.resumeThread(); // Resuming the thread

        // Simulating some more processing...
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorThread.stopThread(); // Stopping the thread
    }
}*/
