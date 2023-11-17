package bitbybit.docker;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Container;

import java.util.List;
import java.util.Scanner;

public class DockerMain {
    public static DockerImage dockerImage;
    public static DockerInstance dockerInstance;
    public static void main(String[] args) {
        DockerClient dockerClient = initializeDockerClient();

        dockerImage = new DockerImage(dockerClient);
        dockerInstance = new DockerInstance(dockerClient);

        //image tests
        try {
            dockerImage.searchImages("alpine:latest");
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
        }

        //container tests
        try {

            Scanner scan = new Scanner(System.in);
            //String containerId = dockerInstance.createContainer("nginx:latest", "my-nginx6");
            //String containerId2 = dockerInstance.createContainer("nginx:latest", "my-nginx6");
// Create a list of tasks to execute
            //List<ExecutorThread.ContainerTask> tasks =
            //dockerInstance.removeContainer("428077b1f1e17ceefc9b686cc2e0d8bec1e1044c4c4544badd502eb2133f0764");
            //Container c = dockerInstance.getContainer("428077b1f1e17ceefc9b686cc2e0d8bec1e1044c4c4544badd502eb2133f0764");
           //String containerName = scan.nextLine();
           // ExecutorThread executorThread2 = new ExecutorThread(dockerClient, containerId, tasks2);
            //String containerId = dockerInstance.getContainerIdByName("my-nginx6-58");
            //ExecutorThread executorThread = new ExecutorThread(dockerClient, containerId, tasks);
            //executorThread.start();
            //executorThread2.start();
            //System.out.println("Main is here");
            //dockerInstance.startContainer("l,cldcdcl");
            //dockerInstance.startContainer(containerId);
            //String name = dockerInstance.getContainerIdByName("my-nginx6-58");
            //System.out.println(name);
            //dockerInstance.startContainer("kdj");
            //dockerInstance.pauseContainer(containerId);
            //dockerInstance.stopContainer(containerId);
            //dockerInstance.unpauseContainer(containerId);

            //dockerInstance.startContainer(containerId);
            //dockerInstance.pauseContainer(containerId);
            //dockerInstance.restartContainer(containerId);
            //dockerInstance.startContainer(containerId);

// Wait for some time or perform other tasks

// Stop the executor thread after some time or when done with container tasks
            //try {
              //  executorThread.join();
            //}  catch (InterruptedException e) {
              //    e.printStackTrace();
                //}
            //executorThread.stopExecution();
            /*dockerInstance.startContainer(containerId);
            System.out.println("Container started.");
            MonitorThread monitorThread = new MonitorThread(dockerClient, containerId);*/
            //monitorThread.start();
           // try {
               // monitorThread.join();
            //} catch (InterruptedException e) {
              //  e.printStackTrace();
            //}
           // monitorThread.printFinalMetrics();

           // String[] command = { "sh", "-c", "echo Hello, Docker!" };
            //String execId = dockerInstance.executeCommandInContainer(containerId, command);
            //System.out.println("Command executed in container. Exec ID: " + execId + " " +containerId);


           // dockerInstance.stopContainer(containerId);
            //System.out.println("Container stopped.");

            //System.out.println(dockerInstance.getContainerLogs(containerId));

            // Inspect container
            //Container container = dockerInstance.inspectContainer(containerId);
            //System.out.println("Container ID: " + container.getId());
            //System.out.println("Container Image: " + container.getImage());



            /*List<Container> containers = dockerInstance.listContainers();
            System.out.println("Listing all containers:");
            for (Container c : containers) {
               System.out.println( c.getId());

            }*/

        } catch (DockerException e) {
            e.printStackTrace();
        }


        dockerInstance.close();
        dockerImage.close();
    }



    private static DockerClient initializeDockerClient() {
        return DockerClientBuilder.getInstance
                ("tcp://localhost:2375")
                .build();
    }
    public static DockerImage getDockerImage(){
        return dockerImage;
    }

    public static DockerInstance getDockerInstance(){
        return dockerInstance;
    }

}
