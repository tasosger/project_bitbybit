package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;


import java.util.Collections;
import java.io.IOException;
import java.util.List;

public class DockerImage {
    private DockerClient dockerClient;
    private List<Image> images;

    public DockerImage(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }
    public void pullImage(String imageName) throws DockerException,InterruptedException {
        dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        images = listImages();
    }

    public void pullImage(String imageName, String tag) throws DockerException,InterruptedException {
        dockerClient.pullImageCmd(imageName)
                .withTag(tag)
                .exec(new PullImageResultCallback())
                .awaitCompletion(30, TimeUnit.SECONDS);
        images = listImages();
    }
    public String getImageIdByName(String imageName) {

        for (Image image : images) {
            if (Arrays.asList(image.getRepoTags()).contains(imageName)) {
                return image.getId();
            }
        }

        return null; // Container with the specified name not found
    }
    public String getImageNameById(String imageId) {

        for (Image image : images) {
            if (image.getId().equals(imageId)) {
                return image.getRepoTags()[0]; // Assuming the first tag is the primary tag
            }
        }

        return null; // Image with the specified ID not found
    }
    public void pushImage(String imageName) throws DockerException, InterruptedException {
        dockerClient.pushImageCmd(imageName)
                .withTag("latest") // Specify the tag of the image you want to push
                .exec(new PushImageResultCallback())
                .awaitSuccess(); // Wait for the push operation to complete
    }

    public void removeImage(String imageId) throws DockerException {
        dockerClient.removeImageCmd(imageId).exec();
        images = listImages();
    }

    //useless method :)
    public void pullImageIfNotExists(String imageName) throws DockerException, InterruptedException{
        List<Image> images = dockerClient.listImagesCmd()
                .withImageNameFilter(imageName)
                .exec();

        if (images.isEmpty()) {
            pullImage(imageName);
            System.out.println("Image was not already pulled. Image pulled successfully");
        }
    }

    public List<Image> listImages() throws DockerException {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        return listImagesCmd.exec();
    }

    public List<SearchItem> searchImages(String name) throws DockerException {
        return dockerClient.searchImagesCmd(name).exec();
    }

    public InspectImageResponse inspectImage(String imageId) throws DockerException {
        return dockerClient.inspectImageCmd(imageId).exec();
    }

    public void buildImage(String dockerfilePath, String imageName) throws DockerException {
        dockerClient.buildImageCmd()
                .withDockerfilePath(dockerfilePath)
                .withTags(Collections.singleton(imageName))
                .exec(new BuildImageResultCallback())
                .awaitImageId();
    }
    public void close() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Docker client is not initialized. Make sure the initialization process was successful.");
        }
    }
    }