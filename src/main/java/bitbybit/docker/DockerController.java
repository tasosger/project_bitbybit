package bitbybit.docker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
@RestController
public class DockerController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @PostMapping("/CreateContainer")
     public String container_creation(@RequestParam(name = "containerName") String containerName,
                                      @RequestParam(name = "imageName") String imageName){
        String containerid = DockerInstance.createContainer(containerName,imageName);
        return "container created with id "+containerid;
    }
    @PostMapping("/StartContainer")
    public String container_start(@RequestParam(name = "containerName") String containerName,
                                     @RequestParam(name= "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.StartContainerTask(containerid,millis));
        return containerid;

    }
    @PostMapping("/StopContainer")
    public String container_stop(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.StopContainerTask(containerid));
        return containerid;
    }
    @PostMapping("/PauseContainer")
    public String container_pause(@RequestParam(name = "containerName") String containerName,
                                  @RequestParam(name = "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.PauseContainerTask(containerid,millis));
        return containerid;
    }
    @PostMapping("/UnpauseContainer")
    public String container_unpause(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.UnpauseContainerTask(containerid));
        return containerid;
    }
    @PostMapping("/ExecCommandCont")
    public String container_command(@RequestParam(name = "containerName") String containerName,
                                    @RequestParam(name = "command") String[] command){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.ExecuteCommandContainerTask(containerid,command));
        return containerid;
    }
    @PostMapping("/RemoveContainer")
    public String container_remove(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RemoveContainerTask(containerid));
        return containerid;
    }
    @PostMapping("/RestartContainer")
    public String container_restart(@RequestParam(name = "containerName") String containerName,
                                  @RequestParam(name = "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RestartContainerTask(containerid,millis));
        return containerid;
    }

}