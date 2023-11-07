package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.command.PullImageResultCallback;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Image;


import java.io.IOException;
import java.util.List;

public class DockerImage {
    private final DockerClient dockerClient;

    public DockerImage() {
        // Initialize the Docker client
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    public void pullImage(String imageName) throws DockerException,InterruptedException {
        dockerClient.pullImageCmd("baeldung/alpine")
                .withTag("git")
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
        try {
            dockerClient.close();
        } catch (IOException e) {
            // Handle the exception properly, e.g., print or log an error message
            e.printStackTrace();
        }
    }
}

