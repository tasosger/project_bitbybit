package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.command.InspectImageResponse;


import java.io.IOException;
import java.util.List;

public class DockerImage {
    private DockerClient dockerClient;

    public DockerImage() {
        try {
            // Create a Docker client with the desired Docker host
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://localhost:2375")
                    .build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
            System.out.println("Docker client initialized successfully.");
        } catch (Exception e) {
            // Handle any exceptions that occur during initialization
            e.printStackTrace();
            System.err.println("Docker client initialization failed.");
        }
    }
    public void pullImage(String imageName) throws DockerException,InterruptedException {
        dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
    }

    public void pullImage(String imageName, String tag) throws DockerException,InterruptedException {
        dockerClient.pullImageCmd(imageName)
                .withTag(tag)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
    }

    public List<Image> listImages() throws DockerException {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        return listImagesCmd.exec();
    }

    public InspectImageResponse inspectImage(String imageId) throws DockerException {
        return dockerClient.inspectImageCmd(imageId).exec();
    }
    public void close() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                // Handle the exception properly, e.g., print or log an error message
                e.printStackTrace();
            }
        } else {
            System.err.println("Docker client is not initialized. Make sure the initialization process was successful.");
        }
    }
    }


