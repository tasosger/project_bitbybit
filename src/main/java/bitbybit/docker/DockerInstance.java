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
    private static final DockerClient dockerClient = initializeDockerClient();
    private static final ReentrantLock dockerLock = new ReentrantLock();
    public static List<Container> containers;
    public static List<ThreadPairs> executorthreads = new LinkedList<ThreadPairs>();
    public static List<ThreadPairs> monitorthreads = new LinkedList<ThreadPairs>();
    public static void initializeThreads(){
        for (Container container: listContainers()){
            executorthreads.add(new ThreadPairs(container.getId(), new ExecutorThread()));
            monitorthreads.add(new ThreadPairs(container.getId(), new MonitorThread(container.getId())));
            getExecThread(container.getId()).start();
            getMonThread(container.getId()).start();
        }
    }
    public static void closeThreads(){
        for(ThreadPairs p: monitorthreads){
            p.getThread().interrupt();
        }
        for(ThreadPairs p: executorthreads){
            p.getThread().interrupt();
        }

    }
    public static String createContainer(String containerName, String image)  {
        dockerLock.lock();
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withName(containerName)
                    .exec();
            containers = listContainers();
            System.out.println("Container created successfully: " + container.getId());
            executorthreads.add(new ThreadPairs(container.getId(), new ExecutorThread()));
            monitorthreads.add(new ThreadPairs(container.getId(), new MonitorThread(container.getId())));
            Objects.requireNonNull(getExecThread(container.getId())).start();
            Objects.requireNonNull(getMonThread(container.getId())).start();
            DatabaseHandler.add_container(container.getId(), containerName , image);
            return container.getId();
        } catch (ConflictException e) {
            System.err.println("Container name already in use. Generating new name");
            String uniqueContainerName = generateUniqueContainerName(containerName);
            return createContainer(uniqueContainerName, image);
        } catch (Exception e) {
            System.err.println("Cannot create container"+ " " + e.getMessage());
        } finally {
           dockerLock.unlock();
        }
        return null;
    }
    public static void startContainer(String containerId)  {
       try {
           if (isContainerRunning(containerId)) {
              throw new ContainerAlreadyRunningException("Container is already running with ID: " + containerId);
            }
            dockerClient.startContainerCmd(containerId).exec();
            System.out.println("Container Started successfully: "+ containerId);
        }catch (NotFoundException e) {
           System.err.println("Container not found with ID: " + containerId + " " + e.getMessage());
       } catch (ContainerAlreadyRunningException e){
           System.err.println("Container is already running with ID: " + containerId+ " " + e.getMessage());
        }catch (Exception e) {
            System.err.println("Error starting container with ID: " + containerId+ " " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String executeCommandInContainer(String containerId, String[] command)  {
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
    public static void stopContainer(String containerId)  {
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
    public static void killContainer(String containerId) {
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
    public static ExecutorThread getExecThread(String containerid){
        try{
            for(ThreadPairs t: executorthreads){
                if(t.getId().equals(containerid)){
                    return (ExecutorThread) t.getThread();
                }
            }
            return null;
        }  catch (NotFoundException e){
            System.err.println("Cantainer not found");
        }
        return null;
    }
    public static MonitorThread getMonThread(String containerid){
        try{
            for(ThreadPairs t: monitorthreads){
                if(t.getId().equals(containerid)){
                    return (MonitorThread) t.getThread();
                }
            }
        }  catch (NotFoundException e){
            System.err.println("Cantainer not found");
        }
        return null;
    }
    public static void pauseContainer(String containerId)  {
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
    public static void unpauseContainer(String containerId) {
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
    public static void restartContainer(String containerId)  {
        try {
            dockerClient.restartContainerCmd(containerId).exec();
            System.out.println("Restarted container successfully: " + containerId);
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId+ " " + e.getMessage());
        }catch (Exception e){
            System.err.println("Error restarting container: " + containerId+ " " + e.getMessage());
        }
    }
    public static void removeContainer(String containerId) {
        try {
            if (!containerExists(containerId)) {
                System.err.println("Container with ID " + containerId + " does not exist.");
                return;
            }
            if (isContainerRunning(containerId)) {
                stopContainer(containerId);
            }
            getMonThread(containerId).interrupt();
            System.out.println("Removing container");
            RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(containerId);
            removeContainerCmd.exec();
            getExecThread(containerId).stopthread();
            System.out.println("Container with ID " + containerId + " removed successfully.");
            containers = listContainers();
        } catch (Exception e) {
            System.err.println("Error removing container with ID: " + containerId+ " " + e.getMessage());
        }
    }
    public static Container getContainer(String containerId) {
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
    public static String getContainerLogs(String containerId)  {
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
    public static String getContainerIdByName(String containerName) {
        try {
            containers = listContainers();
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
    public static String getContainerNameById(String containerId) {
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
    public static List<Container> listContainers()  {
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(true);
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    public static List<InspectVolumeResponse> displayDiskVolumes()    {
        try {
        ListVolumesResponse listVolumesResponse = dockerClient.listVolumesCmd().exec();
        List<InspectVolumeResponse> volumes = listVolumesResponse.getVolumes();
        return volumes;

    } catch (Exception e) {
        System.err.println("Error displaying disk volumes: " + e.getMessage());
    }
        return null;
    }
    public static  String getVolumeMounts(String containerId) {
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

        return null;
    }

    public static List<Network> displaySubnets() {
        try {
        List<Network> networks = dockerClient.listNetworksCmd().exec();
        return networks;
        } catch (Exception e) {
        System.err.println("Error displaying subnets: " + e.getMessage());
    }
        return null;
    }

    public static List<Container> listrunningContainer(){
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(false);
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    public static List<Container> listpausedContainer(){
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(false).withStatusFilter(Collections.singleton("paused"));
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing"+ " " + e.getMessage());
        }
        return null;
    }
    private static String generateUniqueContainerName(String Name) {
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

    public static boolean containerNameExists(String Name) {
        List<Container> containers = listContainers();
        return containers.stream().anyMatch(container -> Arrays.asList(container.getNames()).contains("/" + Name));
    }
    public static boolean isContainerRunning(String containerId) {
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

    public static void close() {
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
    public static class ThreadPairs{
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
