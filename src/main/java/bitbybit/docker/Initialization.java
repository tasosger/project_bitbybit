package bitbybit.docker;

import com.github.dockerjava.api.model.Container;
import com.sun.jdi.request.DuplicateRequestException;

import java.util.List;
import java.util.Objects;

public class Initialization {
    public static void initializeInst_im(){
        DockerInstance.initializeThreads();
        DockerInstance.containers = DockerInstance.listContainers();
        DockerImage.images= DockerImage.listImages();
    }
    public static void initialize_db(){
        DatabaseHandler.form_connection();
    }
    public static void init_threads(){
        List<Container> cont = DockerInstance.listContainers();
        for(Container c:cont){
            DockerInstance.executorthreads.add(new DockerInstance.ThreadPairs(c.getId(), new ExecutorThread()));
            DockerInstance.monitorthreads.add(new DockerInstance.ThreadPairs(c.getId(), new MonitorThread(c.getId())));
            Objects.requireNonNull(DockerInstance.getExecThread(c.getId())).start();
            Objects.requireNonNull(DockerInstance.getMonThread(c.getId())).start();
            DatabaseHandler.add_container(c.getId(), c.getNames()[0] , c.getImage());
        }
    }
    public static void initialize_all(){
        initialize_db();
        initializeInst_im();
        init_threads();
    }

}
