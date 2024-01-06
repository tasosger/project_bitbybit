package bitbybit.docker;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volumes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class DockerController {

    @RequestMapping(value = "/CreateContainer", method = {RequestMethod.GET, RequestMethod.POST})
     public String container_creation(@RequestParam(name = "containerName") String containerName,
                                      @RequestParam(name = "imageName") String imageName){
        String containerid = DockerInstance.createContainer(containerName,imageName);
        return "container created with id "+containerid;
    }
    @RequestMapping(value = "/StartContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_start(@RequestParam(name = "containerName") String containerName,
                                     @RequestParam(name= "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);

            if (DockerInstance.getExecThread(containerid) != null) {
                Objects.requireNonNull(DockerInstance.getExecThread(containerid)).addTask(new ExecutorThread.StartContainerTask(containerid, millis));
            } else {
                DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid, new ExecutorThread()));
                DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid, new MonitorThread(containerid)));
                Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
                Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
                Objects.requireNonNull(DockerInstance.getExecThread(containerid)).addTask(new ExecutorThread.StartContainerTask(containerid, millis));
            }

        return containerid;

    }
    @RequestMapping(value = "/StopContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_stop(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        if(DockerInstance.getExecThread(containerid)!=null) {
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.StopContainerTask(containerid));
        } else {
            DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid,new ExecutorThread()));
            DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid,new MonitorThread(containerid)));
            Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
            Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.StopContainerTask(containerid));
        }
        return containerid;
    }
    @RequestMapping(value = "/PauseContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_pause(@RequestParam(name = "containerName") String containerName,
                                  @RequestParam(name = "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        if (DockerInstance.getExecThread(containerid)!=null) {
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.PauseContainerTask(containerid, millis));
        } else {
            DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid,new ExecutorThread()));
            DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid,new MonitorThread(containerid)));
            Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
            Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.PauseContainerTask(containerid, millis));
        }
        return containerid;
    }
    @RequestMapping(value = "/UnpauseContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_unpause(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        if(DockerInstance.getExecThread(containerid)!=null) {
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.UnpauseContainerTask(containerid));
        } else {
            DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid,new ExecutorThread()));
            DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid,new MonitorThread(containerid)));
            Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
            Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.UnpauseContainerTask(containerid));
        }
        return containerid;
    }
    @RequestMapping(value = "/ExecCommandContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_command(@RequestParam(name = "containerName") String containerName,
                                    @RequestParam(name = "command") String command){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        String[] command1 = command.split("\\s+");
        System.out.println(command);
        String res = "kfj";
        res = DockerInstance.executeCommandInContainer(containerid,command1);
        return res;
    }
    @RequestMapping(value = "/RemoveContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_remove(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        if(DockerInstance.getExecThread(containerid)!=null) {
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RemoveContainerTask(containerid));
        } else {
            DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid,new ExecutorThread()));
            DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid,new MonitorThread(containerid)));
            Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
            Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
            DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RemoveContainerTask(containerid));
        }
        return containerid;
    }
    @RequestMapping(value = "/RestartContainer", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_restart(@RequestParam(name = "containerName") String containerName,
                                  @RequestParam(name = "millis") int millis){
        String containerid = DockerInstance.getContainerIdByName(containerName);

            if (DockerInstance.getExecThread(containerid) != null) {
                DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RestartContainerTask(containerid, millis));
            } else {
                DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(containerid, new ExecutorThread()));
                DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(containerid, new MonitorThread(containerid)));
                Objects.requireNonNull(DockerInstance.getExecThread(containerid)).start();
                Objects.requireNonNull(DockerInstance.getMonThread(containerid)).start();
                DockerInstance.getExecThread(containerid).addTask(new ExecutorThread.RestartContainerTask(containerid, millis));
            }

        return containerid;
    }
    @RequestMapping(value = "/ContainerLogs", method = {RequestMethod.GET, RequestMethod.POST})
    public String container_logs(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        StringBuilder logs = DockerInstance.getContainerLogs(containerid);
        return logs.toString();
    }
    @RequestMapping(value = "/ListContainers", method = {RequestMethod.GET, RequestMethod.POST})
    public String list_container(){
        try {
            List<Container> l = DockerInstance.listContainers();
            StringBuilder s = new StringBuilder();
            for (Container c : l) {
                s.append("Container Name: ").append(c.getNames()[0]);
                s.append(", Image: ").append(c.getImage());
                s.append(", Status: ").append(c.getStatus()).append("\n");
            }
            return s.toString();
        } catch (NullPointerException e){
            return null;
        }
    }
    @RequestMapping(value = "/ListRunningContainers", method = {RequestMethod.GET, RequestMethod.POST})
    public String list_runcontainer(){
        List<Container> l = DockerInstance.listrunningContainer();
        StringBuilder s = new StringBuilder();
        for (Container c : l) {
            s.append("Container Name: ").append(c.getNames()[0]);
            s.append(", Image: ").append(c.getImage()).append("\n");
        }
        return s.toString();
    }
    @RequestMapping(value = "/ListPausedContainers", method = {RequestMethod.GET, RequestMethod.POST})
    public String list_pausedcontainer(){
        List<Container> l = DockerInstance.listpausedContainer();
        StringBuilder s = new StringBuilder();
        for (Container c : l) {
            s.append("Container Name: ").append(c.getNames()[0]);
            s.append(", Image: ").append(c.getImage()).append("\n");
        }
        return s.toString();
    }
    @RequestMapping(value = "/GetVolumeMounts", method = {RequestMethod.GET, RequestMethod.POST})
    public String volume_mounts(@RequestParam(name = "containerName") String containerName){
        String containerid = DockerInstance.getContainerIdByName(containerName);
        return   DockerInstance.getVolumeMounts(containerid);
    }



    @RequestMapping(value = "/PullImage", method = {RequestMethod.GET, RequestMethod.POST})
    public void pull_image(@RequestParam(name ="imageName") String imageName){
        DockerImage.pullImage(imageName);
    }
    @RequestMapping(value = "/RemoveImage", method = {RequestMethod.GET, RequestMethod.POST})
    public void remove_image(@RequestParam(name="imageName") String imageName){
        String imageid = DockerImage.getImageIdByName(imageName);
        DockerImage.removeImage(imageid);
    }

    @RequestMapping(value = "/ListImages", method = {RequestMethod.GET, RequestMethod.POST})
    public String list_images(){
        List<Image> e = DockerImage.listImages();
        StringBuilder s = new StringBuilder();
        for (Image i : e) {
            if (i.getRepoTags().length>0) {
                s.append("Image Name: ").append(i.getRepoTags()[0]);
                s.append(", ID: ").append(i.getId()).append("\n");
            } else {
                s.append("ID: ").append(i.getId()).append("\n");
            }
        }
        return s.toString();
    }
    @RequestMapping(value = "/ImageResp", method = {RequestMethod.GET,RequestMethod.POST})
    public String image_resp(@RequestParam(name = "imageName") String image){
        String imageid = DockerImage.getImageIdByName(image);
        InspectImageResponse resp = DockerImage.inspectImage(imageid);
        StringBuilder s = new StringBuilder();
        s.append("Image ID: " + resp.getId()+"\n");
        s.append("Repo Tags: " + resp.getRepoTags()+"\n");
        s.append("OS/Architecture: " + resp.getOs()+"\n");
        s.append("Config: " + resp.getConfig()+"\n");
        s.append("Container Config: " + resp.getContainerConfig()+"\n");
        s.append("Image Size: "+resp.getSize()+"\n");
        return s.toString();
    }
    @RequestMapping(value = "/ListSubnets", method = {RequestMethod.GET, RequestMethod.POST})
    public String subnets() {
     List<Network> nt = DockerInstance.displaySubnets();
     StringBuilder s = new StringBuilder();
     if (nt!=null) {
         for (Network n : nt) {
             s.append("Network ID: ").append(n.getId()).append(" ");
             s.append("Name: ").append(n.getName()).append(" ");
             s.append("Driver: ").append(n.getDriver()).append(" ");
             s.append("Scope: ").append(n.getScope()).append("\n");
         }
     } else {
         s.append("No networks found");

     }     return s.toString();
    }
    @RequestMapping(value = "/ListVolumes", method = {RequestMethod.GET, RequestMethod.POST})
    public static String volumes(){
        List<InspectVolumeResponse> v = DockerInstance.displayDiskVolumes();
        StringBuilder s = new StringBuilder();
        if(v!=null){
            for (InspectVolumeResponse ve:v){
                s.append("Name: ").append(ve.getName()).append(" ");
                s.append("Driver:").append(ve.getDriver()).append("\n");
            }
        } else {
            s.append("No volumes found");

        }
        return s.toString();
    }
    @RequestMapping(value = "/DateMeasurements", method = {RequestMethod.GET, RequestMethod.POST})
    public String dmeasurement(@RequestParam(name = "date") String date){
        return DatabaseHandler.getmeasurement(date);
    }
    @RequestMapping(value = "/IdMeasurements", method = {RequestMethod.GET, RequestMethod.POST})
    public String idmeasurement(@RequestParam(name = "name") String name){
        String id = DockerInstance.getContainerIdByName(name);
        return DatabaseHandler.getidmeasurement(id);
    }
    @RequestMapping(value = "/DateIdMeasurements", method = {RequestMethod.GET, RequestMethod.POST})
    public String idmeasurement(@RequestParam(name = "name") String name,@RequestParam(name = "date") String date){
        String id = DockerInstance.getContainerIdByName(name);
        return DatabaseHandler.getdateidmeasurement(id,date);
    }
    }

