package bitbybit.docker;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Container;

import java.util.List;
import java.util.Scanner;

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
            di.listrunningContainer();
            di.listpausedContainer();
            di.displaySubnets();
            di.displayDiskVolumes();
            //Scanner scan = new Scanner(System.in);
            //String containerId = dockerInstance.createContainer("nginx:latest", "my-nginx6");
            //String[] command = {"ls", "/"};
            //dockerInstance.startContainer(containerId);
            //String s = dockerInstance.executeCommandInContainer(containerId, command);

            //System.out.println("Container Logs:\n" + s);
            //  MonitorThread mt = new MonitorThread(dockerClient, containerId);
            //mt.start();
            // dockerInstance.stopContainer(containerId);
            // try {
            //    mt.join();
            // } catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //for (MonitorThread.ContainerMetrics c : MonitorThread.metricsList){
            //   System.out.println(c.toString());
            //}


            // String s2  = dockerInstance.getContainerLogs(containerId);
            //try {
            //    Thread.sleep(5000);
            //} catch (InterruptedException e) {


            //}
            //System.out.println(s2);
            //String containerId2 = dockerInstance.createContainer("nginx:latest", "my-nginx6");
            //dockerInstance.startContainer(containerId);
            //String containerId2 = dockerInstance.createContainer("nginx:latest", "my-nginx6");
// Create a list of tasks to execute
            //dockerInstance.startContainer(containerId);
            /*ExecutorThread executorThread = new ExecutorThread(containerId, dockerImage, dockerInstance);
            ExecutorThread executorThread2 = new ExecutorThread(containerId2, dockerImage, dockerInstance);
            executorThread.addTask(new ExecutorThread.StartContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.PauseContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.StopContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.StartContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.RestartContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.PauseContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.UnpauseContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.StopContainerTask( containerId, dockerInstance));
            executorThread.addTask(new ExecutorThread.StopContainerTask( containerId, dockerInstance));

            executorThread2.addTask(new ExecutorThread.StartContainerTask( containerId2, dockerInstance));
            executorThread2.addTask(new ExecutorThread.PauseContainerTask( containerId2, dockerInstance));
            executorThread2.addTask(new ExecutorThread.StopContainerTask( containerId2, dockerInstance));
            executorThread2.addTask(new ExecutorThread.StartContainerTask( containerId2, dockerInstance));
            MonitorThread monitorThread1 = new MonitorThread(dockerClient, containerId);
            MonitorThread monitorThread2 = new MonitorThread(dockerClient, containerId);
            executorThread.start();
            executorThread2.start();*/

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

