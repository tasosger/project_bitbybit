package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.Container;
import java.awt.Container;


public class DockerInstance {
    private final DockerClient dockerClient;
    private final ReentrantLock dockerLock = new ReentrantLock();

    public DockerInstance() {
        // Initialize the Docker client
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    public String createContainer(String image, String containerName) throws DockerException {
        dockerLock.lock();
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withName(containerName)
                    .exec();
            return container.getId();
        } finally {
            dockerLock.unlock();
        }
    }

    public void startContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            dockerClient.startContainerCmd(containerId).exec();
        } finally {
            dockerLock.unlock();
        }
    }

    public String executeCommandInContainer(String containerId, String[] command) throws DockerException {
        dockerLock.lock();
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String execId = execCreateCmdResponse.getId();

            // Create a ResultCallback to handle the output of the command
            ResultCallback<Frame> callback = new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {
                    // Handle the start of the command execution (optional)
                }

                @Override
                public void onNext(Frame item) {
                    // Handle the output of the command (e.g., print it to the console or process it)
                }

                @Override
                public void onError(Throwable throwable) {
                    // Handle any errors that occur during the command execution (optional)
                }

                @Override
                public void onComplete() {
                    // Handle the completion of the command execution (optional)
                }

                @Override
                public void close() {
                    // Close any resources when the command execution is finished (optional)
                }
            };

            // Execute the command with the provided callback
            dockerClient.execStartCmd(execId).exec(callback);

            return execId;
        } finally {
            dockerLock.unlock();
        }
    }

    public void stopContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(containerId);
            stopContainerCmd.exec();
        } finally {
            dockerLock.unlock();
        }
    }

    public void killContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            KillContainerCmd killContainerCmd = dockerClient.killContainerCmd(containerId);
            killContainerCmd.exec();
        } finally {
            dockerLock.unlock();
        }
    }

    public void pauseContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            dockerClient.pauseContainerCmd(containerId).exec();
        } finally {
            dockerLock.unlock();
        }
    }

    public void restartContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            // First, unpause the container to resume its processes
            dockerClient.unpauseContainerCmd(containerId).exec();

            // Then, restart the container
            dockerClient.restartContainerCmd(containerId).exec();
        } finally {
            dockerLock.unlock();
        }
    }



    public String createSnapshot(String containerId, String snapshotImageName) throws DockerException {
        dockerLock.lock();
        try {
            // First, pause the container
            dockerClient.pauseContainerCmd(containerId).exec();

            // Create an image from the paused container
            String snapshotImageId = dockerClient.commitCmd(containerId)
                    .withRepository(snapshotImageName)
                    .exec();

            // Unpause the container
            dockerClient.unpauseContainerCmd(containerId).exec();

            return snapshotImageId;
        } finally {
            dockerLock.unlock();
        }
    }

    public Container inspectContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();

            // Extract the Container information from InspectContainerResponse
            Container container = new Container();
            container.setId(inspectionResponse.getId());

            // Access the Config property and extract the Image
            ContainerConfig containerConfig = inspectionResponse.getConfig();
            container.setImage(containerConfig.getImage());
            // Add more attributes as needed

            return container;
        } finally {
            dockerLock.unlock();
        }
    }

    public List<Container> listContainers() throws DockerException {
        dockerLock.lock();
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(true);
            return listContainersCmd.exec();
        } finally {
            dockerLock.unlock();
        }
    }

    public void close() {
        dockerLock.lock();
        try {
            dockerClient.close();
        } catch (IOException e) {
            // Handle the exception properly, e.g., print or log an error message
            e.printStackTrace();
        } finally {
            dockerLock.unlock();
        }
    }
}
