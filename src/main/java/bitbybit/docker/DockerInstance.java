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
import java.util.concurrent.locks.ReentrantLock;
import com.github.dockerjava.api.exception.ConflictException; //Used to handle container name already in use exception




public class DockerInstance {
    private final DockerClient dockerClient;
    private final ReentrantLock dockerLock = new ReentrantLock();
    public List<Container> containers;
    public DockerInstance(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        this.containers = listContainers();
    }

    public String createContainer(String image, String containerName) throws DockerException {
        dockerLock.lock();
        try {
            DockerMain.getDockerImage().pullImageIfNotExists(image);
            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withName(containerName)
                    .exec();
            containers = listContainers();
            System.out.println("Container created successfully: " + container.getId());
            return container.getId();
        } catch (InterruptedException e) {
            System.err.println("Container Creation Interrupted");
        } catch (ConflictException e) {
            System.err.println("Conatiner name already in use. Generating new name");
            String uniqueContainerName = generateUniqueContainerName(containerName);
            return createContainer(image, uniqueContainerName);
        } catch (Exception e) {
          System.err.println("Cannot create container");
        } finally {
            dockerLock.unlock();
        }
        return null;
    }

    public void startContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            if (isContainerRunning(containerId)) {
                throw new ContainerAlreadyRunningException("Container is already running with ID: " + containerId);
            }
            dockerClient.startContainerCmd(containerId).exec();
            System.out.println("Container Started successfully: "+ containerId);
        }catch (NotFoundException e) {
           System.err.println("Container not found with ID: " + containerId);
        } catch (ContainerAlreadyRunningException e){
            System.err.println("Container is already running with ID: " + containerId);
        }catch (Exception e) {
            System.err.println("Error starting container with ID: " + containerId);
        } finally {
            dockerLock.unlock();
        }
    }

    public String executeCommandInContainer(String containerId, String[] command) throws DockerException {
        dockerLock.lock();
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();

            // Create a ResultCallback to handle the output of the command
            ResultCallback<Frame> callback = new ResultCallback<Frame>() {
                @Override
                public void onStart(Closeable closeable) {
                    System.out.println("Command execution started.");
                }

                @Override
                public void onNext(Frame item) {
                    String output = new String(item.getPayload());
                    System.out.println("Command output: " + output);                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Error during command execution: " + throwable.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("Command execution completed.");
                }

                @Override
                public void close() {
                    // Close any resources when the command execution is finished (optional)
                }
            };

            // Execute the command with the provided callback
            dockerClient.execStartCmd(execId).exec(callback);

            return execId;
        } finally {
            dockerLock.unlock();
        }
    }
    public Container getContainer(String  containerid) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();
            for (Container container : containers) {
                if (container.getId().equals(containerid)) {
                    System.out.println("Container returned");
                    return container;
                }
            }
            throw new NotFoundException("Container not found");
        } catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerid);
        } catch (Exception e) {
            System.err.println("Cannot return container");
        }
        return null; // Container with the specified ID not found
    }

    public String getContainerLogs(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            // Use InspectContainerResponse to get the container's log path
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
            String logPath = inspectionResponse.getLogPath();

            // Use LogContainerCmd to retrieve the container logs
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTailAll();

            // Use StringBuilder to capture the logs
            StringBuilder logs = new StringBuilder();

            // Attach the logs to the StringBuilder
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
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void close() {
                }
            });

            return logs.toString();
        } finally {
            dockerLock.unlock();
        }
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
                System.err.println("Container not found with Name: " + containerName);
            } catch (Exception e) {
                System.err.println("Cannot return ID");
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
            System.err.println("Container not found with Id: " + containerId);
        } catch (Exception e) {
            System.err.println("Cannot return Name");
        }

        return null;
    }


    public void stopContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(containerId);
            stopContainerCmd.exec();
            System.out.println("Container stopped successfully: " + containerId);
        }catch(ContainerNotRunningException e) {
            System.err.println("Container is not running");
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId);
        } catch (Exception e) {
            System.err.println("Cannot stop container");
        } finally {
            dockerLock.unlock();
        }
    }

    public void killContainer(String containerId) throws DockerException {
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
            System.err.println("Container is not running");
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId);
        } catch (Exception e) {
            System.err.println("Cannot kill container");
        } finally {
            dockerLock.unlock();
        }
    } //not recommended for usage

    public void pauseContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            dockerClient.pauseContainerCmd(containerId).exec();
            System.out.println("Container paused successfully: " + containerId);
        }catch (ContainerNotRunningException e){
            System.err.println("Container is not running");
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId);
        } catch (Exception e) {
            System.err.println("Cannot stop container");
        }finally {
            dockerLock.unlock();
        }
    }
    public void unpauseContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            if (!isContainerRunning(containerId)) {
                throw  new ContainerNotRunningException("Container is not running");
            }
            dockerClient.unpauseContainerCmd(containerId).exec();
            System.out.println("Unpaused container " + containerId);
        } catch (ContainerNotRunningException e){
            System.err.println("Container is not running. Cannot unpause it");
        }catch (NotFoundException e) {
            System.err.println("Container not found: " + containerId);
        } catch (NotModifiedException e) {
            System.out.println("Container is not paused: " + containerId);
        } catch (Exception e) {
            System.err.println("Error unpausing container with ID: " + containerId);
        } finally {
            dockerLock.unlock();
        }
    }

    public void restartContainer(String containerId) throws DockerException {
        dockerLock.lock();
        try {
            dockerClient.restartContainerCmd(containerId).exec();
            System.out.println("Restarted container successfully: " + containerId);
        }catch (NotFoundException e) {
            System.err.println("Container not found with ID: " + containerId);
        }catch (Exception e){
            System.err.println("Error restarting container: " + containerId);
        }finally {
            dockerLock.unlock();
        }
    }
    public void removeContainer(String containerId) {
        dockerLock.lock();
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
            System.err.println("Error removing container with ID: " + containerId);
        } finally {
            dockerLock.unlock();
        }
    }


    public String createSnapshot(String containerId, String snapshotImageName) throws DockerException {
        dockerLock.lock();
        try {
            // First, pause the container
            dockerClient.pauseContainerCmd(containerId).exec();

            // Create an image from the paused container
            String snapshotImageId = dockerClient.commitCmd(containerId)
                    .withRepository(snapshotImageName)
                    .exec();

            // Unpause the container
            dockerClient.unpauseContainerCmd(containerId).exec();

            return snapshotImageId;
        } finally {
            dockerLock.unlock();
        }
    }

    public List<Container> listContainers() throws DockerException {
        dockerLock.lock();
        try {
            ListContainersCmd listContainersCmd = dockerClient.listContainersCmd()
                    .withShowAll(true);
            return listContainersCmd.exec();
        } catch (Exception e){
            System.err.println("Error during listing");
        }finally {
            dockerLock.unlock();
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
            System.out.println("Error while Generating name");
        }
         return null;
    }

    private boolean containerNameExists(String Name) {
        List<Container> containers = listContainers();
        return containers.stream().anyMatch(container -> Arrays.asList(container.getNames()).contains("/" + Name));
    }
    private boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse inspectionResponse = dockerClient.inspectContainerCmd(containerId).exec();
            InspectContainerResponse.ContainerState state = inspectionResponse.getState();
            return state != null && state.getRunning();
        } catch (NotFoundException e) {
            System.err.println("Container not found: " + containerId); // Container not found, consider it not running
        }
        return false;
    }

    public void close() {
        dockerLock.lock();
        try {
            dockerClient.close();
        } catch (IOException e) {
            System.out.println("Cannot close Docker Client");
            e.printStackTrace();
        } finally {
            dockerLock.unlock();
        }
   }
    private boolean containerExists(String containerId) {
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

}


