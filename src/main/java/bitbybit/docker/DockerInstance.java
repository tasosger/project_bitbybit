package bitbybit.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;


public class DockerInstance {
    private final DockerClient dockerClient;
    private static final ReentrantLock dockerLock = new ReentrantLock();
    public static List<Container> containers;
    public static List<ThreadPairs> executorthreads = new LinkedList<ThreadPairs>();
    public static List<ThreadPairs> monitorthreads = new LinkedList<ThreadPairs>();

    public DockerInstance(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        this.containers = listContainers();
    }


    public String createContainer(String containerName, String image)  {
        dockerLock.lock();
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withName(containerName)
                    .exec();
            containers = listContainers();
            System.out.println("Container created successfully: " + container.getId());
            executorthreads.add(new ThreadPairs(container.getId(), new ExecutorThread(this)));
           // monitorthreads.add(new ThreadPairs(container.getId(), new MonitorThread(dockerClient,this)));
            return container.getId();
        } catch (ConflictException e) {
            System.err.println("Conatiner name already in use. Generating new name");
            String uniqueContainerName = generateUniqueContainerName(containerName);
            return createContainer(uniqueContainerName, image);
        } catch (Exception e) {
          System.err.println("Cannot create container"+ " " + e.getMessage());
          e.printStackTrace();
        } finally {
           dockerLock.unlock();
        }
        return null;
    }

    public void startContainer(String containerId)  {
       try {
           if (isContainerRunning(containerId)) {
              throw new ContainerAlreadyRunningException("Container is already running with ID: " + containerId);
            }
            dockerClient.startContainerCmd(containerId).exec();
            System.out.println("Container Started successfully: "+ containerId);
           // getMonThread(containerId).start();
        }catch (NotFoundException e) {
           System.err.println("Container not found with ID: " + containerId + " " + e.getMessage());
       } catch (ContainerAlreadyRunningException e){
           System.err.println("Container is already running with ID: " + containerId+ " " + e.getMessage());
        }catch (Exception e) {
            System.err.println("Error starting container with ID: " + containerId+ " " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String executeCommandInContainer(String containerId, String[] command)  {
        try {
            if (!isContainerRunning(containerId)) {
                throw new ContainerNotRunningException("Container is not running");
            }
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();
            StringBuilder commandOutput = new StringBuilder();
            CountDownLatch commandCompleted = new CountDownLatch(1);
            ResultCallback<Frame> callback = new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {
                    System.out.println("Command execution started.");
                }

                @Override
                public void onNext(Frame item) {
                    String output = new String(item.getPayload());
                    commandOutput.append(output);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error during command execution: " + throwable.getMessage());
                    commandCompleted.countDown();
                }

                @Override
                public void onComplete() {
                    System.out.println("Command execution completed.");
                    commandCompleted.countDown();
                }

                @Override
                public void close() {
                    // Close any resources when the command execution is finished (optional)
                }
            };

            dockerClient.execStartCmd(execId).exec(callback);
            commandCompleted.await();
            return commandOutput.toString();
        }catch (ContainerNotRunningException e) {
            System.err.println("Container is not running"+ " " + e.getMessage());
        } catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during command execution: " + e.getMessage());
        }
        return null;
    }
    public void stopContainer(String containerId)  {
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(containerId);
            stopContainerCmd.exec();
            System.out.println("Container stopped successfully: " + containerId);
        }catch(ContainerNotRunningException e) {
            System.err.println("Container is not running"+ " " + e.getMessage());
            e.printStackTrace();
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot stop container"+ " " + e.getMessage());
        }
    }
    public void killContainer(String containerId) {
        dockerLock.lock();
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            KillContainerCmd killContainerCmd = dockerClient.killContainerCmd(containerId);
            killContainerCmd.exec();
            System.out.println("Bye Bye");
            containers = listContainers();
        } catch(ContainerNotRunningException e) {
            System.err.println("Container is not running"+ " " + e.getMessage());
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot kill container"+ " " + e.getMessage());
        } finally {
            dockerLock.unlock();
        }
    }
    public ExecutorThread getExecThread(String containerid){
        try{
            for(ThreadPairs t: executorthreads){
                if(t.getId()==containerid){
                    return (ExecutorThread) t.getThread();
                }
            }
        }  catch (NotFoundException e){
            System.err.println("Cantainer not found");
        }
        return null;
    }
    public MonitorThread getMonThread(String containerid){
        try{
            for(ThreadPairs t: monitorthreads){
                if(t.getId()==containerid){
                    return (MonitorThread) t.getThread();
                }
            }
        }  catch (NotFoundException e){
            System.err.println("Cantainer not found");
        }
        return null;
    }

    public void pauseContainer(String containerId)  {
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            dockerClient.pauseContainerCmd(containerId).exec();
            System.out.println("Container paused successfully: " + containerId);
        }catch (ContainerNotRunningException e){
            System.err.println("Container is not running"+ " " + e.getMessage());
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot stop container"+ containerId+ " " + e.getMessage());
        }
    }
    public void unpauseContainer(String containerId) {
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            dockerClient.unpauseContainerCmd(containerId).exec();
            System.out.println("Unpaused container " + containerId);
        } catch (ContainerNotRunningException e){
            System.err.println("Container is running. Cannot unpause it"+ " " + e.getMessage());
        }catch (NotFoundException e) {
            System.err.println("Container not found: " + containerId+ " " + e.getMessage());
        } catch (NotModifiedException e) {
            System.out.println("Container is not paused: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error unpausing container with ID: " + containerId+ " " + e.getMessage());
        }
    }
    public void restartContainer(String containerId)  {
        try {
            dockerClient.restartContainerCmd(containerId).exec();
            System.out.println("Restarted container successfully: " + containerId);
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        }catch (Exception e){
            System.err.println("Error restarting container: " + containerId+ " " + e.getMessage());
        }
    }
    public void removeContainer(String containerId) {
        try {
            if (!containerExists(containerId)) {
                System.err.println("Container with ID " + containerId + " does not exist.");
                return;
            }
            stopContainer(containerId);
            RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(containerId);
            removeContainerCmd.exec();
            System.out.println("Container with ID " + containerId + " removed successfully.");
            containers = listContainers();
        } catch (Exception e) {
            System.err.println("Error removing container with ID: " + containerId+ " " + e.getMessage());
        }
    }

    public Container getContainer(String containerId) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            for (Container container : containers) {
                if (container.getId().equals(containerId)) {
                    return container;
                }
            }
            throw new NotFoundException("Container not found");
        } catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot return container"+ " " + e.getMessage());
        }
        return null;
    }
    public String getContainerLogs(String containerId)  {
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
            String logPath = inspectionResponse.getLogPath();
            System.out.println("Log path: " + logPath);
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTailAll();

            StringBuilder logs = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            logContainerCmd.exec(new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onNext(Frame item) {
                    logs.append(new String(item.getPayload()));
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }

                @Override
                public void close() {
                }
            });
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for log collection " + e.getMessage());
        }
        return logs.toString();
        } catch (ContainerNotRunningException e){
            System.err.println("Container not running "+ containerId+" "+e.getMessage());
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during log retrieval: " + e.getMessage());
        }
        return null;
    }
    public String getContainerIdByName(String containerName) {
        try {
            for (Container container : containers) {
                if (Arrays.asList(container.getNames()).contains("/" + containerName)) {
                    return container.getId();
                }
            }
            throw new NotFoundException("container not found with Name: " + containerName);
        } catch (NotFoundException e) {
                System.err.println("Container not found with Name: " + containerName+ " " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Cannot return ID"+ " " + e.getMessage());
            }
        return null; // Container with the specified name not found
    }
    public String getContainerNameById(String containerId) {
        try {
            for (Container container : containers) {
                if (container.getId().equals(containerId)) {
                    return container.getNames()[0];
                }
            }
            throw new NotFoundException("container not found with ID: " + containerId);
        } catch (NotFoundException e) {
            System.err.println("Container not found with Id: " + containerId+ " " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cannot return Name"+ " " + e.getMessage());
        }

        return null;
    }
    public List<Container> listContainers()  {
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(true);
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    public void displayDiskVolumes()    {
        try {
        ListVolumesResponse listVolumesResponse = dockerClient.listVolumesCmd().exec();
        List<InspectVolumeResponse> volumes = listVolumesResponse.getVolumes();
        if (volumes.isEmpty()) {
            System.out.println("No volumes found.");
        } else {
            System.out.println("DISPLAYING VOLUMES");
            for (InspectVolumeResponse inspectVolume : volumes) {
                System.out.println("Volume Name: " + inspectVolume.getName());
                System.out.println("Driver: " + inspectVolume.getDriver());
                System.out.println("Mountpoint: " + inspectVolume.getMountpoint());
            }
        }
    } catch (Exception e) {
        System.err.println("Error displaying disk volumes: " + e.getMessage());
    }
    }
    private String getVolumeMounts(String containerId) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();

            for (Container container : containers) {
                if (container.getId().equals(containerId)) {
                    return container.getMounts().toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting volume mounts for container " + containerId+" "+e.getMessage());
        }

        return List.of().toString();
    }

    public void displaySubnets() {
        try {
        List<Network> networks = dockerClient.listNetworksCmd().exec();
            if (networks.isEmpty()) {
                System.out.println("No networks found.");
            } else {
                System.out.println("DISPLAYING NETWORKS");
        for (Network network : networks) {
            System.out.println("Network Name: " + network.getName());
            List<Network.Ipam.Config> ipamConfigs = network.getIpam().getConfig();

            if (!ipamConfigs.isEmpty()) {
                System.out.println("Subnet: " + ipamConfigs.get(0).getSubnet());
                System.out.println("-----------------------");
            } else {
                System.out.println("No IPAM configurations found for network: " + network.getName());
            }
        }
        }

            } catch (Exception e) {
        System.err.println("Error displaying subnets: " + e.getMessage());
        e.printStackTrace();
    }
    }

    public List<Container> listrunningContainer(){
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(false);
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    public List<Container> listpausedContainer(){
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(false).withStatusFilter(Collections.singleton("paused"));
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    private String generateUniqueContainerName(String Name) {
        try {
            int i = 1;
            String newName = Name + "-" + i;
            while (containerNameExists(newName)) {
                i++;
                newName = Name + "-" + i;
            }
            return newName;
        } catch (Exception e){
            System.out.println("Error while Generating name"+ " " + e.getMessage());
        }
         return null;
    }

    public boolean containerNameExists(String Name) {
        List<Container> containers = listContainers();
        return containers.stream().anyMatch(container -> Arrays.asList(container.getNames()).contains("/" + Name));
    }
    public boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
            InspectContainerResponse.ContainerState state = inspectionResponse.getState();
            return state != null && state.getRunning();
        } catch (NotFoundException e) {
            System.err.println("Container not found: " + containerId+ " " + e.getMessage()); // Container not found, consider it not running
        } catch (Exception e) {
            System.err.println("An error has occured"+ " " + e.getMessage());
        }
        return false;
    }

    public void close() {
        dockerLock.lock();
        try {
            dockerClient.close();
            System.out.println("Docker Instance Signing Off");
        } catch (IOException e) {
            System.out.println("Cannot close Docker Client");
        } finally {
            dockerLock.unlock();
        }
   }
    public static boolean containerExists(String containerId) {
        return containers.stream().anyMatch(container -> container.getId().equals(containerId));
    }
    private static class ContainerAlreadyRunningException extends Exception {
        public ContainerAlreadyRunningException(String message) {
            //System.err.println("Container already running");
        }
    }
    private static class ContainerNotRunningException extends Exception {
        public ContainerNotRunningException(String message) {
            //System.err.println("Container is not running");
        }
    }

    private static DockerClient initializeDockerClient() {
        return DockerClientBuilder.getInstance
                        ("tcp://localhost:2375")
                .build();
    }
    private class ThreadPairs{
        private String containerid;
        private Thread thread;
        public ThreadPairs(String containerid, Thread thread){
            this.containerid = containerid;
            this.thread = thread;
        }
        public Thread getThread(){
            try{
                return thread;
            } catch (Exception e){
                System.err.println("Error while returning thread");
            }
            return null;
        }
        public String getId(){
            try{
                return containerid;
            } catch (Exception e){
                System.err.println("Error while returning id");
            }
            return null;
        }
    }

}
