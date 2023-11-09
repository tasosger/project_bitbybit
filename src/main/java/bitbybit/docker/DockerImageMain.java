package bitbybit.docker;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.command.InspectImageResponse;

import java.util.List;

public class DockerImageMain {
    public static void main(String[] args) {
        DockerImage dockerImage = new DockerImage();

        try {
            dockerImage.pullImage("alpine:latest");
            System.out.println("Image pulled successfully.");

            // List images
            System.out.println("Listing images:");
            List<Image> images = dockerImage.listImages();
            for (Image image : images) {
                System.out.println(image.getId());
            }


            System.out.println("Inspecting an image:");
            InspectImageResponse inspection = dockerImage.inspectImage("nginx:latest");
            System.out.println("Image ID: " + inspection.getId());


        } catch (DockerException | InterruptedException e) {

            e.printStackTrace();
        } finally {

            dockerImage.close();
        }
    }
}

