package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.*;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.command.InspectImageResponse;


import java.io.IOException;

public class DockerImage {
    private static final DockerClient dockerClient= initializeDockerClient();
    public static List<Image> images;
    public DockerImage() {
       images = listImages();
    }
    private void pullImage(String imageName) {
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(30, TimeUnit.SECONDS);
            images = listImages();
            System.out.println("Image pulled successfully: " + imageName);
        } catch (InterruptedException e) {
            System.err.println("Pull Interrupted");
        } catch (NotFoundException e) {
            System.err.println("Image does not exist"+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Image can not be pulled"+ " " + e.getMessage());
        }
    }
    public void pullImage(String imageName, String tag) {
        try {
            dockerClient.pullImageCmd(imageName)
                    .withTag(tag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(30, TimeUnit.SECONDS);
            images = listImages();
            System.out.println("Image pulled successfully: " + imageName);
        } catch (InterruptedException e) {
            System.err.println("Pull Interrupted"+ " " + e.getMessage());
        } catch (NotFoundException e) {
            System.err.println("Image does not exist"+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Image can not be pulled"+ " " + e.getMessage());
        }
    }
    public void pullImageIfNotExists(String imageName) {
        try{
        List<Image> images = dockerClient.listImagesCmd()
                .withImageNameFilter(imageName)
                .exec();

        if (images.isEmpty()) {
            pullImage(imageName);
            System.out.println("Image was not already pulled. Image pulled successfully");
        }
        }catch(Exception e){
            System.err.println("Cannot pull requested image"+ " " + e.getMessage());
            }

    }
    public static String getImageIdByName(String imageName) {
        try {
            for (Image image : images) {
                if (Arrays.asList(image.getRepoTags()).contains(imageName)) {
                    return image.getId();
                }
            }
            throw new NotFoundException("Image not found");
        } catch (NotFoundException e) {
            System.err.println("Image not found with name: " + imageName+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot return ID"+ " " + e.getMessage());
        }
        return null;
    }
    public static String getImageNameById(String imageId) {
        try {
            for (Image image : images) {
                if (image.getId().equals(imageId)) {
                    return image.getRepoTags()[0]; // Assuming the first tag is the primary tag
                }
            }
            throw new NotFoundException("Image not found with ID: " + imageId);
        } catch (NotFoundException e) {
            System.err.println("Image not found with Id: " + imageId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot return Name"+ " " + e.getMessage());
        }
        return null;
    }
    public void removeImage(String imageID)  {
        try {
            dockerClient.removeImageCmd(imageID).exec();
            System.out.println("Image removed: " + imageID);
            images = listImages();
        } catch (NotFoundException e) {
            System.err.println("Image does not exist"+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot remove image"+ " " + e.getMessage());
        }
    }
    public List<Image> listImages() {
        try {
            ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
            return listImagesCmd.exec();
        } catch (Exception e) {
            System.err.println("Cannot list images"+ " " + e.getMessage());
        }
        return null;
    }

    public List<SearchItem> searchImages(String name) {
        try {
            return dockerClient.searchImagesCmd(name).exec();
        } catch (NotFoundException e) {
                System.err.println("Image does not exist"+ " " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Cannot return inspect object"+ " " + e.getMessage());
            }
            return null;
    }

    public InspectImageResponse inspectImage(String imageID) {
        try {
            return dockerClient.inspectImageCmd(imageID).exec();
        } catch (NotFoundException e) {
            System.err.println("Image does not exist"+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot return inspect object"+ " " + e.getMessage());
        }
        return null;
    }
    public void displayImageResponce(String id){
        InspectImageResponse resp = inspectImage(id);
        if (resp != null) {
            System.out.println("Image ID: " + resp.getId());
            System.out.println("Repo Tags: " + resp.getRepoTags());
            System.out.println("OS/Architecture: " + resp.getOs());
            System.out.println("Config: " + resp.getConfig());
            System.out.println("Container Config: " + resp.getContainerConfig());
            Long imageSize = resp.getSize();
            if (imageSize != null) {
                System.out.println("Image Size: " + imageSize + " bytes");
            }
        }
    }
    public void close() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                System.err.println("Can not close client");
            } catch (Exception e) {
                System.err.println("Can not close client");
            }
        } else {
            System.err.println("Docker client is not initialized. Make sure the initialization was successful.");
        }
    }

    private class InvalidImageNameException extends Exception {
        public InvalidImageNameException(String message) {
            super(message);
        }
    }

    private static DockerClient initializeDockerClient() {
        return DockerClientBuilder.getInstance
                        ("tcp://localhost:2375")
                .build();
    }

    }
