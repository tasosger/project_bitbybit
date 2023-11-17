/*package bitbybit.docker;

import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;

import java.util.List;

public class DockerInstanceMain{

    public static void main(String[] args) {
        DockerInstance dockerInstance = new DockerInstance();

        try {

            String containerId = dockerInstance.createContainer("nginx:latest", "my-nginx5");
            System.out.println("Container created with ID: " + containerId);

            dockerInstance.startContainer(containerId);
            System.out.println("Container started.");


            String[] command = {"/bin/sh", "-c", "echo 'Hello, Docker!' > /tmp/greeting.txt"};
            String execId = dockerInstance.executeCommandInContainer(containerId, command);
            System.out.println("Command executed in container. Exec ID: " + execId + " " +containerId);


            dockerInstance.stopContainer(containerId);
            System.out.println("Container stopped.");

            System.out.println(dockerInstance.getContainerLogs(containerId));

            // Inspect container
            //Container container = dockerInstance.inspectContainer(containerId);
            //System.out.println("Container ID: " + container.getId());
            //System.out.println("Container Image: " + container.getImage());



            List<Container> containers = dockerInstance.listContainers();
            System.out.println("Listing all containers:");
            for (Container c : containers) {
                System.out.println("Container ID: " + c.getId());

            }

        } catch (DockerException e) {
            e.printStackTrace();
        } finally {

            dockerInstance.close();
        }
    }
}*/