package bitbybit.docker;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DockerMain {
    public static DockerImage dim;
    public static DockerInstance di;
    public static DockerClient dockerClient;

    public static void main(String[] args) {
        dockerClient = initializeDockerClient();
        di = new DockerInstance(dockerClient);
        dim = new DockerImage();

        //try {
        //  dockerInstance.getExecutorThread().join();
        //} catch (InterruptedException e){

        //}
           String containerid = di.createContainer("mycontainer","nginx:latest");
           di.startContainer(containerid);
           dim.displayImageResponce(dim.getImageIdByName("nginx:latest"));

          try {
              Thread.sleep(10000);
          } catch (InterruptedException e){


          }        //container tests
        try {
            MonitorThread m = new MonitorThread(dockerClient,di,containerid);
            m.start();
            try {
                m.join();
            } catch (InterruptedException e){

            }
            for(MonitorThread.ContainerMetrics c: MonitorThread.metricsList){
                System.out.println(c.toString());
            }
          List<Container> cont = di.listrunningContainer();
            for(Container c:cont){
                System.out.println(c.getId());
            }


            di.listpausedContainer();
            di.displaySubnets();
            di.displayDiskVolumes();


// Wait for some time or perform other tasks

// Stop the executor thread after some time or when done with container tasks
            //try {
            //  executorThread.join();
            //}  catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //executorThread.stopExecution();
            //dockerInstance.startContainer(containerId);
            //System.out.println("Container started.");

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


        //dockerInstance.close();
        //dockerImage.close();
    }


    private static DockerClient initializeDockerClient() {
        try {
            return DockerClientBuilder.getInstance
                            ("tcp://localhost:2375")
                    .build();
        } catch (Exception e){
            System.out.println("Cannot initialize client");
            System.exit(1);
        }
        return null;
    }

    private static void showmenu() {
        System.out.println("Choose a task");
        System.out.println("1.Pull image");
        System.out.println("2.Create container");
        System.out.println("3.Start Container");
        System.out.println("4.Stop container");
        System.out.println("5.Exit");
    }
}

